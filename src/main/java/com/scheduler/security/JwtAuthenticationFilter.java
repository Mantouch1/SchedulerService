package com.scheduler.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER = "Bearer ";
	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER)) {
			try {
				Claims claims = jwtService.parse(header.substring(BEARER.length()));
				List<GrantedAuthority> authorities = new ArrayList<>();
				jwtService.extractList(claims, "roles").forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
				jwtService.extractList(claims, "permissions").forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
				SecurityContextHolder.getContext().setAuthentication(
						new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities));
			} catch (JwtException | IllegalArgumentException e) {
				SecurityContextHolder.clearContext(); // invalid / expired token -> 401 from the entry point
			}
		}
		chain.doFilter(request, response);
	}
}
