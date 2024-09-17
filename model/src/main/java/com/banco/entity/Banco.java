package com.banco.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bancos")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_banco")
    private Long id;

    @Column(name = "codigo")
    private Integer codigo;

    @Column(name = "abreviatura")
    private String abreviatura;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "estado")
    private String estado;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

