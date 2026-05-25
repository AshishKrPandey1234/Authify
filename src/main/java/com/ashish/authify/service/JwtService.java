package com.ashish.authify.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Generate a secure signing key for HS256 algorithm
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 30 minutes in milliseconds = 30 * 60 * 1000
    private static final long EXPIRATION_TIME = 1800000;

    // 1. Generate Token using User's Email as the Subject
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 2. Extract Username/Email from Token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Extract Expiration Date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY) // Modern replacement for setSigningKey
                .build()
                .parseSignedClaims(token) // Modern replacement for parseClaimsJws
                .getPayload(); // Modern replacement for getBody
    }

    // 4. Check if Token is Expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 5. Validate Token against User Details
    public Boolean validateToken(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail) && !isTokenExpired(token));
    }
}