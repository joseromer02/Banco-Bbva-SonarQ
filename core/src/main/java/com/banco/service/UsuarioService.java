package com.banco.service;

import com.banco.dto.UsuarioRequestDto;
import com.banco.dto.UsuarioResponseDto;
import com.banco.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<UsuarioResponseDto> listarUsuarios();

    Usuario findByCorreo(String correo);

    UsuarioResponseDto saveUsuario(UsuarioRequestDto dto);

    boolean changePassword(String email, String newPassword);

    Optional<Usuario> findByCorreoG(String correo);

    void saveUsuarioGoogle(Usuario usuario);

    boolean existsByCi(Long ci);
}
