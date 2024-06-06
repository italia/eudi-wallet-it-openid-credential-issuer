package it.ipzs.pidprovider.controller;

import java.net.URI;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import it.ipzs.pidprovider.dto.CredentialResponse;
import it.ipzs.pidprovider.dto.ParResponse;
import it.ipzs.pidprovider.dto.ProofRequest;
import it.ipzs.pidprovider.dto.TokenResponse;
import it.ipzs.pidprovider.exception.MalformedCredentialProofException;
import it.ipzs.pidprovider.exception.MissingOrBlankParamException;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.service.AuthorizationService;
import it.ipzs.pidprovider.service.CredentialService;
import it.ipzs.pidprovider.service.ParService;
import it.ipzs.pidprovider.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.FormParam;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AuthController {

	private final ParService parService;
	private final TokenService tokenService;
	private final CredentialService credentialService;
	private final AuthorizationService authService;

	@Value("${auth-controller.redirect-url}")
	private String redirectUrl;

	@Value("${auth-controller.issuer-url}")
	private String issuer;

	public AuthController(ParService parService, TokenService tokenService, CredentialService credentialService,
			AuthorizationService authService) {
		this.parService = parService;
		this.tokenService = tokenService;
		this.credentialService = credentialService;
		this.authService = authService;
	}

	@PostMapping(path = "/as/par", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ParResponse> parRequest(@FormParam("response_type") String response_type,
			@FormParam("client_id") String client_id, @FormParam("code_challenge") String code_challenge,
			@FormParam("code_challenge_method") String code_challenge_method,
			@FormParam("client_assertion_type") String client_assertion_type,
			@FormParam("client_assertion") String client_assertion, @FormParam("request") String request) {

		log.trace("/as/par params: response_type {} - code_challenge_method {} - client_assertion_type {}",
				response_type, code_challenge_method, client_assertion_type);
		log.trace("-> client_id {}", client_id);
		log.trace("-> code_challenge {}", code_challenge);
		log.trace("-> client_assertion {}", client_assertion);
		log.trace("-> request {}", request);
		log.info("Par request received: client_id {}", client_id);

		validateParams(response_type, client_id, code_challenge, code_challenge_method, client_assertion_type,
				client_assertion, request);
		Object cnf = parService.validateClientAssertionAndRetrieveCnf(client_assertion);
		ParResponse response = parService.generateRequestUri(request, cnf);
		log.trace("par response: {}", response);
		log.info("Pushed Authorization Request accepted: {}", response);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/authorize")
	public ResponseEntity<?> authorize(@RequestParam("client_id") String client_id,
			@RequestParam("request_uri") String request_uri) {

		log.info("Authorize request received: clientId {} - requestUri {}", client_id, request_uri);
		log.trace("/authorize params: client_id {} - request_uri {}", client_id, request_uri);
		// TODO eIDAS LoA High
		validateParams(client_id, request_uri);
		SessionInfo si = authService.retrieveStateParam(client_id, request_uri);
		String uri = redirectUrl.concat("?state=").concat(si.getState());
		log.trace("authorize response: {}", uri);
		log.info("Authorize request accepted");
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(uri)).build();
	}

	@GetMapping(path = "/callback")
	public ModelAndView callback(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView("form_post_callback");

		String state = params.get("state");
		log.info("Callback request received: state {}", state);
		validateParams(state);
		SessionInfo si = null;
		String responseDirectPost = "";
		try {
			si = authService.checkStateParamAndReturnSessionInfo(state);
		} catch (Exception e) {
			log.error("", e);
			throw e;
		}
		try {
			responseDirectPost = authService.generateDirectPostAuthorizeResponse(si, state, issuer);
		} catch (JOSEException | ParseException e) {
			log.error("Error in generating authorize response jwt", e);
			throw e;
		}
		log.trace("callback response: {}", responseDirectPost);
		log.info("callback request accepted");
		mav.addObject("response", responseDirectPost);
		mav.addObject("clientUri", si.getRedirectUri());

		return mav;
	}


	@PostMapping(path = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TokenResponse> token(@FormParam("grant_type") String grant_type,
			@FormParam("client_id") String client_id, @FormParam("code") String code,
			@FormParam("code_verifier") String code_verifier,
			@FormParam("client_assertion_type") String client_assertion_type,
			@FormParam("client_assertion") String client_assertion, @FormParam("redirect_uri") String redirect_uri,
			@RequestHeader("DPoP") String dpop)
			throws JOSEException, ParseException {

		log.trace("/token params: grant_type {} - client_assertion_type {}", grant_type, client_assertion_type);
		log.trace("-> client_id {}", client_id);
		log.trace("-> client_assertion {}", client_assertion);
		log.trace("-> redirect_uri {}", redirect_uri);
		log.trace("-> code_verifier {}", code_verifier);
		log.trace("-> code {}", code);
		log.trace("-> DPoP header {}", dpop);
		
		log.info("Token request: client_id {}", client_id);

		validateParams(grant_type, client_id, code, code_verifier, client_assertion, client_assertion_type,
				redirect_uri);
		validateHeaders(dpop);

		tokenService.validateClientAssertion(client_assertion_type, client_assertion);
		tokenService.checkDpop(dpop);
		tokenService.checkParams(client_id, code, code_verifier);

		TokenResponse response = tokenService.generateTokenResponse(client_id, dpop);
		log.trace("token response: {}", response);
		log.info("--> /token - success");
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/credential", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CredentialResponse> credential(
			@FormParam("credential_definition") String credential_definition, @FormParam("format") String format,
			@FormParam("proof") String proof, @RequestHeader("DPoP") String dpop,
			@RequestHeader("Authorization") String authorization)
			throws JOSEException, ParseException {

		log.trace("/credential params: credential_definition {} - format {}", credential_definition, format);
		log.trace("-> proof {}", proof);
		log.trace("-> DPoP header {}", dpop);
		log.trace("-> Authorization header {}", authorization);
		
		log.info("Credential request received: credential_definition {} - format {}", credential_definition, format);

		validateParams(credential_definition, format, proof);
		validateHeaders(dpop, authorization);

		ProofRequest proofReq = null;
		credentialService.checkDpop(dpop);
		ObjectMapper om = new ObjectMapper();
		try {
			proofReq = om.readValue(proof, ProofRequest.class);
			credentialService.validateProofRequest(proofReq, dpop);
		} catch (JsonProcessingException e) {
			log.error("", e);
			throw new MalformedCredentialProofException("Credential proof not valid");
		}
		credentialService.checkAuthorizationAndProof(authorization, proofReq);
		CredentialResponse response;
		try {
			response = credentialService.generateSdCredentialResponse(dpop, proofReq);
			log.trace("credential response: {}", response);
			log.info("--> /credential - success");
			return ResponseEntity.ok(response);
		} catch (JOSEException e) {
			log.error("Error in credential generation", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (ParseException e) {
			log.error("Error in credential generation", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
	}

	private void validateParams(String... params) {
		if (Stream.of(params).anyMatch(Objects::isNull) || Stream.of(params).anyMatch(String::isBlank)) {
			log.error("Missing or blank param");
			throw new MissingOrBlankParamException("Missing or blank param");
		}
	}

	private void validateHeaders(String... headers) {
		if (Stream.of(headers).anyMatch(Objects::isNull) || Stream.of(headers).anyMatch(String::isBlank)) {
			log.error("Missing or blank header");
			throw new MissingOrBlankParamException("Missing or blank header");
		}
	}

}
