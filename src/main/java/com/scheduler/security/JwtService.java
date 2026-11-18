package com.scheduler.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Validates the access tokens issued by IdentityService (same shared HS256 secret). */
@Service
public class JwtService {

	private final SecretKey key;

	public JwtService(@Value("${app.jwt.secret}") String secret) {
		if (secret.length() < 32) {
			throw new IllegalStateException("app.jwt.secret (JWT_SECRET) must be at least 32 characters");
		}
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/** Returns the claims if signature and expiry are valid, otherwise throws JwtException. */
	public Claims parse(String token) throws JwtException {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public List<String> extractList(Claims claims, String name) {
		Object value = claims.get(name);
		if (value instanceof Collection<?> c) {
			return c.stream().map(String::valueOf).toList();
		}
		return List.of();
	}
}
