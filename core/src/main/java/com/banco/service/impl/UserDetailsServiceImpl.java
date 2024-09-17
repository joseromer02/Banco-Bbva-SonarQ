package com.banco.service.impl;

import com.banco.entity.Usuario;
import com.banco.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService{
    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario authUser =  this.usuarioRepository.findByCorreo(correo.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario"));

        return new User(
                authUser.getUsername(),
                authUser.getPassword(),
                authUser.isEnabled(),
                true,
                authUser.isAccountNonExpired(),
                true,
                authUser.getAuthorities()
        );
    }
}
