package it.ipzs.qeaaissuer;

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

import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.service.AuthorizationService;
import it.ipzs.qeaaissuer.service.SRService;
import it.ipzs.qeaaissuer.util.SessionUtil;

class AuthorizationServiceTest {

	@Mock
	private SRService srService;

	@Mock
	private SessionUtil sessionUtil;

	private AuthorizationService authorizationService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		authorizationService = new AuthorizationService(srService, sessionUtil);
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
		String state = authorizationService.retrieveStateParam("clientId", "requestUri");

		// Verify
		assertEquals("state", state);
		verify(sessionUtil, times(1)).getSessionInfo("clientId");
	}

	@Test
	void testRetrieveStateParam_invalidSessionInfo() {
		// Mock SessionUtil
		when(sessionUtil.getSessionInfo("clientId")).thenReturn(null);

		// Test and Verify
		assertThrows(IllegalArgumentException.class,
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
		assertThrows(IllegalArgumentException.class,
				() -> authorizationService.checkStateParamAndReturnSessionInfo("state"));

		verify(sessionUtil, times(0)).putSessionInfo(any(SessionInfo.class));
	}
}

