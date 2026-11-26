package com.tarea.calificada16.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
	private final ClientRepository clients;

	public ClientController(ClientRepository clients) {
		this.clients = clients;
	}

	@GetMapping
	public List<Client> list() {
		return clients.findAllByOrderByCreatedAtDesc();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Client create(@Valid @RequestBody ClientInput input) {
		return clients.save(new Client(input.name().trim(), input.email().trim().toLowerCase(), input.phone()));
	}

	public record ClientInput(
			@NotBlank @Size(max = 120) String name,
			@NotBlank @Email @Size(max = 180) String email,
			@Size(max = 40) String phone) {
	}
}
