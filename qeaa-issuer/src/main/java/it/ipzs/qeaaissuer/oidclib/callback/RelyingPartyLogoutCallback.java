package it.ipzs.qeaaissuer.oidclib.callback;

import it.ipzs.qeaaissuer.oidclib.model.AuthnRequest;
import it.ipzs.qeaaissuer.oidclib.model.AuthnToken;

public interface RelyingPartyLogoutCallback {

	public void logout(String userKey, AuthnRequest authnRequest, AuthnToken authnToken);

}
