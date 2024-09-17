package com.banco.dto;

import java.math.BigDecimal;

public class TransferenciaMLSRequest {

    private String cuentaOrigen;
    private String nombreOrigen;
    private String cuentaDestino;
    private String nombreDestino;
    private Long codigoBancoDestino;
    private Long codigoBancoOrigen;
    private BigDecimal importe;
    private String glosa;

    // Constructor actualizado
    public TransferenciaMLSRequest(String cuentaOrigen, String nombreOrigen, String cuentaDestino, String nombreDestino, Long codigoBancoDestino, Long codigoBancoOrigen, BigDecimal importe, String glosa) {
        this.cuentaOrigen = cuentaOrigen;
        this.nombreOrigen = nombreOrigen;
        this.cuentaDestino = cuentaDestino;
        this.nombreDestino = nombreDestino;
        this.codigoBancoDestino = codigoBancoDestino;
        this.codigoBancoOrigen = codigoBancoOrigen;
        this.importe = importe;
        this.glosa = glosa;
    }

    // Getters y Setters
    public String getCuentaOrigen() {
        return cuentaOrigen;
    }

    public void setCuentaOrigen(String cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }

    public String getNombreOrigen() {
        return nombreOrigen;
    }

    public void setNombreOrigen(String nombreOrigen) {
        this.nombreOrigen = nombreOrigen;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(String cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }

    public String getNombreDestino() {
        return nombreDestino;
    }

    public void setNombreDestino(String nombreDestino) {
        this.nombreDestino = nombreDestino;
    }

    public Long getCodigoBancoDestino() {
        return codigoBancoDestino;
    }

    public void setCodigoBancoDestino(Long codigoBancoDestino) {
        this.codigoBancoDestino = codigoBancoDestino;
    }

    public Long getCodigoBancoOrigen() {
        return codigoBancoOrigen;
    }

    public void setCodigoBancoOrigen(Long codigoBancoOrigen) {
        this.codigoBancoOrigen = codigoBancoOrigen;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }
}
