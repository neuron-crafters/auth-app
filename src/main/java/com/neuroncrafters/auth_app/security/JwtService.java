package com.neuroncrafters.auth_app.security;

import com.neuroncrafters.auth_app.entities.Role;
import com.neuroncrafters.auth_app.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;


    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Invalid secret!!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    // generate Token
    public String generateToken(User user) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles() == null ? Collections.emptyList() :
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "type", "access"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles() == null ? Collections.emptyList() :
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "type", "refresh"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // parse the token
    public Jws<Claims> parse(String token) {
        // This will verify
        // 1. Signature validity
        // 2. Check token expiration as well
        // 3. Not before claim
        // 4. Format instruction also checked.
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    // isAccessToken from token
    public boolean isAccessToken(String token) {
        Claims c = parse(token).getPayload();
        return c.get("type").equals("access");
    }

    // isRefreshToken from token
    public boolean isRefreshToken(String token) {
        Claims c = parse(token).getPayload();
        return c.get("type").equals("refresh");
    }

    // getUserId from token
    public UUID getUserId(String token) {
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    // get jti from token
    public String getJti(String token) {
        Claims c = parse(token).getPayload();
        return c.getId();
    }

    // get Roles of user
    public List<String> getRoles(String token) {
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    // get Email from token
    public String getEmail(String token) {
        Claims c = parse(token).getPayload();
        return c.get("email").toString();
    }
}
