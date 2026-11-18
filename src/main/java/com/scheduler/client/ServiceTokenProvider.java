package com.scheduler.client;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Logs in to IdentityService with the technical account (role SERVICE) and caches the access token
 * until shortly before it expires.
 */
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

	private final IdentityAuthClient identityAuthClient;

	@Value("${app.service-account.email:}")
	private String email;
	@Value("${app.service-account.password:}")
	private String password;

	private String token;
	private Instant expiresAt = Instant.EPOCH;

	public synchronized String bearer() {
		if (email.isBlank() || password.isBlank()) {
			throw new IllegalStateException("SERVICE_ACCOUNT_EMAIL / SERVICE_ACCOUNT_PASSWORD are not configured");
		}
		if (token == null || Instant.now().isAfter(expiresAt.minusSeconds(30))) {
			TokenResponse r = identityAuthClient.login(new LoginRequest(email, password));
			token = r.getAccessToken();
			expiresAt = Instant.now().plusSeconds(r.getExpiresInSeconds());
		}
		return "Bearer " + token;
	}
}
