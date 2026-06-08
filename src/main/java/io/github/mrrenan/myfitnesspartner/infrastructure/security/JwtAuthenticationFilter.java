package io.github.mrrenan.myfitnesspartner.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Pega o header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Se não tem token, passa para o próximo filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (remove "Bearer ")
        String token = authHeader.substring(7);

        try {
            // 4. Valida o token
            if (jwtService.isTokenValid(token)) {

                // 5. Extrai o usuário do token
                String whatsappNumber = jwtService.extractWhatsappNumber(token);

                // 6. Cria a autenticação e coloca no contexto do Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                whatsappNumber,
                                null,
                                List.of() // sem roles por enquanto
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuário autenticado: {}", whatsappNumber);
            }
        } catch (Exception e) {
            log.warn("Erro ao processar token JWT: {}", e.getMessage());
        }

        // 7. Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}