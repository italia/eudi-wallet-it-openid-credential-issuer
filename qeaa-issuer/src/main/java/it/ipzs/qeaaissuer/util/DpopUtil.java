package it.ipzs.qeaaissuer.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeaaissuer.exception.JwkKeyTypeException;
import it.ipzs.qeaaissuer.exception.JwsHeaderMissingFieldException;
import it.ipzs.qeaaissuer.exception.DpopJwtMissingClaimException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DpopUtil {

	private static final String TYPE_HEADER = "dpop+jwt";
	private static final String JTI_CLAIM = "jti";
	private static final String HTM_CLAIM = "htm";
	private static final String HTU_CLAIM = "htu";
	private static final String IAT_CLAIM = "iat";
	private static final String ATH_CLAIM = "ath";

	public JWTClaimsSet parse(String dpop) throws ParseException, JOSEException {
		SignedJWT jwt = SignedJWT.parse(dpop);

		JWSHeader jwsHeader = jwt.getHeader();
		validateHeader(jwsHeader);
		JWK jwk = jwsHeader.getJWK();

		JWSVerifier verifier;
		if (jwk instanceof ECKey ecKey) {
			verifier = new ECDSAVerifier(ecKey);
		} else if (jwk instanceof RSAKey rsaKey) {
			verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
		} else {
			log.error("Jwk key type not matched: expected type ECKey/RSAKey - received type {}",
					jwk.getKeyType().getValue());
			throw new JwkKeyTypeException("JWK key type not matched");
		}

		jwt.verify(verifier);
		validateClaims(jwt.getJWTClaimsSet());

		return jwt.getJWTClaimsSet();

	}

	private void validateClaims(JWTClaimsSet claims) {
		Object htm = claims.getClaim(HTM_CLAIM);
		Object htu = claims.getClaim(HTU_CLAIM);
		Object iat = claims.getClaim(IAT_CLAIM);
		Object jti = claims.getClaim(JTI_CLAIM);

		if (Stream.of(htm, htu, iat, jti).anyMatch(Objects::isNull)) {
			log.error("Invalid DPoP - missing claim: htm {} - htu {} - iat {} - jti {}", htm, htu, iat, jti);
			throw new DpopJwtMissingClaimException("Invalid DPoP - missing claim");
		}
	}

	private void validateHeader(JWSHeader jwsHeader) {
		if (jwsHeader.getType() == null || !TYPE_HEADER.equals(jwsHeader.getType().getType())) {
			log.error("Type header not matched: expected {} - received {}", TYPE_HEADER, jwsHeader.getType());
			throw new JwsHeaderMissingFieldException("Type header not matched: " + jwsHeader.getType());
		}

		if (jwsHeader.getAlgorithm() == null) {
			log.error("Algorithm header is null");
			throw new JwsHeaderMissingFieldException("Algorithm header is missing");
		}

		if (jwsHeader.getJWK() == null) {
			log.error("JWK header is null");
			throw new JwsHeaderMissingFieldException("JWK header is missing");
		}
	}

	public String getJtiClaim(JWTClaimsSet claims) throws ParseException {
		return claims.getStringClaim(JTI_CLAIM);
	}

	public String getHtmClaim(JWTClaimsSet claims) throws ParseException {
		return claims.getStringClaim(HTM_CLAIM);
	}

	public String getHtuClaim(JWTClaimsSet claims) throws ParseException {
		return claims.getStringClaim(HTU_CLAIM);
	}

	public String getAthClaim(JWTClaimsSet claims) throws ParseException {
		return claims.getStringClaim(ATH_CLAIM);
	}

	public Date getIatClaim(JWTClaimsSet claims) {
		return claims.getIssueTime();
	}

	public Base64 getSignature(String dpop) throws ParseException {
		SignedJWT jwt = SignedJWT.parse(dpop);
		return jwt.getSignature();
	}

	public String getKid(String dpop) throws ParseException {
		SignedJWT jwt = SignedJWT.parse(dpop);
		return jwt.getHeader().getKeyID();
	}

	public JWK getJwk(String dpop) throws ParseException {
		SignedJWT jwt = SignedJWT.parse(dpop);
		return jwt.getHeader().getJWK();
	}

}
