package it.ipzs.pidprovider.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RecordDto implements Serializable {

	private static final long serialVersionUID = -5547446763710649329L;

	private String type;

	private SourceDto source;
}
