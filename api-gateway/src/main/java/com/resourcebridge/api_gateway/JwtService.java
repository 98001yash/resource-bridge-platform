package com.resourcebridge.api_gateway;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Service
public class JwtService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new RuntimeException("Missing userId claim in JWT");
        }

        return userIdObj.toString();
    }

    public String getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        System.out.println("JWT Claims: " + claims);

        Object rolesObj = claims.get("role");
        if (rolesObj == null) {
            throw new RuntimeException("Missing roles claim in JWT");
        }

        if (rolesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> rolesList = (List<String>) rolesObj;
            return String.join(",", rolesList);
        }

        return rolesObj.toString();
    }

    public boolean isVerified(String token) {

        Claims claims = extractClaims(token);

        Object verified = claims.get("verified");

        if (verified == null) {
            return false;
        }

        return Boolean.parseBoolean(verified.toString());
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}