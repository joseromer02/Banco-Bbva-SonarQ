package com.banco.service.impl;

import com.banco.dto.*;
import com.banco.entity.Cuenta;
import com.banco.entity.EstadoTransaccion;
import com.banco.entity.Transaccion;
import com.banco.exception.NotDataFoundException;
import com.banco.repository.CuentaRepository;
import com.banco.repository.TransaccionRepository;
import com.banco.service.AuthService;
import com.banco.service.TransaccionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransaccionServiceImpl implements TransaccionService {

    @Value("${laredo.token}")
    private String laredoToken;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private AuthService tokenService;  // Autenticación para obtener el token

    @Override
    public List<TransaccionDto> obtenerTransaccionesPorCuenta(Long cuentaId) {
        return transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoId(cuentaId, cuentaId)
                .stream()
                .map(transaccion -> new TransaccionDto(
                        transaccion.getFecha(),
                        transaccion.getDescripcion(),
                        transaccion.getMonto(),
                        determinarTipoTransaccion(cuentaId, transaccion)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Transaccion realizarTransferencia(Long cuentaOrigenNumero, Long cuentaDestinoNumero, BigDecimal monto, String descripcion) {
        // Obtener la cuenta origen
        Optional<Cuenta> cuentaOrigenOpt = cuentaRepository.findByNumeroCuenta(cuentaOrigenNumero);
        if (cuentaOrigenOpt.isEmpty()) {
            throw new NotDataFoundException("Cuenta origen no encontrada");
        }
        Cuenta cuentaOrigen = cuentaOrigenOpt.get();

        // Validar saldo suficiente en la cuenta origen
        validarSaldoSuficiente(new CuentaResponseDto(cuentaOrigen), monto);

        // Obtener la cuenta destino
        Optional<Cuenta> cuentaDestinoOpt = cuentaRepository.findByNumeroCuenta(cuentaDestinoNumero);
        if (cuentaDestinoOpt.isEmpty()) {
            throw new NotDataFoundException("Cuenta destino no encontrada");
        }
        Cuenta cuentaDestino = cuentaDestinoOpt.get();

        // Actualizar los saldos
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));  // Debitar
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(monto));     // Acreditar

        // Crear la transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMonto(monto);
        transaccion.setDescripcion(descripcion);
        transaccion.setFecha(LocalDateTime.now());

        // Guardar las cuentas actualizadas y la transacción
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);
        return transaccionRepository.save(transaccion);
    }

    public String iniciarTransferenciaExterna(CuentaResponseDto cuentaOrigenDto,
                                               String cuentaDestinoNumero,
                                               String nombreDestino,
                                               Long codigoBancoDestino,
                                               BigDecimal monto,
                                               String descripcion,
                                               Long codigoBancoOrigen) throws Exception {
        String url = "https://mls-ubp.azurewebsites.net/api/v1/transferencia";

        // Llamamos al constructor actualizado
        TransferenciaMLSRequest request = new TransferenciaMLSRequest(
                cuentaOrigenDto.getNumeroCuenta(),       // Cuenta de origen
                "BBVA",                                 // Nombre del banco de origen
                cuentaDestinoNumero,                    // Cuenta de destino
                nombreDestino,                          // Nombre del banco de destino
                codigoBancoDestino,                     // Código del banco de destino
                codigoBancoOrigen,                      // Código del banco de origen
                monto,                                  // Importe o monto
                descripcion                             // Glosa o descripción
        );

        // Crear las cabeceras con el token

        laredoToken = tokenService.getTokenApiAzure();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + laredoToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransferenciaMLSRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<TransferenciaMLSResponse> response = restTemplate.postForEntity(url, entity, TransferenciaMLSResponse.class);

            // Verificar si la respuesta contiene el codigoTransaccion
            TransferenciaMLSResponse respuestaMLS = response.getBody();

            if (respuestaMLS == null || respuestaMLS.getCodigoTransaccion() == null) {
                throw new Exception("Error en la transferencia MLS: Falta el código de transacción en la respuesta.");
            }

            System.out.println("Código de Transacción MLS generado: " + respuestaMLS.getCodigoTransaccion());

            // Devolver el código de transacción que se utilizará para verificar el estado
            return respuestaMLS.getCodigoTransaccion();
        } catch (Exception e) {
            System.out.println("Error al iniciar la transferencia en el MLS: " + e.getMessage());
            throw new Exception("Error al iniciar la transferencia en el MLS: " + e.getMessage());
        }
    }

    @Async
    @Transactional
    public void iniciarVerificacionAsincrona(String codigoTransaccionMLS, Long transaccionId) {
        boolean transaccionFinalizada = false;

        // Realizamos reintentos hasta que se obtenga un estado definitivo (COMPLETADA o ERROR)
        while (!transaccionFinalizada) {
            try {
                // Llamar a la API del MLS para obtener el estado de la transacción
                String url = "https://mls-ubp.azurewebsites.net/api/v1/transferencia/" + codigoTransaccionMLS;

                HttpHeaders headers = new HttpHeaders();

                headers.set("Authorization", "Bearer " + laredoToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Creamos la entidad HTTP con los headers
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<EstadoTransaccionDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, EstadoTransaccionDto.class);
                //Respuesta de transaccion
                EstadoTransaccionDto estadoTransaccion = response.getBody();

                // Verificar el estado de la transacción
                if (estadoTransaccion != null && estadoTransaccion.getEstado() != null) {
                    EstadoTransaccion estadoEnum;
                    try {
                        estadoEnum = EstadoTransaccion.valueOf(estadoTransaccion.getEstado());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Estado no válido recibido desde la API: " + estadoTransaccion.getEstado());
                        continue;  // Continuamos si el estado no es válido
                    }

                    // Actualizar el estado de la transacción en la base de datos
                    Optional<Transaccion> transaccionOpt = transaccionRepository.findById(transaccionId);
                    if (transaccionOpt.isPresent()) {
                        Transaccion transaccion = transaccionOpt.get();
                        transaccion.setEstado(estadoEnum); // Actualizamos el estado con el enum convertido
                        transaccion.setIdMls(UUID.fromString(codigoTransaccionMLS));

                        transaccionRepository.save(transaccion);

                        // Si el estado es COMPLETADA o ERROR, finalizamos la verificación
                        if (estadoEnum == EstadoTransaccion.PROCESADO) {
                            transaccionFinalizada = true;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al consultar el estado de la transacción: " + e.getMessage());
            }
        }
    }

//    @Override
//    @Transactional
//    public Transaccion recibirTransferenciaExterna(CuentaResponseDto cuentaOrigenDto, String destinoNumero, String nombreDestino, Long cuentaDestinoNumero, BigDecimal monto, String descripcion, Long codigoBancoOrigen) throws Exception {
//        // Obtener la cuenta destino
//        Optional<Cuenta> cuentaDestinoOpt = cuentaRepository.findByNumeroCuenta(cuentaDestinoNumero);
//        if (cuentaDestinoOpt.isEmpty()) {
//            throw new NotDataFoundException("Cuenta destino no encontrada");
//        }
//        Cuenta cuentaDestino = cuentaDestinoOpt.get();
//
//        // Actualizar saldo de la cuenta destino
//        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(monto));
//        cuentaRepository.save(cuentaDestino);
//
//        // Crear la transacción para reflejar la transferencia recibida
//        Transaccion transaccion = new Transaccion();
//        transaccion.setCuentaOrigen(null); // Como es una transferencia externa, la cuenta origen es desconocida
//        transaccion.setCuentaDestino(cuentaDestino);
//        transaccion.setMonto(monto);
//        transaccion.setDescripcion(descripcion);
//        transaccion.setFecha(LocalDateTime.now());
//
//        // Guardar la transacción
//        return transaccionRepository.save(transaccion);
//    }

    @Override
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public Transaccion save(Transaccion transaccion) {
        return transaccionRepository.save(transaccion);
    }

    @Override
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public Transaccion realizarTransferenciaExternaNv(Cuenta cuentaOrigen, String nombreDestino, String cuentaDestinoNumero, BigDecimal monto, String descripcion, Long codigoBancoOrigen, Long codigoBancoDestino) throws Exception {
        Transaccion transaccion = Transaccion.builder()
                .cuentaOrigen(cuentaOrigen)
                .cuentaExternaDestino(cuentaDestinoNumero)
                .nombreDestino(nombreDestino)
                .monto(monto)
                .descripcion(descripcion)
                .estado(EstadoTransaccion.PENDIENTE)
                .fecha(LocalDateTime.now())
                .build();

        transaccionRepository.save(transaccion);

        //MLS -> ENDPOINT EXTERNO  (TRANSACIONAL)
        try {
            String url = "https://mls-ubp.azurewebsites.net/api/v1/transferencia";

            laredoToken = tokenService.getTokenApiAzure();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + laredoToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            TransferenciaMLSRequest request = new TransferenciaMLSRequest(
                    cuentaOrigen.getNumeroCuenta().toString(),       // Cuenta de origen
                    "BBVA",                                 // Nombre del banco de origen
                    cuentaDestinoNumero.toString(),                    // Cuenta de destino
                    nombreDestino,                          // Nombre del banco de destino
                    codigoBancoDestino,                     // Código del banco de destino
                    codigoBancoOrigen,                      // Código del banco de origen
                    monto,                                  // Importe o monto
                    descripcion                             // Glosa o descripción
            );

            HttpEntity<TransferenciaMLSRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TransferenciaMLSResponse> response = restTemplate.postForEntity(url, entity, TransferenciaMLSResponse.class);

            // Verificar si la respuesta contiene el codigoTransaccion
            TransferenciaMLSResponse respuestaMLS = response.getBody();

            if (respuestaMLS == null || respuestaMLS.getCodigoTransaccion() == null) {
                throw new Exception("Error en la transferencia MLS: Falta el código de transacción en la respuesta.");
            }

            System.out.println("Código de Transacción MLS generado: " + respuestaMLS.getCodigoTransaccion());

            // Devolver el código de transacción que se utilizará para verificar el estado
            String codigoMls = respuestaMLS.getCodigoTransaccion();

            //ASINCRONO
            this.iniciarVerificacionAsincrona(codigoMls, transaccion.getId());

            //Disminuir saldo
            cuentaOrigen.setSaldo( cuentaOrigen.getSaldo().subtract(monto));

            cuentaRepository.save(cuentaOrigen);

            return transaccion;
        } catch (Exception e) {
            System.out.println("Error al iniciar la transferencia en el MLS: " + e.getMessage());
            throw new Exception("Error al iniciar la transferencia en el MLS: " + e);
        }

    }

    private String determinarTipoTransaccion(Long cuentaId, Transaccion transaccion) {
        if (transaccion.getCuentaOrigen().getId().equals(cuentaId)) {
            return "Enviado";
        } else if (transaccion.getCuentaDestino().getId().equals(cuentaId)) {
            return "Recibido";
        } else {
            return "Desconocido";
        }
    }

    @Override
    public void validarSaldoSuficiente(CuentaResponseDto cuenta, BigDecimal monto) {
        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar la transferencia");
        }
    }

    @Override
    public Transaccion recibirTransferenciaExterna(CuentaResponseDto cuentaOrigenDto, String destinoNumero, String nombreDestino, Long cuentaDestinoNumero, BigDecimal monto, String descripcion, Long codigoBancoOrigen) throws Exception {
        return null;
    }


}
