package com.tarea.calificada16.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
	private final AppUserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String password;

	public AdminInitializer(AppUserRepository users, PasswordEncoder passwordEncoder,
			@Value("${app.admin.username}") String username, @Value("${app.admin.password}") String password) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (!users.existsByUsername(username)) {
			users.save(new AppUser(username, passwordEncoder.encode(password)));
		}
	}
}
