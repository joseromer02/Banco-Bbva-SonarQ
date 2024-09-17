package com.banco.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferenciaMLSResponse {

    private String codigoTransaccion;
    private UUID idMls;  // Aquí agregamos el campo UUID
    private String estado;
    private String mensaje;

    // Getters y Setters
    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public void setCodigoTransaccion(String codigoTransaccion) {
        this.codigoTransaccion = codigoTransaccion;
    }

    public UUID getIdMls() {
        return idMls;
    }

    public void setIdMls(UUID idMls) {
        this.idMls = idMls;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

