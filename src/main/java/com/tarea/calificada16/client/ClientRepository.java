package com.tarea.calificada16.client;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
	List<Client> findAllByOrderByCreatedAtDesc();
}
