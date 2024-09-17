package com.banco.service.impl;

import com.banco.dto.CuentaResponseDto;
import com.banco.entity.Cuenta;
import com.banco.entity.Usuario;
import com.banco.repository.CuentaRepository;
import com.banco.repository.UsuarioRepository;
import com.banco.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CuentaServiceImpl implements CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<CuentaResponseDto> getSaldoByEmail(String email) {
        Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);

        if (usuario != null) {
            System.out.println("Usuario encontrado: " + usuario.getCorreo());

            List<Cuenta> cuentas = cuentaRepository.findByUsuario(usuario);
            cuentas.forEach(cuenta ->
                    System.out.println("Cuenta encontrada: " + cuenta.getNumeroCuenta() + " con saldo: " + cuenta.getSaldo())
            );

            return cuentas.stream()
                    .map(CuentaResponseDto::new)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public CuentaResponseDto crearCuentaParaUsuario(String email) {
        Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);
        if (usuario != null) {
            int cuentaCount = cuentaRepository.countByUsuario(usuario);
            if (cuentaCount >= 3) {
                throw new IllegalArgumentException("El usuario ya tiene el máximo de tres cuentas.");
            }
            Long numeroCuenta = generarNumeroDeCuentaUnico();
            Cuenta nuevaCuenta = new Cuenta();
            nuevaCuenta.setUsuario(usuario);
            nuevaCuenta.setNumeroCuenta(numeroCuenta);
            nuevaCuenta.setSaldo(BigDecimal.ZERO);
            nuevaCuenta.setTipoMoneda("Bs.");
            nuevaCuenta.setNombreBanco("BBVA");
            cuentaRepository.save(nuevaCuenta);
            return new CuentaResponseDto(nuevaCuenta);

        } else {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
    }

    private Long generarNumeroDeCuentaUnico() {
        Random random = new Random();
        Long numeroCuenta;
        boolean existe;
        do {
            numeroCuenta = (long) (random.nextDouble() * Math.pow(10, 10));  // Genera un número de 10 dígitos
            existe = cuentaRepository.existsByNumeroCuenta(numeroCuenta);
        } while (existe);  // Asegurarse de que el número de cuenta sea único
        return numeroCuenta;
    }
}
