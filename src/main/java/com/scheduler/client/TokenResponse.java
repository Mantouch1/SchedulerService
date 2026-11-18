package com.scheduler.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenResponse {
	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private long expiresInSeconds;
}
