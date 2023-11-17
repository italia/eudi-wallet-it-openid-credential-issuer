package it.ipzs.qeaaissuer.dto;

import lombok.Data;

@Data
public class ValidityInfoDto {

	private String signed;
	private String validFrom;
	private String validUntil;
}
