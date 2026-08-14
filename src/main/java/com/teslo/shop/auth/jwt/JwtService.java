package com.teslo.shop.auth.jwt;

import com.teslo.shop.auth.entity.User;
import com.teslo.shop.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(JwtProperties properties) {
        // TODO: Op. 1
        // this.key = Jwts.SIG.HS256.key().build();
        // TODO: Op. 2
        // this.key = new SecretKeySpec(
        //     properties.getSecret().getBytes(StandardCharsets.UTF_8),
        //     "HmacSHA256"
        // );
        // TODO: Op. 3
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = properties.getExpiration();
    }

    public String generateToken(User user) {
        return Jwts.builder()
            .claim("id", user.getId().toString())
            .issuedAt(new Date())
            .expiration(
                new Date(System.currentTimeMillis() + expirationSeconds * 1000)
            )
            .signWith(key)
            .compact();
    }

    public String extractUserId(String token) {
        return parse(token).get("id", String.class);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
