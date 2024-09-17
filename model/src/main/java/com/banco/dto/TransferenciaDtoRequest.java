package com.banco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferenciaDtoRequest {

    @NotNull(message = "La cuenta origen es obligatoria")
    private Long cuentaOrigen;  // Asegúrate que sea de tipo Long, no String

    @NotNull(message = "La cuenta destino es obligatoria")
    private Long cuentaDestino;

    @NotNull(message = "El monto es obligatorio")
    private BigDecimal monto;

    private String descripcion;

    // Getters y Setters
    public Long getCuentaOrigen() {
        return cuentaOrigen;
    }

    public void setCuentaOrigen(Long cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }

    public Long getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(Long cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}