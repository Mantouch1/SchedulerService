package com.scheduler.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "identity-auth", url = "${identity.service.url}")
public interface IdentityAuthClient {

	@PostMapping("/api/auth/login")
	TokenResponse login(@RequestBody LoginRequest request);
}
