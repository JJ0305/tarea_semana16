package com.tarea.calificada16;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockCookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Calificada16ApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void apiRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/clients"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginCreatesAuthenticatedSessionUsingCsrfToken() throws Exception {
		MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andReturn();
		String token = csrfResult.getResponse().getCookie("XSRF-TOKEN").getValue();
		MockCookie csrfCookie = new MockCookie("XSRF-TOKEN", token);

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
				.cookie(csrfCookie)
				.header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"test-admin","password":"test-password"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("test-admin"))
				.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("test-admin"));

		MvcResult clientResult = mockMvc.perform(post("/api/clients")
				.session(session)
				.cookie(csrfCookie)
				.header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Cliente de prueba","email":"cliente@example.com","phone":"555-123"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Cliente de prueba"))
				.andReturn();
		String clientId = clientResult.getResponse().getContentAsString()
				.replaceAll(".*\\\"id\\\":(\\d+).*", "$1");

		mockMvc.perform(post("/api/requests")
				.session(session)
				.cookie(csrfCookie)
				.header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"clientId":%s,"subject":"Consulta","description":"Solicitud de prueba"}
						""".formatted(clientId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.clientName").value("Cliente de prueba"));

		mockMvc.perform(get("/api/requests").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].subject").value("Consulta"));
	}
}
