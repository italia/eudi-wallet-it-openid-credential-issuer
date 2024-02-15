package it.ipzs.qeaaissuer.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.qeaaissuer.dto.ParResponse;
import it.ipzs.qeaaissuer.exception.ClientAssertionValidationException;
import it.ipzs.qeaaissuer.exception.HashedWalletInstanceAttestationGenerationException;
import it.ipzs.qeaaissuer.exception.ParJwtRequestValidationException;
import it.ipzs.qeaaissuer.exception.ParRequestJwtMissingParameterException;
import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.util.ParRequestJwtUtil;
import it.ipzs.qeaaissuer.util.SessionUtil;
import it.ipzs.qeaaissuer.util.WalletInstanceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParService {

	private final WalletInstanceUtil walletInstanceUtil;
	private final ParRequestJwtUtil parRequestUtil;
	private final SessionUtil sessionUtil;

	private String generateUriId() {
		return UUID.randomUUID().toString();
	}

	public Object validateClientAssertionAndRetrieveCnf(String clientAssertion) {
		try {
			JWTClaimsSet parse = walletInstanceUtil.parse(clientAssertion);
			log.info("- client assertion validated");
			return parse.getClaim("cnf");
		} catch (ParseException | JOSEException e) {
			log.error("! client assertion not validated", e);
			throw new ClientAssertionValidationException(e);
		}

	}

	public ParResponse generateRequestUri(String request, Object cnf, String clientAssertion) {
		try {
			JWTClaimsSet jwtClaimsSet = parRequestUtil.parse(request);

			SessionInfo si = new SessionInfo();
			si.setCnf(cnf);
			Object redirectUri = jwtClaimsSet.getClaim("redirect_uri");
			if (redirectUri != null)
				si.setRedirectUri((String) redirectUri);

			Object codeChallenge = jwtClaimsSet.getClaim("code_challenge");
			if (codeChallenge != null)
				si.setCodeChallenge((String) codeChallenge);

			Object state = jwtClaimsSet.getClaim("state");
			if (state != null)
				si.setState((String) state);

			Object clientId = jwtClaimsSet.getClaim("client_id");
			if (clientId != null)
				si.setClientId((String) clientId);

			if (Stream.of(redirectUri, codeChallenge, state, clientId).anyMatch(Objects::isNull)) {
				log.debug("redirectUri {} - codeChallenge {} - state {} - clientId {}", redirectUri, codeChallenge,
						state, clientId);
				throw new ParRequestJwtMissingParameterException("JWT request missing required parameter.");
			}
			ParResponse response = new ParResponse();

			response.setRequestUri("urn:ietf:params:oauth:request_uri:".concat(generateUriId()));
			response.setExpiresIn(60);
			si.setWalletInstanceAttestation(clientAssertion);
			si.setHashedWia(generateHashedWia(clientAssertion));
			si.setRequestUri(response.getRequestUri());
			si.setVerified(false);
			si.setCredentialGenerated(false);
			sessionUtil.putSessionInfo(si);

			return response;
		} catch (ParseException | JOSEException | IllegalArgumentException e) {
			log.error("! PAR jwt request not validated", e);
			throw new ParJwtRequestValidationException(e);
		} catch (NoSuchAlgorithmException e) {
			log.error("! hashed wallet instance attestation generation error", e);
			throw new HashedWalletInstanceAttestationGenerationException(e);
		}

	}

	private String generateHashedWia(String clientAssertion) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = digest.digest(clientAssertion.getBytes(StandardCharsets.UTF_8));
		String encodeToString = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
		log.debug("---> hashed wia {}", encodeToString);
		return encodeToString;
	}
}
