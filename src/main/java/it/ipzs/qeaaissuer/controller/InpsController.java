package it.ipzs.qeaaissuer.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.ipzs.qeaaissuer.dto.CedDto;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/inps/")
@Slf4j
public class InpsController {

	@Value("${inps-ws.uri:http://localhost:9898/client/get/}")
	private String inpsUri;

	@PostMapping("/test/edc")
	public ResponseEntity<?> testEdc(@RequestParam String cf, @RequestParam String key) {

		if (!"ipzssviluppo".equals(key)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		RestTemplate restTemplate = new RestTemplate();
		try {
			String uri = inpsUri.concat(cf);
			ResponseEntity<CedDto> entity = restTemplate.getForEntity(new URI(uri), CedDto.class);
			CedDto result = entity.getBody();
			log.info("entity {}", result);
			return ResponseEntity.ok(result);
//			pidConfig = new JSONObject(result);

		} catch (RestClientException | URISyntaxException e) {
			log.error("Error ced retrieval", e);

			return ResponseEntity.internalServerError().body(e.getMessage());
		}

	}

}
