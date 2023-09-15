package it.ipzs.qeeaissuer.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * For testing purpose only, used to bypass user authentication with eIDAS High
 * and consent
 *
 */
@RestController
@RequestMapping("/cie/")
public class TestHighConsentController {

	@Value("${consent-controller.consent-url}")
	private String consentUrl;

	@Value("${consent-controller.accepted-url}")
	private String acceptedUrl;

	@Value("${consent-controller.callback-url}")
	private String callbackUrl;

	@GetMapping("login")
	public ResponseEntity<?> loginNoCheck(@RequestParam String state) {

		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(consentUrl.concat("?state=").concat(state)))
				.build();
	}

	@GetMapping("consent")
	public ResponseEntity<?> consentNoCheck(@RequestParam String state) {

		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(acceptedUrl.concat("?state=").concat(state)))
				.build();
	}

	@GetMapping("accepted")
	public ResponseEntity<?> acceptedNoCheck(@RequestParam String state) {

		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(callbackUrl.concat("?state=").concat(state)))
				.build();
	}
}
