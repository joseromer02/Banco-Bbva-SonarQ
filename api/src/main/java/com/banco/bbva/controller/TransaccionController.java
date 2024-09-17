package com.banco.bbva.controller;

import ch.qos.logback.core.util.StringUtil;
import com.banco.dto.CuentaResponseDto;
import com.banco.dto.TransaccionDto;
import com.banco.dto.TransferenciaDtoRequest;
import com.banco.dto.TransferenciaExternaDtoRequest;
import com.banco.entity.Transaccion;
import com.banco.service.CuentaService;
import com.banco.service.TransaccionService;
import com.banco.service.TransferenciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin(origins = {"https://localhost:5173", "http://localhost:8080"}, allowCredentials = "true", methods = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@Slf4j
@RequestMapping("/api/v1/transacciones")
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private CuentaService cuentaService;

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<TransaccionDto>> obtenerTransaccionesPorCuenta(@PathVariable Long cuentaId) {
        List<TransaccionDto> transacciones = transaccionService.obtenerTransaccionesPorCuenta(cuentaId);
        return ResponseEntity.ok(transacciones);
    }

    @PostMapping("/transferencia-interna")
    public ResponseEntity<?> realizarTransferenciaInterna(
            @AuthenticationPrincipal User usuarioAutenticado,
            @RequestBody TransferenciaDtoRequest transferenciaDtoRequest) {

        System.out.println("Usuario autenticado: " + usuarioAutenticado.getUsername());

        // Verificar que la cuenta origen no sea nula
        if (transferenciaDtoRequest.getCuentaOrigen() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La cuenta origen es inválida");
        }

        System.out.println("Cuenta origen solicitada: " + transferenciaDtoRequest.getCuentaOrigen());

        // Obtener las cuentas del usuario autenticado
        List<CuentaResponseDto> cuentas = cuentaService.getSaldoByEmail(usuarioAutenticado.getUsername());
        System.out.println("Cuentas asociadas al usuario: ");
        cuentas.forEach(cuenta -> System.out.println("Cuenta: " + cuenta.getNumeroCuenta() + " con saldo: " + cuenta.getSaldo()));

        // Comparar utilizando Objects.equals para evitar problemas de comparación con objetos Long
//        boolean tieneAcceso = cuentas.stream()
//                .anyMatch(cuenta -> {
//                    System.out.println("Comparando cuenta: " + cuenta.getNumeroCuenta() + " con " + transferenciaDtoRequest.getCuentaOrigen());
//                    return Objects.equals(cuenta.getNumeroCuenta(), transferenciaDtoRequest.getCuentaOrigen());
//                });

        String cuenta = cuentas.stream().filter((c) -> c.equals(transferenciaDtoRequest.getCuentaOrigen())).toString();

        log.info("EXISTE CUENTA CON ACCESO: {}", cuenta);

        if (StringUtil.isNullOrEmpty(cuenta)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a la cuenta origen seleccionada");
        }

        try {
            // Realizar la transferencia
            Transaccion transaccion = transaccionService.realizarTransferencia(
                    transferenciaDtoRequest.getCuentaOrigen(),
                    transferenciaDtoRequest.getCuentaDestino(),
                    transferenciaDtoRequest.getMonto(),
                    transferenciaDtoRequest.getDescripcion()
            );

            TransaccionDto transaccionDto = new TransaccionDto(transaccion);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaccionDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errorde tipo genérico {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

//    @PostMapping("/transferencia-externa")
//    public ResponseEntity<?> realizarTransferenciaExterna(
//            @AuthenticationPrincipal User usuarioAutenticado,
//            @RequestBody TransferenciaExternaDtoRequest transferenciaDtoRequest) {
//
//        // Verificamos si el usuario autenticado tiene acceso a la cuenta origen
//        List<CuentaResponseDto> cuentas = cuentaService.getSaldoByEmail(usuarioAutenticado.getUsername());
//        String cuenta = cuentas.stream().filter((c) -> c.equals(transferenciaDtoRequest.getCuentaOrigen())).toString();
//
//        log.info("EXISTE CUENTA CON ACCESO: {}", cuenta);
//
//        if (StringUtil.isNullOrEmpty(cuenta)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a la cuenta origen seleccionada");
//        }
//
//
//        try {
//            // Código de banco de origen (se puede parametrizar según el caso)
//            Long codigoBancoOrigen = 1000L; // Ejemplo con BBVA como banco de origen (puedes adaptarlo)
//
//            // Realizar la transferencia externa usando los nuevos parámetros GUARDAR
//            Transaccion transaccion = transferenciaService.realizarTransferenciaExterna(
//                    cuentas.get(0), // Cuenta origen (del usuario autenticado)
//                    transferenciaDtoRequest.getCuentaDestino(),
//                    transferenciaDtoRequest.getNombreDestino(), // Nombre del banco destino
//                    transferenciaDtoRequest.getCodigoBancoDestino(), // Código banco destino
//                    transferenciaDtoRequest.getMonto(),
//                    transferenciaDtoRequest.getGlosa(), // Usamos "glosa" como la descripción de la transferencia
//                    codigoBancoOrigen // Código de banco origen
//            );
//
//
//            // Devolver la transacción creada con estado 201 (Created)
//            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
//
//        } catch (Exception e) {
//            // En caso de error, devolver el mensaje correspondiente
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Error al realizar la transferencia externa: " + e.getMessage());
//        }
//    }

    @PostMapping("/transferencia-externa")
    public ResponseEntity<?> realizarTransferenciaExterna(@RequestBody TransferenciaExternaDtoRequest transferenciaDtoRequest,
                                                          @AuthenticationPrincipal User usuarioAutenticado) {
        try{
            Transaccion transaccion = this.transferenciaService.realizarTransferenciaExternaNv(
                    transferenciaDtoRequest.getCuentaOrigen(), transferenciaDtoRequest.getCuentaDestino(), transferenciaDtoRequest.getNombreDestino(),
                    transferenciaDtoRequest.getCodigoBancoDestino(), transferenciaDtoRequest.getMonto(), transferenciaDtoRequest.getGlosa(),
                    transferenciaDtoRequest.getCodigoBancoOrigen()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
        } catch(Exception e) {
            log.error("Excepcion generica: {}", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                 .body("Error al realizar la transferencia externa: " + e.getMessage());
        }
    }

//    @PostMapping("/transferencia-externa")
//    public ResponseEntity<?> realizarTransferenciaExterna(
//            @AuthenticationPrincipal User usuarioAutenticado,
//            @RequestBody TransferenciaExternaDtoRequest transferenciaDtoRequest) {
//
//        try {
//            transferenciaService.realizarTransferenciaExterna(c.getCuentaOrigen(),
//                    transferenciaDtoRequest.getCuentaDestino(), transferenciaDtoRequest.getNombreDestino(),
//                    transferenciaDtoRequest.getCodigoBancoDestino(), transferenciaDtoRequest.getMonto(),
//                    transferenciaDtoRequest.getGlosa(), transferenciaDtoRequest.getCodigoBancoOrigen());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//
//        // Devolver la transacción creada con estado 201 (Created)
//            return null;
//                    //ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
//
//    }

//    @PostMapping("/recibir-transferencia-externa")
//    public ResponseEntity<?> recibirTransferenciaExterna(
//            @RequestBody TransferenciaExternaRequest request) {
//        try {
//            Transaccion transaccion = transaccionService.
//                    recibirTransferenciaExterna(
//                            cuentaOrigenDto, cuentaDestinoNumero, nombreDestino, request.getCuentaDestinoNumero(),
//                    request.getMonto(),
//                    request.getDescripcion(),
//                            codigoBancoOrigen);
//            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al recibir la transferencia: " + e.getMessage());
//        }
//    }
//}

}
