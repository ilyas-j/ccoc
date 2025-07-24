package com.stage.coc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret:}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:604800000}")
    private int jwtExpirationInMs;

    private SecretKey signingKey;

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            if (jwtSecret != null && !jwtSecret.isEmpty() && jwtSecret.length() >= 64) {
                // Utiliser la clé fournie si elle est assez longue
                signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                System.out.println("🔑 JWT: Utilisation de la clé personnalisée");
            } else {
                // Générer une clé sécurisée automatiquement
                signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
                System.out.println("🔑 JWT: Génération automatique d'une clé sécurisée pour HS512");
                System.out.println("⚠️  ATTENTION: La clé est générée à chaque démarrage. Pour la production, utilisez une clé fixe dans application.properties");
            }
        }
        return signingKey;
    }

    public String generateToken(Authentication authentication) {
        // Gérer les deux cas possibles
        Object principal = authentication.getPrincipal();
        Long userId;

        if (principal instanceof UserPrincipal) {
            userId = ((UserPrincipal) principal).getId();
        } else {
            throw new IllegalStateException("Type de principal non supporté: " + principal.getClass());
        }

        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        String token = Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        System.out.println("🎫 JWT Token généré pour l'utilisateur ID: " + userId);
        return token;
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.parseLong(claims.getSubject());
        System.out.println("🎫 JWT Token décodé pour l'utilisateur ID: " + userId);
        return userId;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            System.err.println("❌ JWT: Signature invalide");
        } catch (MalformedJwtException ex) {
            System.err.println("❌ JWT: Token malformé");
        } catch (ExpiredJwtException ex) {
            System.err.println("❌ JWT: Token expiré");
        } catch (UnsupportedJwtException ex) {
            System.err.println("❌ JWT: Token non supporté");
        } catch (IllegalArgumentException ex) {
            System.err.println("❌ JWT: Claims vides");
        }
        return false;
    }
}