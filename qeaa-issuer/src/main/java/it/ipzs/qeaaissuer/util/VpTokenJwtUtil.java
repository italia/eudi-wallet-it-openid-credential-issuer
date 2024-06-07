package it.ipzs.qeaaissuer.util;

import java.text.ParseException;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.ipzs.qeaaissuer.exception.VpTokenKidValidationException;
import it.ipzs.qeaaissuer.exception.VpTokenVerifyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class VpTokenJwtUtil {


	public JWTClaimsSet parse(String vpToken) throws ParseException {
		SignedJWT jwt = SignedJWT.parse(vpToken);

		return jwt.getJWTClaimsSet();
	}

	public void validate(String vpToken, Map<String, Object> cnfPid) throws ParseException, JOSEException {
		SignedJWT jwt = SignedJWT.parse(vpToken);
		JWSHeader jwsHeader = jwt.getHeader();
		JSONObject cnfJsonObj = new JSONObject(cnfPid);
		JSONObject jwkJsonObj = cnfJsonObj.getJSONObject("jwk");
		if (jwkJsonObj != null && jwkJsonObj.getString("kid") != null && jwsHeader.getKeyID() != null
				&& !jwsHeader.getKeyID().equals(jwkJsonObj.getString("kid"))) {
				log.error("VpToken JWT not verified - JWK Key ID different from CNF in Pid Credentials");
				throw new VpTokenKidValidationException("VpToken JWT not verified");
		}
		if (jwkJsonObj != null) {
			ECKey ecKey = ECKey.parse(jwkJsonObj.toMap());

			JWSVerifier verifier = new ECDSAVerifier(ecKey);

			if (Boolean.FALSE.equals(jwt.verify(verifier))) {
				log.error("VpToken JWT not verified with kid-generated key");
				throw new VpTokenVerifyException("VpToken JWT not verified");
			}
		}
	}
}
