package com.example.distributed_lovable.common_lib.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Component
public class AuthUtil {

    @Value("${jwt.secret-key:default_secret_key_placeholder_change_in_production_environment_32bytes_long}")
    private String jwtSecretKey;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(JwtUserPrincipal user){
        return Jwts.builder()
                .subject(user.username())
                .claim("userId",user.userId().toString())
                .claim("name",user.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*100*60))
                .signWith(getSecretKey())
                .compact();

    }

    public JwtUserPrincipal verifyAccessToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new IllegalArgumentException("JWT token is missing required 'userId' claim");
        }
        Long userId = Long.parseLong(userIdObj.toString());

        String username = claims.getSubject();
        String name = claims.get("name", String.class);

        return new JwtUserPrincipal(userId,name,username,null,new ArrayList<>());
    }

    public Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal userPrincipal)){

            throw new AuthenticationCredentialsNotFoundException("NO jwt found");

        }

        return userPrincipal.userId();
    }
}
