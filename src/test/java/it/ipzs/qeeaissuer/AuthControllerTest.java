package it.ipzs.qeeaissuer;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.ipzs.qeeaissuer.controller.AuthController;
import it.ipzs.qeeaissuer.dto.Cnf;
import it.ipzs.qeeaissuer.dto.ParResponse;
import it.ipzs.qeeaissuer.service.ParService;

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
		String requestUri = "https://pid-provider.example.org/as/par/1234567890";
		ParResponse parResponse = new ParResponse();
		parResponse.setRequestUri(requestUri);
		Cnf cnf = mock(Cnf.class);
		when(parService.validateClientAssertionAndRetrieveCnf("some valid client assertion")).thenReturn(cnf);
		when(parService.generateRequestUri(request, cnf)).thenReturn(parResponse);

		ResponseEntity<ParResponse> response = authController.parRequest(null, null, null, null, null,
				"some valid client assertion", request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(parResponse, response.getBody());
	}

	@Test
	void testParRequestFailure() {
		String request = "some invalid request";
		Cnf cnf = mock(Cnf.class);
		when(parService.validateClientAssertionAndRetrieveCnf("some valid client assertion")).thenReturn(cnf);
		when(parService.generateRequestUri(request, cnf)).thenThrow(new IllegalArgumentException("Invalid request"));


		assertThatIllegalArgumentException().isThrownBy(() -> // when
		authController.parRequest(null, null, null, null, null, "some valid client assertion", request));
	}
}