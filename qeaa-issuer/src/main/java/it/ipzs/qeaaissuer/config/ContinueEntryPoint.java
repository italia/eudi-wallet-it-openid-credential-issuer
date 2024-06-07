package it.ipzs.qeaaissuer.config;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ContinueEntryPoint extends LoginUrlAuthenticationEntryPoint {

	public ContinueEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {

		String continueParamValue = UrlUtils.buildRequestUrl(request);
		String redirect = super.determineUrlToUseForThisRequest(request, response, exception);
		if (continueParamValue.contains("/cie/login")) {

			return UriComponentsBuilder.fromPath(redirect).toUriString().concat("?" + request.getQueryString());
		} else

			return UriComponentsBuilder.fromPath(redirect).toUriString();
	}
}
