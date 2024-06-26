package it.ipzs.pidprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.ipzs.pidprovider.exception.SessionInfoByClientIdNotFoundException;
import it.ipzs.pidprovider.exception.SessionInfoByStateNotFoundException;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.service.AuthorizationService;
import it.ipzs.pidprovider.service.SRService;
import it.ipzs.pidprovider.util.CallbackJwtUtil;
import it.ipzs.pidprovider.util.SessionUtil;

class AuthorizationServiceTest {

	@Mock
	private SRService srService;

	@Mock
	private SessionUtil sessionUtil;

	@Mock
	private CallbackJwtUtil dpJwtUtil;

	private AuthorizationService authorizationService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		authorizationService = new AuthorizationService(srService, sessionUtil, dpJwtUtil);
	}

	@Test
	void testGenerateCode() {
		// Mock SRService
		when(srService.generateRandomByByteLength(32)).thenReturn("mockedCode");

		// Test
		String code = authorizationService.generateCode();

		// Verify
		assertEquals("mockedCode", code);
		verify(srService, times(1)).generateRandomByByteLength(32);
	}

	@Test
	void testRetrieveStateParam_validSessionInfo() {
		// Mock SessionUtil
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setRequestUri("requestUri");
		sessionInfo.setVerified(false);
		sessionInfo.setState("state");

		when(sessionUtil.getSessionInfo("clientId")).thenReturn(sessionInfo);

		// Test
		SessionInfo si = authorizationService.retrieveStateParam("clientId", "requestUri");

		// Verify
		assertEquals("state", si.getState());
		verify(sessionUtil, times(1)).getSessionInfo("clientId");
	}

	@Test
	void testRetrieveStateParam_invalidSessionInfo() {
		// Mock SessionUtil
		when(sessionUtil.getSessionInfo("clientId")).thenReturn(null);

		// Test and Verify
		assertThrows(SessionInfoByClientIdNotFoundException.class,
				() -> authorizationService.retrieveStateParam("clientId", "requestUri"));

		verify(sessionUtil, times(1)).getSessionInfo("clientId");
	}

	@Test
	void testCheckStateParamAndReturnSessionInfo_validSessionInfo() {
		// Mock SessionUtil
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setVerified(false);
		sessionInfo.setCode("code");

		when(sessionUtil.getSessionInfoByState("state")).thenReturn(sessionInfo);

		// Test
		SessionInfo result = authorizationService.checkStateParamAndReturnSessionInfo("state");

		// Verify
		assertThat(result).isNotNull();
		assertEquals(sessionInfo, result);
		assertEquals(true, sessionInfo.isVerified());
		verify(sessionUtil, times(1)).putSessionInfo(sessionInfo);
	}

	@Test
	void testCheckStateParamAndReturnSessionInfo_invalidSessionInfo() {
		// Mock SessionUtil
		when(sessionUtil.getSessionInfoByState("state")).thenReturn(null);

		// Test and Verify
		assertThrows(SessionInfoByStateNotFoundException.class,
				() -> authorizationService.checkStateParamAndReturnSessionInfo("state"));

		verify(sessionUtil, times(0)).putSessionInfo(any(SessionInfo.class));
	}
}

