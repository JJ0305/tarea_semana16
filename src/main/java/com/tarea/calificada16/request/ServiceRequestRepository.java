package com.tarea.calificada16.request;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
	@EntityGraph(attributePaths = "client")
	List<ServiceRequest> findAllByOrderByCreatedAtDesc();
}
