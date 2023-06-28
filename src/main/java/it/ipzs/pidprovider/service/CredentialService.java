package it.ipzs.pidprovider.service;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.ipzs.pidprovider.dto.CredentialClaimsDto;
import it.ipzs.pidprovider.dto.CredentialResponse;
import it.ipzs.pidprovider.dto.EvidenceDto;
import it.ipzs.pidprovider.dto.PlaceOfBirthDto;
import it.ipzs.pidprovider.dto.RecordDto;
import it.ipzs.pidprovider.dto.SourceDto;
import it.ipzs.pidprovider.dto.VerificationDto;
import it.ipzs.pidprovider.dto.VerifiedClaims;
import it.ipzs.pidprovider.model.SessionInfo;
import it.ipzs.pidprovider.util.AccessTokenUtil;
import it.ipzs.pidprovider.util.CredentialProofUtil;
import it.ipzs.pidprovider.util.DpopUtil;
import it.ipzs.pidprovider.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialService {

	private final SRService srService;
	private final DpopUtil dpopUtil;
	private final AccessTokenUtil accessTokenUtil;
	private final CredentialProofUtil proofUtil;
	private final SessionUtil sessionUtil;

	public CredentialResponse generateCredentialResponse(String proof) throws JOSEException, ParseException {
		CredentialResponse response = CredentialResponse.builder().format("vc+jwt")
				.nonce(srService.generateRandomByByteLength(32)).nonceExpiresIn(86400)
				.credential(generateCredential(proof)).build();

		return response;
	}

	private String generateCredential(String proof) throws JOSEException, ParseException {

		ECKey ecJWK = new ECKeyGenerator(Curve.P_256).keyID(proofUtil.getKid(proof)).generate(); // TODO kid check
		
        Claims claims = Jwts.claims()
        		.setSubject("urn:uuid:".concat(UUID.randomUUID().toString()))
        		.setIssuer("https://localhost")
        		.setIssuedAt(new Date())
        		.setExpiration(new Date(new Date().getTime() + 86400 * 1000))
        		.setId(UUID.randomUUID().toString());
        
		// FIXME test data
		// TODO implement vc+sd-jwt
		VerifiedClaims vc = new VerifiedClaims();
		CredentialClaimsDto dto = new CredentialClaimsDto();
		dto.setGivenName("Mario");
		dto.setUniqueId("idANPR");
		dto.setFamilyName("Rossi");
		dto.setBirthDate("1980-01-10");
		dto.setTaxIdNumber("TINIT-RSSMRA80A10H501A");
		dto.setPlaceOfBirth(PlaceOfBirthDto.builder().country("IT").locality("Rome").build());

		vc.setClaims(dto);
		VerificationDto ver = new VerificationDto();
		ver.setAssuranceLevel("high");
		ver.setTrustFramework("eidas");
		EvidenceDto ev = new EvidenceDto();
		ev.setType("electronic_record");

		RecordDto rec = new RecordDto();
		rec.setType("eidas.it.cie");
		SourceDto src = new SourceDto();
		src.setCountryCode("IT");
		src.setOrganizationId("m_it");
		src.setOrganizationName("Ministero dell'Interno");

		rec.setSource(src);

		ev.setRecord(rec);
		ver.setEvidence(ev);

		vc.setVerification(ver);

		claims.put("verified_claims", vc);

		// TODO wip algorithm and key
        return Jwts.builder()
                .setClaims(claims)
				.signWith(SignatureAlgorithm.HS512, ecJWK.getX().toString())
				.setHeaderParam("typ", "vc+jwt")
                .compact();
	}

	public void checkDpop(String dpop) {
		try {
			JWTClaimsSet claimsSet = dpopUtil.parse(dpop);
			String htuClaim = dpopUtil.getHtuClaim(claimsSet);
			String htmClaim = dpopUtil.getHtmClaim(claimsSet);

			// TODO better check uri
			if (!htuClaim.endsWith("/credential") || !htmClaim.equals("POST")) {
				throw new IllegalArgumentException("Invalid claims: " + htmClaim + " - " + htuClaim);
			}
		} catch (ParseException | JOSEException e) {
			log.error("", e);
			throw new RuntimeException("", e);
		}
	}

	public void checkAuthorizationAndProof(String authorization, String proof) throws ParseException, JOSEException {
		JWTClaimsSet tokenClaims = accessTokenUtil.parse(authorization);
		JWTClaimsSet proofClaims = proofUtil.parse(proof);
		
		Object proofNonce = proofClaims.getClaim("nonce");
		Object proofClientId = proofClaims.getClaim("iss");
		Object tokenNonce = tokenClaims.getClaim("nonce");
		if(Stream.of(proofNonce, proofClientId, tokenNonce).anyMatch(Objects::isNull)) {
			log.error("proof Nonce {} - token nonce {} - proof clientId {} ", proofNonce, tokenNonce, proofClientId);
			throw new IllegalArgumentException("proof or authorization parameter missing required key");
		} else {
			boolean checkPassed = false;
			SessionInfo sessionInfo = sessionUtil.getSessionInfo((String) proofClientId);
			if (sessionInfo != null) {
				log.debug("session info {}", sessionInfo);
				String nonce = sessionInfo.getNonce();
				if (nonce.equals(tokenNonce) && nonce.equals(proofNonce)) {
					checkPassed = true;
				} else {
					log.debug("token nonce {} - proof nonce {} - nonce in session {}", tokenNonce, proofNonce, nonce);
				}
			}

			if (!checkPassed)
				throw new IllegalArgumentException("Nonce does not match");
		}

	}

}
