package it.ipzs.qeaaissuer.dto;

import lombok.Data;

@Data
public class IssuerSignedItemDto {

	private int digestID;

	private String random;

	private String elementIdentifier;

	private Object elementValue;

}
