package it.ipzs.qeaaissuer.controller;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.ipzs.qeaaissuer.oidclib.OIDCConstants;
import it.ipzs.qeaaissuer.oidclib.OidcConfig;
import it.ipzs.qeaaissuer.oidclib.OidcWrapper;
import it.ipzs.qeaaissuer.oidclib.schemas.WellKnownData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/rp")
public class WellKnownController {

	private static Logger logger = LoggerFactory.getLogger(WellKnownController.class);

	@Autowired
	private OidcConfig oidcConfig;

	@Autowired
	private OidcWrapper oidcWrapper;

	@GetMapping("/" + OIDCConstants.OIDC_FEDERATION_WELLKNOWN_URL)
	public ResponseEntity<String> wellKnownFederation(@RequestParam(required = false) String format,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		boolean jsonMode = "json".equals(format);

		WellKnownData wellKnown = oidcWrapper.getWellKnownData(request.getRequestURL().toString(), jsonMode);
		if (wellKnown.getStep() == WellKnownData.STEP_ONLY_JWKS) {
			logger.info("Generated jwk. Please add it into 'application.yaml' or save as '"
					+ oidcConfig.getRelyingParty().getJwkFilePath() + "'.\n" + wellKnown.getValue());

			String body = new JSONObject().put("ERROR", "Do OnBoarding configuration").toString();

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
		}

		if (jsonMode) {
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(wellKnown.getValue());
		}
		else {
			return ResponseEntity.ok().contentType(new MediaType("application", "entity-statement+jwt"))
					.body(wellKnown.getValue());
		}
	}


}
