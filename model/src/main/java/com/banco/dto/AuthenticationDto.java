package com.banco.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationDto {
    private String correo;
    private String contrasena;
}
