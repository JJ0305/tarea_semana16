package com.tarea.calificada16.request;

import com.tarea.calificada16.client.Client;
import com.tarea.calificada16.client.ClientRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {
	private final ServiceRequestRepository requests;
	private final ClientRepository clients;

	public ServiceRequestController(ServiceRequestRepository requests, ClientRepository clients) {
		this.requests = requests;
		this.clients = clients;
	}

	@GetMapping
	public List<RequestView> list() {
		return requests.findAllByOrderByCreatedAtDesc().stream()
				.map(request -> new RequestView(request.getId(), request.getClient().getId(),
						request.getClient().getName(), request.getSubject(), request.getDescription(),
						request.getStatus(), request.getCreatedAt()))
				.toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RequestView create(@Valid @RequestBody RequestInput input) {
		Client client = clients.findById(input.clientId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
		ServiceRequest saved = requests.save(
				new ServiceRequest(client, input.subject().trim(), input.description().trim()));
		return new RequestView(saved.getId(), client.getId(), client.getName(), saved.getSubject(),
				saved.getDescription(), saved.getStatus(), saved.getCreatedAt());
	}

	public record RequestInput(
			@NotNull Long clientId,
			@NotBlank @Size(max = 180) String subject,
			@NotBlank @Size(max = 2000) String description) {
	}

	public record RequestView(Long id, Long clientId, String clientName, String subject, String description,
			String status, java.time.Instant createdAt) {
	}
}
