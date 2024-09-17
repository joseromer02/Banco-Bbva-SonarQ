package com.banco.bbva.web;

import com.banco.entity.Cuenta;
import com.banco.entity.Transaccion;
import com.banco.entity.Usuario;
import com.banco.repository.CuentaRepository;
import com.banco.repository.TransaccionRepository;
import com.banco.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class Inicializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.findAll().isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNombre("Usuario");
            usuario.setApellido("Prueba");
            usuario.setCi(1234567L);
            usuario.setCorreo("prueba@gmail.com");
            usuario.setContrasena(this.passwordEncoder.encode("12345678"));
            this.usuarioRepository.save(usuario);
        }

        if (transaccionRepository.findAll().isEmpty()) {
            // Obtener cuentas por ID
            Cuenta cuenta1 = cuentaRepository.findById(1L).orElse(null);
            Cuenta cuenta2 = cuentaRepository.findById(3L).orElse(null);

            // Verificar que ambas cuentas existan
            if (cuenta1 != null && cuenta2 != null) {
                // Crear primera transacción
                Transaccion transaccion1 = new Transaccion();
                transaccion1.setCuentaOrigen(cuenta1);
                transaccion1.setCuentaDestino(cuenta2);
                transaccion1.setMonto(new BigDecimal("526.40"));
                transaccion1.setFecha(LocalDateTime.now());
                transaccion1.setDescripcion("Ingreso de dinero prueba");

                // Crear segunda transacción
                Transaccion transaccion2 = new Transaccion();
                transaccion2.setCuentaOrigen(cuenta2);
                transaccion2.setCuentaDestino(cuenta1);
                transaccion2.setMonto(new BigDecimal("1500.00"));
                transaccion2.setFecha(LocalDateTime.now());
                transaccion2.setDescripcion("Pago de dinero prueba");

                // Guardar ambas transacciones
                transaccionRepository.saveAll(Arrays.asList(transaccion1, transaccion2));
            } else {
                // Manejar el caso donde alguna de las cuentas no existe
                System.out.println("Una de las cuentas no existe. No se pueden crear las transacciones.");
            }
        }
    }
}
