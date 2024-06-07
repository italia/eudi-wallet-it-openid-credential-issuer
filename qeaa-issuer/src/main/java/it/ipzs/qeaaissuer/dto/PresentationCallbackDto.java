package it.ipzs.qeaaissuer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresentationCallbackDto {

	private String vp_token;
	private String state;
	private String nonce;
	private PresentationSubmissionDto presentation_submission;

}
