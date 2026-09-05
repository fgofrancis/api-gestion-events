package com.gestion.eventos.api.security.config;

import com.gestion.eventos.api.security.jwt.JwtAuthEntryPoint;
import com.gestion.eventos.api.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // 👈 habilita CORS
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthEntryPoint) //401
                                .accessDeniedHandler((request, response, accessDeniedException)->{
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\":\"Acceso denegado\",\"message\":\"No tiene permiso para realizar esta accion\"}");
                                })
                        )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        )
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 👈 permite preflight
                            .requestMatchers("/api/v1/auth/**").permitAll();

                    if(environment.acceptsProfiles(Profiles.of("dev"))){

                            auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                                           "/v3/api-docs/**", "/v3/api-docs.yaml",
                                                            "/swagger-resources/**",
                                                            "/webjars/**"
                            ).permitAll();
                    }
                    auth.anyRequest().authenticated();
                });
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager
            (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /* Resolviendo el problema de CORS - Cross-Origin Resource Sharing o Intercambio de Recursos de Origen Cruzado */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:3000",
                "https://voluble-fenglisu-3a6b4d.netlify.app/"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With" // 👈 agrega esta
        ));
        /* Para manejar los tokens enviados desde el backend al frontend */
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        /* Usada para manejer cookies*/
        configuration.setAllowCredentials(true);
        /*3600L = a 3,600 seg == 1 hora Guarda una respuesta durante una hora que recibió el servidor. ej:
        * El frontend pregunta al backend puedo hacer un POST el backend answers yes, ahora el frontend necesita hacer otro POST ya no tiene
        * que pregunetar al backend. Esto agiliza todo */
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

















}
