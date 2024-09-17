package com.banco.service;

import com.banco.dto.CuentaResponseDto;
import com.banco.entity.Transaccion;

import java.math.BigDecimal;

public interface TransferenciaService {
    public Transaccion realizarTransferenciaExterna(CuentaResponseDto cuentaOrigenDto,
                                                    String cuentaDestinoNumero,
                                                    String nombreDestino, // Nuevo parámetro para el nombre del banco destino
                                                    Long codigoBancoDestino, // Código banco destino
                                                    BigDecimal monto,
                                                    String descripcion, // Glosa o descripción de la transferencia
                                                    Long codigoBancoOrigen) throws Exception ;


    Transaccion realizarTransferenciaExternaNv(Long cuentaOrigen,
                                             String cuentaDestinoNumero,
                                             String nombreDestino, // Nuevo parámetro para el nombre del banco destino
                                             Long codigoBancoDestino, // Código banco destino
                                             BigDecimal monto,
                                             String descripcion, // Glosa o descripción de la transferencia
                                             Long codigoBancoOrigen) throws Exception;
}
