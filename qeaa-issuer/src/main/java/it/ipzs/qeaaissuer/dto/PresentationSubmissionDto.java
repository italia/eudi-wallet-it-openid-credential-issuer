package it.ipzs.qeaaissuer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentationSubmissionDto {

	private String definition_id;
	private String id;

}
