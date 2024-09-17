package com.banco.dto;

import com.banco.entity.Transaccion;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransaccionDto {
    private Long id;
    private Long cuentaDestino;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fecha;

    // Constructor que convierte una entidad Transaccion en un DTO
    public TransaccionDto(Transaccion transaccion) {
        this.id = transaccion.getId();
        this.cuentaDestino = transaccion.getCuentaDestino().getNumeroCuenta();
        this.monto = transaccion.getMonto();
        this.descripcion = transaccion.getDescripcion();
        this.fecha = transaccion.getFecha();
    }

    public TransaccionDto(LocalDateTime fecha, String descripcion, BigDecimal monto, String determinarTipoTransaccion) {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}

