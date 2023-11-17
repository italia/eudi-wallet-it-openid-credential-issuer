package it.ipzs.qeaaissuer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import it.ipzs.qeaaissuer.controller.AuthController;
import it.ipzs.qeaaissuer.service.AuthorizationService;
import it.ipzs.qeaaissuer.service.CredentialService;
import it.ipzs.qeaaissuer.service.ParService;
import it.ipzs.qeaaissuer.service.PidCredentialService;
import it.ipzs.qeaaissuer.service.QeaaIssuerService;
import it.ipzs.qeaaissuer.service.TokenService;

@WebMvcTest(AuthController.class)
class AuthControllerApiTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@MockBean
	private ParService parService;

	@MockBean
	private TokenService tokenService;

	@MockBean
	private CredentialService credentialService;

	@MockBean
	private AuthorizationService authService;

	@MockBean
	private QeaaIssuerService qeaaIssuerService;

	@MockBean
	private PidCredentialService pidService;

	@BeforeEach
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	void testApiShouldReturn400BadRequest() throws Exception {
		
		mockMvc.perform(post("/token").contentType(MediaType.APPLICATION_FORM_URLENCODED.toString()))
				.andExpect(status().isBadRequest()).andDo(print());

		mockMvc.perform(get("/authorize").contentType(MediaType.APPLICATION_FORM_URLENCODED.toString()))
				.andExpect(status().isBadRequest()).andDo(print());

		mockMvc.perform(post("/credential").contentType(MediaType.APPLICATION_FORM_URLENCODED.toString()))
				.andExpect(status().isBadRequest()).andDo(print());
	}
}
