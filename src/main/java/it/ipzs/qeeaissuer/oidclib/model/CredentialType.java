package it.ipzs.qeeaissuer.oidclib.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CredentialType {

	private String format;
	private String id;
	private final List<String> cryptographic_binding_methods_supported = List.of("jwk");
	private final List<String> cryptographic_suites_supported = List.of("RS256");

	private List<DisplayConf> display;

	private List<CredentialDefinition> credential_definition;

}
