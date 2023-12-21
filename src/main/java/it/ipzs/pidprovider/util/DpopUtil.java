package it.ipzs.pidprovider.util;

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

import it.ipzs.pidprovider.exception.DpopJwtMissingClaimException;
import it.ipzs.pidprovider.exception.JwkKeyTypeException;
import it.ipzs.pidprovider.exception.JwsHeaderMissingFieldException;

@Component
public class DpopUtil {

    private static final String TYPE_HEADER = "dpop+jwt";
    private static final String JTI_CLAIM = "jti";
    private static final String HTM_CLAIM = "htm";
    private static final String HTU_CLAIM = "htu";
    private static final String IAT_CLAIM = "iat";

    
	public JWTClaimsSet parse(String dpop) throws ParseException, JOSEException {
		SignedJWT jwt = SignedJWT.parse(dpop);

		JWSHeader jwsHeader = jwt.getHeader();
		validateHeader(jwsHeader);
		JWK jwk = jwsHeader.getJWK();
		
		JWSVerifier verifier;
		 if (jwk instanceof ECKey) {
				ECKey ecKey = (ECKey) jwk;
	            verifier = new ECDSAVerifier(ecKey);
			} else if (jwk instanceof RSAKey) {
				RSAKey rsaKey = (RSAKey) jwk;
	            verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
			} else {
				throw new JwkKeyTypeException("JWK key type not matched: " + jwk.getKeyType().getValue());
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
			throw new DpopJwtMissingClaimException("Invalid DPoP - missing claim");
		}
	}

	private void validateHeader(JWSHeader jwsHeader) {
		if (jwsHeader.getType() == null || !TYPE_HEADER.equals(jwsHeader.getType().getType())) {
			throw new JwsHeaderMissingFieldException("Type header not matched: " + jwsHeader.getType());
		}

		if (jwsHeader.getAlgorithm() == null) {
			throw new JwsHeaderMissingFieldException("Algorithm header is missing");
		}

		if (jwsHeader.getJWK() == null) {
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

	public Date getIatClaim(JWTClaimsSet claims) throws ParseException {
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
