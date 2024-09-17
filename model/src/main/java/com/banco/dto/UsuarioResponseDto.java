package com.banco.dto;

import com.banco.entity.Usuario;
import lombok.Data;

import java.io.Serializable;

@Data
public class UsuarioResponseDto implements Serializable {
    private long id;
    private String nombre;
    private String apellido;
    private Long ci;
    private String correo;

    public UsuarioResponseDto(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.ci = usuario.getCi();
        this.correo = usuario.getCorreo();
    }
}
