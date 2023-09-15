package it.ipzs.qeeaissuer.oidclib.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@JsonInclude(content = Include.NON_EMPTY)
public class DisplayConf {

	private String name;
	private String locale;
	private String background_color;
	private String text_color;
	private LogoConf logo;

}
