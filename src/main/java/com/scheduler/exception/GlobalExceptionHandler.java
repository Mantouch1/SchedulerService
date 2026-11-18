package com.scheduler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import feign.FeignException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail notFound(ResourceNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	ProblemDetail conflict(ConflictException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(BadRequestException.class)
	ProblemDetail badRequest(BadRequestException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail validation(MethodArgumentNotValidException ex) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b).orElse("Validation failed");
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MissingServletRequestParameterException.class })
	ProblemDetail unreadable(Exception ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ProblemDetail tooLarge(MaxUploadSizeExceededException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "File too large");
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail forbidden(AccessDeniedException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
	}

	/** A call to another service failed (Feign). */
	@ExceptionHandler(FeignException.class)
	ProblemDetail upstream(FeignException ex) {
		int st = ex.status();
		if (st == 401 || st == 403 || st == 404) {
			return ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(st), "Refused by upstream service (" + st + ")");
		}
		logger.error("Upstream service error (status {})", st, ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Upstream service unavailable");
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail unexpected(Exception ex) {
		logger.error("Unexpected error", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
	}
}
