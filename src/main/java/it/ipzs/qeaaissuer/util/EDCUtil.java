package it.ipzs.qeaaissuer.util;


import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.ipzs.qeaaissuer.dto.CedDto;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EDCUtil {

	@Value("${edc-service.url:http://localhost:9898/client/get/}")
	private String serviceUrl;

	public CedDto getEDCInfo(String cf) {
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<CedDto> entity = restTemplate.getForEntity(new URI(serviceUrl + cf), CedDto.class);
			CedDto result = entity.getBody();
			log.debug("result {}", result);
			if (result == null || (result.getResultCode() != null && result.getResultCode().startsWith("KO"))) {
				log.error("Error in data from EDC Service, mock EDC data...");
				// TODO remove mock data
				result = new CedDto();
				result.setNome("Mario");
				result.setCognome("Rossi");
				result.setDataNascita("1980-10-01");
				result.setScadenzaCarta("2025-10-01");
				result.setSerialeCarta("00000000");
				result.setDirittoAccompangatore(1);
			} else {
				result.setScadenzaCarta(alignDateFormat(result.getScadenzaCarta()));
				result.setDataNascita(alignDateFormat(result.getDataNascita()));
			}

			return result;

		} catch (Exception e) {
			log.error("Error in EDC info retrieval, mock EDC data...", e);

			// TODO remove mock data
			CedDto result = new CedDto();
			result.setNome("Mario");
			result.setCognome("Rossi");
			result.setDataNascita(alignDateFormat("01/10/1980"));
			result.setScadenzaCarta(alignDateFormat("01/10/2025"));
			result.setSerialeCarta("00000000");
			result.setDirittoAccompangatore(1);
			return result;
		}

	}

	private String alignDateFormat(String sourceDate) {
		SimpleDateFormat required = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat expectedFromService = new SimpleDateFormat("dd/MM/yyyy");
		String result = null;
		boolean expectedFormat = false;
		try {
			required.parse(sourceDate);
			result = sourceDate;
		} catch (ParseException e) {
			expectedFormat = true;
		}

		if (expectedFormat) {
			try {
				Date date = expectedFromService.parse(sourceDate);
				result = required.format(date);
			} catch (ParseException e) {
				log.error("", e);
				result = "1970-01-01";
			}
		}

		return result;
	}
}
