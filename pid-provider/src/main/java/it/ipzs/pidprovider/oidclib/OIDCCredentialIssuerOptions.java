package it.ipzs.pidprovider.oidclib;

import java.util.List;

import it.ipzs.pidprovider.oidclib.model.CredentialType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OIDCCredentialIssuerOptions extends GlobalOptions<OIDCCredentialIssuerOptions> {

	private String jwk;
	private String encrJwk;
	private String credentialIssueUrl;
	private String authorizationEndpoint;
	private String tokenEndpoint;
	private String pushedAuthorizationRequestEndpoint;
	private String credentialEndpoint;
	private final List<String> dpopSigningAlgValuesSupported = List.of("RS256");
	private List<CredentialType> credentialsSupported;
	private String trustMarks;
	private String sub;
	private List<String> trustChain;
}
