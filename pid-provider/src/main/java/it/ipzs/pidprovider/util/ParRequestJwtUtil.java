package it.ipzs.pidprovider.util;

import java.text.ParseException;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class ParRequestJwtUtil {

	public JWTClaimsSet parse(String dpop) throws ParseException, JOSEException {
		SignedJWT jwt = SignedJWT.parse(dpop);

		JWSHeader jwsHeader = jwt.getHeader();
		ECKey ecKey = new ECKeyGenerator(Curve.P_256).keyID(jwsHeader.getKeyID()).generate();

		JWSVerifier verifier = new ECDSAVerifier(ecKey);

		jwt.verify(verifier);

		return jwt.getJWTClaimsSet();

	}
}
