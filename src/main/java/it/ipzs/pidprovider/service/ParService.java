package it.ipzs.pidprovider.service;

import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

import it.ipzs.pidprovider.dto.ParResponse;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.util.ParRequestJwtUtil;
import it.ipzs.pidprovider.util.SessionUtil;
import it.ipzs.pidprovider.util.WalletInstanceUtil;
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

	public void validateClientAssertion(String clientAssertion) {
		// TODO validate wallet instance
		try {
			walletInstanceUtil.parse(clientAssertion);
		} catch (ParseException | JOSEException e) {
			log.error("", e);
		}

	}

	public ParResponse generateRequestUri(String request) {
		try {
			JWTClaimsSet jwtClaimsSet = parRequestUtil.parse(request);

			SessionInfo si = new SessionInfo();
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
				throw new IllegalArgumentException("JWT request missing required parameter.");
			}
			ParResponse response = new ParResponse();

			response.setRequestUri("urn:ietf:params:oauth:request_uri:".concat(generateUriId()));
			response.setExpiresIn(60);
			si.setRequestUri(response.getRequestUri());
			sessionUtil.putSessionInfo(si);

			return response;
		} catch (ParseException | JOSEException e) {
			log.error("", e);
			throw new RuntimeException(e);
		}

	}

}
