package com.banco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_cuenta", nullable = false, unique = true)
    private Long numeroCuenta;

    @Column(name = "saldo", nullable = false)
    private BigDecimal saldo;

    @Column(name = "tipo_moneda", nullable = false)
    private String tipoMoneda;

    @Column(name = "nombre_banco", nullable = false)
    private String nombreBanco; // Nuevo campo para almacenar el nombre del banco

    @OneToMany(mappedBy = "cuentaOrigen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Evita que la colección sea serializada
    private List<Transaccion> transaccionesOrigen;

    @OneToMany(mappedBy = "cuentaDestino", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Evita que la colección sea serializada
    private List<Transaccion> transaccionesDestino;
}
