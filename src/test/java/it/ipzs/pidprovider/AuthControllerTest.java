package it.ipzs.pidprovider;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.ipzs.pidprovider.controller.AuthController;
import it.ipzs.pidprovider.dto.ParResponse;
import it.ipzs.pidprovider.service.ParService;

class AuthControllerTest {

	private AuthController authController;
	private ParService parService;

	@BeforeEach
	public void setUp() {
		parService = mock(ParService.class);
		authController = new AuthController(parService, null, null, null);
	}

	@Test
	void testParRequestSuccess() {
		String request = "some valid request";
		String requestUri = "https://pid-provider.example.org/as/par/1234567890";
		ParResponse parResponse = new ParResponse();
		parResponse.setRequestUri(requestUri);
		when(parService.generateRequestUri(request)).thenReturn(parResponse);

		ResponseEntity<ParResponse> response = authController.parRequest(null, null, null, null, null, null, request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(parResponse, response.getBody());
	}

	@Test
	void testParRequestFailure() {
		String request = "some invalid request";
		when(parService.generateRequestUri(request)).thenThrow(new IllegalArgumentException("Invalid request"));


		assertThatIllegalArgumentException().isThrownBy(() -> // when
		authController.parRequest(null, null, null, null, null, null, request));
	}
}