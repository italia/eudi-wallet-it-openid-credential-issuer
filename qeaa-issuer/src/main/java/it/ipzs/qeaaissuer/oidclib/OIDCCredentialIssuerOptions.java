package it.ipzs.qeaaissuer.oidclib;

import it.ipzs.qeaaissuer.oidclib.model.CredentialType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class OIDCCredentialIssuerOptions extends GlobalOptions<OIDCCredentialIssuerOptions> {

	private String jwk;
	private String mdocJwk;
	private String mdocX5Chain;
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
