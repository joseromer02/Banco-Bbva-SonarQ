package com.banco.bbva.config;

import com.banco.bbva.controller.AuthController;
import com.banco.dto.AuthenticationDto;
import com.banco.dto.OKAuthDto;
import com.banco.entity.Usuario;
import com.banco.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.io.Serializable;
import java.util.Optional;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration implements WebMvcConfigurer, Serializable {

    @Autowired
    private CorsFilter corsFilter;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private JwtTokenFilter jwtTokenFilter;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UsuarioService usuarioService;

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http, AuthController authController) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .addFilterBefore(corsFilter,  SessionManagementFilter.class)
                .authorizeHttpRequests(
                        authorizationManagerRequestMatcherRegistry ->
                                authorizationManagerRequestMatcherRegistry
                                        .requestMatchers(HttpMethod.POST,"/api/v1/auth/login","/api/v1/usuarios").permitAll()
                                        .requestMatchers("/api/v1/transacciones/transferencia-interna").hasRole("USER")
                                        .requestMatchers("/swagger-ui/**","/v3/api-docs/**","/api-docs/**","/swagger-ui.html","/swagger-ui/index.html").permitAll()
                                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            Object principal = authentication.getPrincipal();
                            String email = null;
                            String fullName = null;

                            if (principal instanceof OidcUser) {
                                OidcUser oidcUser = (OidcUser) principal;
                                email = oidcUser.getEmail();
                                fullName = oidcUser.getFullName();
                            } else if (principal instanceof DefaultOAuth2User) {
                                DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
                                email = (String) oauth2User.getAttributes().get("email");
                                fullName = (String) oauth2User.getAttributes().get("name");
                            }

                            if (email != null) {
                                Optional<Usuario> existingUserOpt = usuarioService.findByCorreoG(email);
                                String defaultPassword = null;

                                if (existingUserOpt.isEmpty()) {
                                    // Crear nuevo usuario
                                    String[] nameParts = fullName != null ? fullName.split(" ") : new String[0];
                                    String firstName = nameParts.length > 0 ? nameParts[0] : "";
                                    String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

                                    Usuario newUser = new Usuario();
                                    newUser.setCorreo(email);
                                    newUser.setNombre(firstName);
                                    newUser.setApellido(lastName);
                                    newUser.setCi(generateRandomCi());
                                    newUser.setGoogleAuth(true);
                                    defaultPassword = "g00gl3p44s";
                                    newUser.setContrasena(passwordEncoder.encode(defaultPassword));

                                    usuarioService.saveUsuarioGoogle(newUser);

                                    // Autenticar al usuario con sus nuevas credenciales
                                    AuthenticationDto authData = new AuthenticationDto();
                                    authData.setCorreo(email);
                                    authData.setContrasena(defaultPassword);

                                    // Realizar la autenticación manualmente
                                    OKAuthDto dto = authController.signin(authData).getBody();


                                    // Redirigir al frontend con el token en la URL
                                    String redirectUrl = "https://localhost:5173/?token=" + dto.getToken();
                                    response.sendRedirect(redirectUrl);
                                } else {
                                    if (existingUserOpt.get().isGoogleAuth()) {
                                        // Autenticar al usuario con sus nuevas credenciales
                                        defaultPassword = "g00gl3p44s";

                                        AuthenticationDto authData = new AuthenticationDto();
                                        authData.setCorreo(email);
                                        authData.setContrasena(defaultPassword);

                                        // Realizar la autenticación manualmente
                                        OKAuthDto dto = authController.signin(authData).getBody();

                                        // Redirigir al frontend con el token en la URL
                                        String redirectUrl = "https://localhost:5173/?token=" + dto.getToken();
                                        response.sendRedirect(redirectUrl);

                                    } else {
                                        String redirectUrl = "https://localhost:5173/login?error=El%20usuario%20ya%20existe%20sin%20Google%20Auth";
                                        response.sendRedirect(redirectUrl);
                                    }
                                }
                            } else {
                                response.sendRedirect("https://localhost:5173/login?error=Email%20no%20encontrado");
                            }
                        })
                )
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .cors((cors) -> cors.configurationSource(apiConfigurationSource()));
        ;
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public CustomOidcUserService customOidcUserService() {
//        return new CustomOidcUserService(usuarioService, passwordEncoder);
//    }

    private Long generateRandomCi() {
        Long ci;
        do {
            ci = (long)(Math.random() * 1_000_000_0000L); // Genera un número aleatorio de exactamente 10 dígitos
        } while (usuarioService.existsByCi(ci)); // Verifica que el CI no exista en la base de datos
        return ci;
    }

    private CorsConfigurationSource apiConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
