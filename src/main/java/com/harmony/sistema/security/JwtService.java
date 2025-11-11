package com.harmony.sistema.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // 1. Llama al método generateToken con un mapa de claims vacío para generar un token sin claims adicionales.
    public String generateToken(UserDetails userDetails) {
        System.out.println(" [JWT SERVICE] Generando token para usuario sin claims extras.");
        return generateToken(new HashMap<>(), userDetails);
    }

    // Genera un token JWT incluyendo claims adicionales, el nombre de usuario, fecha de emisión y expiración, y lo firma.
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        System.out.println(" [JWT SERVICE] Construyendo token JWT con claims.");
        // 1. Construye el JWT, estableciendo los claims, el subject, la fecha de emisión y expiración, y la firma.
        String token = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
        System.out.println(" [JWT SERVICE SUCCESS] Token JWT generado exitosamente.");
        return token;
    }

    // 1. Extrae el subject (nombre de usuario) del token utilizando la función de claims genérica.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Verifica si un token es válido comparando el nombre de usuario con UserDetails y comprobando la expiración.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        // 1. Extrae el nombre de usuario del token.
        final String username = extractUsername(token);
        // 2. Compara el nombre de usuario y verifica que el token no haya expirado.
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    // Método genérico para extraer un claim específico del token.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // 1. Extrae todos los claims.
        final Claims claims = extractAllClaims(token);
        // 2. Aplica la función claimsResolver para obtener el claim deseado.
        return claimsResolver.apply(claims);
    }

    // Parsea y extrae todos los claims (cuerpo) del token, validando la firma.
    private Claims extractAllClaims(String token) {
        // 1. Configura el parser con la clave de firma, lo construye y extrae el cuerpo del JWS (Claims).
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // 1. Extrae la fecha de expiración del token utilizando la función de claims genérica.
    private Date extractExpiration(String token) {
        // Log eliminado: Se ejecuta en cada solicitud, causando logs repetitivos.
        return extractClaim(token, Claims::getExpiration);
    }

    // Comprueba si la fecha de expiración del token es anterior a la fecha actual.
    private boolean isTokenExpired(String token) {
        // 1. Obtiene la fecha de expiración y la compara con la fecha actual.
        return extractExpiration(token).before(new Date());
    }

    // Genera la clave de firma a partir de la llave secreta codificada en Base64.
    private Key getSignInKey() {
        // 1. Decodifica la llave secreta de Base64.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // 2. Genera la clave HMAC SHA para la firma.
        return Keys.hmacShaKeyFor(keyBytes);
    }
}