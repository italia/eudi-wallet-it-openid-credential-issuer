package it.ipzs.pidprovider.controller;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

import it.ipzs.pidprovider.dto.CredentialResponse;
import it.ipzs.pidprovider.dto.ParResponse;
import it.ipzs.pidprovider.dto.ProofRequest;
import it.ipzs.pidprovider.dto.TokenResponse;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.service.AuthorizationService;
import it.ipzs.pidprovider.service.CredentialService;
import it.ipzs.pidprovider.service.ParService;
import it.ipzs.pidprovider.service.TokenService;
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

		// TODO check validity client assertion
		parService.validateClientAssertion(client_assertion);
		ParResponse response = parService.generateRequestUri(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/authorize")
	public ResponseEntity<?> authorize(@RequestParam("client_id") String client_id,
			@RequestParam("request_uri") String request_uri) {

		// TODO eIDAS LoA High
		String stateParam = authService.retrieveStateParam(client_id, request_uri);
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(redirectUrl.concat("?state=").concat(stateParam))).build();
	}

	@GetMapping("/callback")
	public ResponseEntity<?> callback(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {

		// TODO manage params from eIDAS LoA High callback and redirect to app universal
		// url

		String state = params.get("state");
		SessionInfo si = null;
		try {
			si = authService.checkStateParamAndReturnSessionInfo(state);
		} catch (Exception e) {
			log.error("", e);
			return ResponseEntity.badRequest().build();
		}
		String uri = si.getRedirectUri().concat("?code=").concat(si.getCode())
				.concat("&state=").concat(state).concat("&iss=https%3A%2F%2Fpid-provider.example.org");
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(uri)).build();
	}

	@PostMapping(path = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TokenResponse> token(@FormParam("grant_type") String grant_type,
			@FormParam("client_id") String client_id, @FormParam("code") String code,
			@FormParam("code_verifier") String code_verifier,
			@FormParam("client_assertion_type") String client_assertion_type,
			@FormParam("client_assertion") String client_assertion, @FormParam("redirect_uri") String redirect_uri,
			@RequestHeader("DPoP") String dpop) throws JOSEException, ParseException {

		try {
			tokenService.checkDpop(dpop);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		try {
			tokenService.checkParams(client_id, code, code_verifier);
		} catch (Exception e) {
			log.error("", e);
			// TODO return error message code after integration
		}
		TokenResponse response = tokenService.generateTokenResponse(client_id, dpop);
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/credential", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CredentialResponse> credential(
			@FormParam("credential_definition") String credential_definition, @FormParam("format") String format,
			@FormParam("proof") String proof,
			@RequestHeader("DPoP") String dpop, @RequestHeader("Authorization") String authorization)
			throws JOSEException, ParseException {

		ProofRequest proofReq = null;
		try {
			credentialService.checkDpop(dpop);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		try {
			ObjectMapper om = new ObjectMapper();
			try {
				proofReq = om.readValue(proof, ProofRequest.class);
			} catch (JsonProcessingException e) {
				log.error("", e);
			}
			credentialService.checkAuthorizationAndProof(authorization, proofReq);
		} catch (Exception e) {
			// TODO remove try-catch after integration
			log.error("", e);
		}
		CredentialResponse response;
		try {
			response = credentialService.generateSdCredentialResponse(proofReq);
			return ResponseEntity.ok(response);
		} catch (NoSuchAlgorithmException | JOSEException e) {
			log.error("", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (ParseException e) {
			log.error("", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
	}

}
