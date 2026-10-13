package kickstart.welcome;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorTest {//and Login!!!

	@Autowired
	private MockMvc mockMvc; //simuliert HTTP-Anfragen

	@Test
	void testHandleError() {
		//mocks
		ErrorAttributes errorAttributes = mock(ErrorAttributes.class);
		WebRequest webRequest = mock(WebRequest.class);
		Model model = mock(Model.class);

		Map<String, Object> errorDetails = Map.of(
			"timestamp", "2024-12-01T12:00:00",
			"status", 404,
			"error", "Not Found",
			"message", "Resource not found",
			"path", "/nonexistent"
		);

		when(errorAttributes.getErrorAttributes(any(WebRequest.class), any())).thenReturn(errorDetails);


		ErrorController controller = new OurErrorController(errorAttributes);
		String viewName = ((OurErrorController) controller).handleError(webRequest, model);


		assertEquals("error", viewName);

		verify(model).addAttribute("timestamp", "2024-12-01T12:00:00");
		verify(model).addAttribute("status", 404);
		verify(model).addAttribute("error", "Not Found");
		verify(model).addAttribute("message", "Resource not found");
		verify(model).addAttribute("path", "/nonexistent");
	}

	@Test
	void testLoginPageReturnsCorrectView() throws Exception {
		// Simuliert eine GET-Anfrage an /login
		mockMvc.perform(get("/login"))
			.andExpect(status().isOk()) // Erwartet HTTP-Status 200
			.andExpect(view().name("login")); // Erwartet die View "login"
	}

}
