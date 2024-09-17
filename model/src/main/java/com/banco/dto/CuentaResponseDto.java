package com.banco.dto;

import com.banco.entity.Cuenta;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CuentaResponseDto implements Serializable {
    private String idCuenta;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String tipoMoneda;
    private String nombreBanco; // Nuevo campo para el nombre del banco

    public CuentaResponseDto(Cuenta cuenta) {
        this.idCuenta = String.valueOf(cuenta.getId());
        this.numeroCuenta = String.valueOf(cuenta.getNumeroCuenta());
        this.saldo = cuenta.getSaldo();
        this.tipoMoneda = cuenta.getTipoMoneda();
        this.nombreBanco = cuenta.getNombreBanco(); // Obtener el nombre del banco desde la entidad Cuenta
    }
}
