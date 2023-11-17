package it.ipzs.qeaaissuer.util;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeaaissuer.dto.PresentationCallbackDto;
import it.ipzs.qeaaissuer.dto.PresentationSubmissionDto;
import it.ipzs.qeaaissuer.model.SessionInfo;
import it.ipzs.qeaaissuer.oidclib.OidcWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CallbackResponseJwtUtil {

	private final OidcWrapper oidcWrapper;

	public String generateDirectPostResponse(SessionInfo si, String state, String issuer)
			throws JOSEException, ParseException {

		log.info("state {}", state);
		log.info("code {}", si.getCode());

		JWK jwk = oidcWrapper.getRelyingPartyJWK();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer(issuer).claim("state", state)
				.claim("code", si.getCode()).build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);

		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

	public String generateAuthDirectPostResponse(String clientId, String requestUri)
			throws JOSEException, ParseException {

		JWK jwk = oidcWrapper.getRelyingPartyJWK();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwk.getKeyID()).build();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().claim("client_id", clientId)
				.claim("request_uri", requestUri).build();

		SignedJWT jwt = new SignedJWT(header, claimsSet);

		JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

		jwt.sign(signer);

		return jwt.serialize();
	}

	public PresentationCallbackDto decrypt(String jweString)
			throws JOSEException, ParseException, NoSuchAlgorithmException {
		log.info("encrypted jwt: {}", jweString);
		EncryptedJWT jweObject = EncryptedJWT.parse(jweString);

		RSADecrypter rsaDecrypter = new RSADecrypter(
				oidcWrapper.getRelyingPartyEncryptionJWK().toRSAKey());
		log.info("rsa Key: {}", oidcWrapper.getRelyingPartyEncryptionJWK());
		log.info("supporthed encr method {}", rsaDecrypter.supportedEncryptionMethods());
		log.info("jwe parsed string {}", jweObject.getParsedString());
		log.info("jwe claim set {}", jweObject.getJWTClaimsSet());
		log.info("jwe payload {}", jweObject.getPayload());

		jweObject.decrypt(rsaDecrypter);


		log.info("decrypted? {}", jweObject.getJWTClaimsSet());
		PresentationCallbackDto decryptedPresentation = PresentationCallbackDto.builder()
				.nonce(jweObject.getJWTClaimsSet().getStringClaim("nonce"))
				.state(jweObject.getJWTClaimsSet().getStringClaim("state"))
				.vp_token(jweObject.getJWTClaimsSet().getStringClaim("vp_token")).build();
		Map<String, Object> psClaims = jweObject.getJWTClaimsSet().getJSONObjectClaim("presentation_submission");
		PresentationSubmissionDto ps = PresentationSubmissionDto.builder()
				.definition_id((String) psClaims.get("definition_id")).id((String) psClaims.get("id")).build();
		decryptedPresentation.setPresentation_submission(ps);

		log.info("presentation callback params: {}", decryptedPresentation);

		return decryptedPresentation;
	}

}
