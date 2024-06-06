package it.ipzs.qeaaissuer;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.ipzs.qeaaissuer.controller.AuthController;
import it.ipzs.qeaaissuer.dto.Cnf;
import it.ipzs.qeaaissuer.dto.ParResponse;
import it.ipzs.qeaaissuer.service.ParService;

class AuthControllerTest {

	private AuthController authController;
	private ParService parService;

	@BeforeEach
	public void setUp() {
		parService = mock(ParService.class);
		authController = new AuthController(parService, null, null, null, null);
	}

	@Test
	void testParRequestSuccess() {
		String request = "some valid request";
		String requestUri = "https://wallet-rp.example.org/as/par/1234567890";
		ParResponse parResponse = new ParResponse();
		parResponse.setRequestUri(requestUri);
		Cnf cnf = mock(Cnf.class);
		when(parService.validateClientAssertionAndRetrieveCnf("some valid client assertion")).thenReturn(cnf);
		when(parService.generateRequestUri(anyString(), any(), anyString())).thenReturn(parResponse);

		ResponseEntity<ParResponse> response = authController.parRequest("test", "test", "test", "test", "test",
				"some valid client assertion", request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(parResponse, response.getBody());
	}

	@Test
	void testParRequestFailure() {
		String request = "some invalid request";
		Cnf cnf = mock(Cnf.class);
		when(parService.validateClientAssertionAndRetrieveCnf("some valid client assertion")).thenReturn(cnf);
		when(parService.generateRequestUri(anyString(), any(), anyString()))
				.thenThrow(new IllegalArgumentException("Invalid request"));


		assertThatIllegalArgumentException().isThrownBy(() -> // when
		authController.parRequest("test", "test", "test", "test", "test", "some valid client assertion", request));
	}
}