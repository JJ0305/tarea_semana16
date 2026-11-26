package com.tarea.calificada16.request;

import com.tarea.calificada16.client.Client;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@Column(nullable = false, length = 180)
	private String subject;

	@Column(nullable = false, length = 2000)
	private String description;

	@Column(nullable = false, length = 30)
	private String status = "PENDIENTE";

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected ServiceRequest() {
	}

	public ServiceRequest(Client client, String subject, String description) {
		this.client = client;
		this.subject = subject;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public Client getClient() {
		return client;
	}

	public String getSubject() {
		return subject;
	}

	public String getDescription() {
		return description;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
