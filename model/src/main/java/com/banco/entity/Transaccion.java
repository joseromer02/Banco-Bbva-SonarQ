package com.banco.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "transacciones")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_origen", nullable = false)
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_destino")
    private Cuenta cuentaDestino;

    @Basic
    @Column(name = "CUENTA_EXTERNA_DESTINO")
    private String cuentaExternaDestino;

    @Basic
    @Column(name = "NOMBRE_DESTINO")
    private String nombreDestino;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "descripcion")
    private String descripcion;

    private UUID idMls;

    // Añadir el campo "estado"
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE; // Valor por defecto

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Cuenta getCuentaOrigen() {
//        return cuentaOrigen;
//    }
//
//    public void setCuentaOrigen(Cuenta cuentaOrigen) {
//        this.cuentaOrigen = cuentaOrigen;
//    }
//
//    public Cuenta getCuentaDestino() {
//        return cuentaDestino;
//    }
//
//    public void setCuentaDestino(Cuenta cuentaDestino) {
//        this.cuentaDestino = cuentaDestino;
//    }
//
//    public LocalDateTime getFecha() {
//        return fecha;
//    }
//
//    public void setFecha(LocalDateTime fecha) {
//        this.fecha = fecha;
//    }
//
//    public BigDecimal getMonto() {
//        return monto;
//    }
//
//    public void setMonto(BigDecimal monto) {
//        this.monto = monto;
//    }
//
//    public String getDescripcion() {
//        return descripcion;
//    }
//
//    public void setDescripcion(String descripcion) {
//        this.descripcion = descripcion;
//    }
//
//    public EstadoTransaccion getEstado() {
//        return estado;
//    }
//
//    public void setEstado(EstadoTransaccion estado) {
//        this.estado = estado;
//    }
//
//    public UUID getIdMls() {
//        return idMls;
//    }
//
//    public void setIdMls(UUID idMls) {
//        this.idMls = idMls;
//    }
}
