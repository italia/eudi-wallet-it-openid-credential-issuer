package it.ipzs.qeaaissuer.dto;

import lombok.Data;

@Data
public class MobileSecurityObjectPayload {

	private String docType;
	private String version;

	private ValidityInfoDto validityInfo;

	private String digestAlgorithm;
	private Object valueDigests;
	private Object deviceKeyInfo;
}
