package it.ipzs.qeaaissuer.util;

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
public class AccessTokenUtil {

    
	public JWTClaimsSet parse(String accessToken) throws ParseException, JOSEException {
		if (accessToken.startsWith("Dpop ")) {
			accessToken = accessToken.replace("Dpop ", "");
		}
		SignedJWT jwt = SignedJWT.parse(accessToken);

		JWSHeader jwsHeader = jwt.getHeader();
		ECKey ecKey = new ECKeyGenerator(Curve.P_256).keyID(jwsHeader.getKeyID()).generate();

		JWSVerifier verifier = new ECDSAVerifier(ecKey);

		jwt.verify(verifier);

		return jwt.getJWTClaimsSet();

	}

	public String getKid(String dpop) throws ParseException {
		SignedJWT jwt = SignedJWT.parse(dpop);
		return jwt.getHeader().getKeyID();
	}

}
