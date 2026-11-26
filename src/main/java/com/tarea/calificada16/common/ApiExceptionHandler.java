package com.tarea.calificada16.common;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Map<String, String> authenticationFailed(AuthenticationException exception) {
		return Map.of("error", "Usuario o contraseña incorrectos");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> invalidInput(MethodArgumentNotValidException exception) {
		return Map.of("error", "Revisa los datos ingresados");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Map<String, String> duplicateEntry(DataIntegrityViolationException exception) {
		return Map.of("error", "Ya existe un registro con esos datos");
	}
}
