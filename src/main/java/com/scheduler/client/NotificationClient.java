package com.scheduler.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-client", url = "${notification.service.url}")
public interface NotificationClient {

	@PostMapping("/api/notifications")
	void send(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody NotificationRequest request);
}
