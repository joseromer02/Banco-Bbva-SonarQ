package com.banco.dto;

import lombok.Getter;

@Getter
public class UsuarioRequestDto {
    private String nombre;
    private String apellido;
    private Long ci;
    private String correo;
    private String contrasena;
}
