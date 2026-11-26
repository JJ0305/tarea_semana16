package com.tarea.calificada16.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository contextRepository;

	public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextRepository) {
		this.authenticationManager = authenticationManager;
		this.contextRepository = contextRepository;
	}

	@GetMapping("/csrf")
	public CsrfView csrf(CsrfToken csrfToken) {
		return new CsrfView(csrfToken.getToken());
	}

	@PostMapping("/login")
	public UserView login(@Valid @RequestBody LoginInput input, HttpServletRequest request,
			HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(input.username(), input.password()));
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		contextRepository.saveContext(context, request, response);
		return new UserView(authentication.getName());
	}

	@GetMapping("/me")
	public UserView me(Authentication authentication) {
		return new UserView(authentication.getName());
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	public record LoginInput(@NotBlank String username, @NotBlank String password) {
	}

	public record UserView(String username) {
	}

	public record CsrfView(String token) {
	}
}
