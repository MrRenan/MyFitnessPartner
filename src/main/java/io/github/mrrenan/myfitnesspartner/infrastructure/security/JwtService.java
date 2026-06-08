package io.github.mrrenan.myfitnesspartner.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            RedisTemplate<String, String> redisTemplate) {
        this.secretKey = hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Gera um JWT para o usuário
     */
    public String generateToken(String whatsappNumber) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(whatsappNumber)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrai o whatsappNumber do token
     */
    public String extractWhatsappNumber(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Verifica se o token é válido e não está na blacklist
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean notExpired = claims.getExpiration().after(new Date());
            boolean notBlacklisted = !isTokenBlacklisted(token);
            return notExpired && notBlacklisted;
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Invalida o token adicionando na blacklist do Redis
     */
    public void invalidateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token,
                        "blacklisted",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
                log.info("Token adicionado à blacklist por {}ms", remainingTime);
            }
        } catch (Exception e) {
            log.warn("Erro ao invalidar token: {}", e.getMessage());
        }
    }

    /**
     * Verifica se o token está na blacklist do Redis
     */
    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token)
        );
    }

    /**
     * Extrai os claims do token
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}