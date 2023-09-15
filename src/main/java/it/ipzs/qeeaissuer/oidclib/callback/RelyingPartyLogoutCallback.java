package it.ipzs.qeeaissuer.oidclib.callback;

import it.ipzs.qeeaissuer.oidclib.model.AuthnRequest;
import it.ipzs.qeeaissuer.oidclib.model.AuthnToken;

public interface RelyingPartyLogoutCallback {

	public void logout(String userKey, AuthnRequest authnRequest, AuthnToken authnToken);

}
