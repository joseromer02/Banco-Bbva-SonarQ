package com.banco.service.impl;

import com.banco.dto.UsuarioRequestDto;
import com.banco.dto.UsuarioResponseDto;
import com.banco.entity.Usuario;
import com.banco.entity.Cuenta;
import com.banco.repository.UsuarioRepository;
import com.banco.repository.CuentaRepository;
import com.banco.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDto::new)
                .toList();
    }

    @Override
    public Usuario findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public UsuarioResponseDto saveUsuario(UsuarioRequestDto dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCi(dto.getCi());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasena(this.passwordEncoder.encode(dto.getContrasena()));
        usuarioRepository.save(usuario);

        log.info("Usuario guardado ", usuario);

        // Crear una cuenta bancaria predeterminada para el usuario
        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setUsuario(usuario);
        nuevaCuenta.setNumeroCuenta(generarNumeroCuenta());
        nuevaCuenta.setSaldo(BigDecimal.ZERO);
        nuevaCuenta.setTipoMoneda("Bs.");
        nuevaCuenta.setNombreBanco("BBVA");
        cuentaRepository.save(nuevaCuenta);

        return new UsuarioResponseDto(usuario);
    }

    @Override
    @Transactional
    public boolean changePassword(String email, String newPassword) {
        Optional<Usuario> userOpt = usuarioRepository.findByCorreo(email);
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            user.setContrasena(passwordEncoder.encode(newPassword));
            usuarioRepository.save(user);
            return true;
        }
        return false;
    }

    public Long generarNumeroCuenta() {
        Random random = new Random();
        long numeroCuenta;
        boolean existe;
        do {
            numeroCuenta = 1000000000L + random.nextLong(9000000000L);
            existe = cuentaRepository.existsByNumeroCuenta(numeroCuenta);
        } while (existe);
        return numeroCuenta;
    }

    @Override
    public Optional<Usuario> findByCorreoG(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    @Override
    public void saveUsuarioGoogle(Usuario usuario) {
        usuarioRepository.save(usuario);
        // Crear una cuenta bancaria predeterminada para el usuario
        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setUsuario(usuario);
        nuevaCuenta.setNumeroCuenta(generarNumeroCuenta());
        nuevaCuenta.setSaldo(BigDecimal.ZERO);
        nuevaCuenta.setTipoMoneda("Bs.");
        cuentaRepository.save(nuevaCuenta);
    }

    @Override
    public boolean existsByCi(Long ci) {
        return usuarioRepository.existsByCi(ci);
    }
}
