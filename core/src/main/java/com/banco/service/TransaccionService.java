package com.banco.service;

import com.banco.dto.CuentaResponseDto;
import com.banco.dto.TransaccionDto;
import com.banco.entity.Cuenta;
import com.banco.entity.Transaccion;

import java.math.BigDecimal;
import java.util.List;

public interface TransaccionService {

    // Método para obtener todas las transacciones asociadas a una cuenta (como origen o destino)
    List<TransaccionDto> obtenerTransaccionesPorCuenta(Long cuentaId);

    // Método para realizar una transferencia entre cuentas dentro del mismo banco
    Transaccion realizarTransferencia(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto, String descripcion);

    // Método para realizar una transferencia a cuentas de otros bancos (externas)


    // Validación de saldo suficiente antes de realizar la transferencia
    void validarSaldoSuficiente(CuentaResponseDto cuenta, BigDecimal monto);
    Transaccion recibirTransferenciaExterna(CuentaResponseDto cuentaOrigenDto, String destinoNumero, String nombreDestino, Long cuentaDestinoNumero, BigDecimal monto, String descripcion, Long codigoBancoOrigen) throws Exception;

    Transaccion save(Transaccion transaccion);


    Transaccion realizarTransferenciaExternaNv(Cuenta cuentaOrigen, String nombreDestino, String cuentaDestinoNumero, BigDecimal monto, String descripcion, Long codigoBancoOrigen, Long codigoBancoDestino) throws Exception;

}
