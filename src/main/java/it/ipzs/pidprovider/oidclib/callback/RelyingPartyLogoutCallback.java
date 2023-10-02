package it.ipzs.pidprovider.oidclib.callback;

import it.ipzs.pidprovider.oidclib.model.AuthnRequest;
import it.ipzs.pidprovider.oidclib.model.AuthnToken;

public interface RelyingPartyLogoutCallback {

	public void logout(String userKey, AuthnRequest authnRequest, AuthnToken authnToken);

}
