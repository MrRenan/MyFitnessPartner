package io.github.mrrenan.myfitnesspartner.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Endpoints públicos — não precisam de token
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",           // login e register
            "/onboarding/**",     // onboarding conversacional (novo usuário)
            "/health",            // health check
            "/webhook/**",        // Webhook
            "/v3/api-docs/**",    // Swagger
            "/swagger-ui/**",     // Swagger UI
            "/swagger-ui.html",   // Swagger UI
            "/users/**",          // ← temporário para MVP
            "/meals/**",          // ← temporário para MVP
            "/daily-goals/**",    // ← temporário para MVP
            "/ai/**",
            "/actuator/**"        // health, metrics, prometheus (monitoramento)
            // Recursos estáticos (HTML, JS, CSS) são tratados pelo webSecurityCustomizer abaixo
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF — não usamos sessão, usamos JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Habilita CORS com a configuração definida abaixo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Sem sessão — cada requisição é autenticada pelo token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define quais endpoints são públicos e quais precisam de token
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )

                // Adiciona nosso filtro JWT antes do filtro padrão do Spring
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Exclui recursos estáticos do filtro de segurança completamente.
     * Arquivos em /static/ (HTML, JS, CSS) não passam pelo Spring Security.
     */
    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/*.html", "/*.js", "/*.css", "/*.ico", "/");
    }

    /**
     * Configuração de CORS para permitir requisições do frontend.
     * Em desenvolvimento aceita qualquer origem local.
     * Em produção a origem deve ser restrita ao domínio do frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origens permitidas — localhost para dev, domínio do portfólio para prod
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.renanleite.dev.br",
                "https://renanleite.dev.br"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}