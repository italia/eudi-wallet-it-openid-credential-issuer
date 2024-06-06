package it.ipzs.qeaaissuer.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProofRequest implements Serializable {

	private static final long serialVersionUID = 2311233911178494530L;

	@JsonProperty("proof_type")
	private String proofType;

	private String jwt;

}
