package com.banco.service.impl;

import com.banco.dto.CuentaResponseDto;
import com.banco.dto.TransaccionDto;
import com.banco.entity.Cuenta;
import com.banco.entity.EstadoTransaccion;
import com.banco.entity.Transaccion;
import com.banco.exception.NotDataFoundException;
import com.banco.repository.CuentaRepository;
import com.banco.repository.TransaccionRepository;
import com.banco.service.TransaccionService;
import com.banco.service.TransferenciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service


public class TransferenciaServiceImpl implements TransferenciaService {

    @Autowired
    private CuentaRepository cuentaRepository;
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private TransaccionServiceImpl transaccionServiceImpl;

    @Autowired
    private  TransaccionService transaccionService;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaccion realizarTransferenciaExterna(CuentaResponseDto cuentaOrigenDto,
                                                    String cuentaDestinoNumero,
                                                    String nombreDestino,
                                                    Long codigoBancoDestino,
                                                    BigDecimal monto,
                                                    String descripcion,
                                                    Long codigoBancoOrigen) throws Exception {
        // Aquí guardamos la transacción como PENDIENTE
        Optional<Cuenta> cuentaOrigenOpt = cuentaRepository.findByNumeroCuenta(Long.valueOf(cuentaOrigenDto.getNumeroCuenta()));
        if (cuentaOrigenOpt.isEmpty()) {
            throw new NotDataFoundException("Cuenta origen no encontrada.");
        }

        Cuenta cuentaOrigen = cuentaOrigenOpt.get();
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(null);  // No conocemos la cuenta destino en este punto
        transaccion.setMonto(monto);
        transaccion.setDescripcion(descripcion);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setEstado(EstadoTransaccion.PENDIENTE); // Estado inicial

        // Guardar la transacción como pendiente
        transaccion = transaccionService.save(transaccion);

        // Llamar a la API del MLS para iniciar la transferencia
        String codigoTransaccionMLS = String.valueOf(transaccionServiceImpl.iniciarTransferenciaExterna(cuentaOrigenDto,cuentaDestinoNumero,nombreDestino,codigoBancoDestino,monto,descripcion,codigoBancoOrigen));

        // Iniciar verificación asincrónica del estado de la transacción
       transaccionServiceImpl.iniciarVerificacionAsincrona(codigoTransaccionMLS, transaccion.getId());

        return transaccion;
    }

    @Override
    public Transaccion realizarTransferenciaExternaNv(Long cuentaOrigen, String cuentaDestinoNumero, String nombreDestino, Long codigoBancoDestino, BigDecimal monto, String descripcion, Long codigoBancoOrigen) throws Exception {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(cuentaOrigen).orElseThrow(
                () -> new NoSuchElementException("No fue posible recuperar la informacion correspondiente a la cuenta")
        );

        if(cuenta.getSaldo().compareTo(monto) != 1 ) {
            throw new IllegalArgumentException("No cuenta con saldo suficiente");
        }



        return transaccionService.realizarTransferenciaExternaNv(cuenta, nombreDestino, cuentaDestinoNumero, monto, descripcion, codigoBancoOrigen, codigoBancoDestino);

    }


}
