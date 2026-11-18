package com.scheduler.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

	private static final String SECRET = "0123456789abcdef0123456789abcdef";
	private static final String OTHER = "ffffffffffffffffffffffffffffffff";

	private String token(String secret, long ttlMs) {
		return Jwts.builder().subject("a@b.de").claim("permissions", List.of("X", "Y"))
				.expiration(new Date(System.currentTimeMillis() + ttlMs))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
	}

	@Test
	void parsesValidToken() {
		JwtService service = new JwtService(SECRET);
		Claims claims = service.parse(token(SECRET, 60_000));
		assertEquals("a@b.de", claims.getSubject());
		assertEquals(List.of("X", "Y"), service.extractList(claims, "permissions"));
	}

	@Test
	void rejectsWrongSignature() {
		JwtService service = new JwtService(SECRET);
		assertThrows(JwtException.class, () -> service.parse(token(OTHER, 60_000)));
	}

	@Test
	void rejectsExpiredToken() {
		JwtService service = new JwtService(SECRET);
		assertThrows(JwtException.class, () -> service.parse(token(SECRET, -60_000)));
	}

	@Test
	void rejectsShortSecret() {
		assertThrows(IllegalStateException.class, () -> new JwtService("short"));
	}
}
