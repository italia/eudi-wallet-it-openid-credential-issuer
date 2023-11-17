package it.ipzs.qeaaissuer.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceOfBirthDto implements Serializable {

	private static final long serialVersionUID = -3230395940265695353L;

	private String country;

	private String locality;
}
