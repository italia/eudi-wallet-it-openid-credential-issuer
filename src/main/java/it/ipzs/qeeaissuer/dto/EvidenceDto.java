package it.ipzs.qeeaissuer.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class EvidenceDto implements Serializable {

	private static final long serialVersionUID = 7290453042601609012L;

	private String type;

	private RecordDto record;
}
