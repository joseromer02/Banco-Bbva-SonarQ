package com.banco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombre", length = 60, nullable = false)
    private String nombre;

    @Column(name = "apellido", length = 60, nullable = false)
    private String apellido;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "ci", nullable = false, unique = true)
    private Long ci;

    @Column(name = "es_google_auth", nullable = false)
    private boolean googleAuth;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Evita que la colección sea serializada
    private List<Cuenta> cuentas;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));  // Ajustable según roles que quieras añadir
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.contrasena;
    }

    @Override
    public String getUsername() {
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Modifica esto si tienes lógica para cuentas expiradas
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Modifica esto si tienes lógica para cuentas bloqueadas
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Modifica esto si tienes lógica para contraseñas expiradas
    }

    @Override
    public boolean isEnabled() {
        return true; // Modifica esto si tienes lógica para cuentas deshabilitadas
    }
}
