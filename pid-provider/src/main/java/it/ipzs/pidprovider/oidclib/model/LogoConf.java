package it.ipzs.pidprovider.oidclib.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LogoConf {

	private String url;
	private String alt_text;
}
