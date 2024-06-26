package it.ipzs.qeaaissuer.oidclib.helper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.ipzs.qeaaissuer.oidclib.GlobalOptions;
import it.ipzs.qeaaissuer.oidclib.OIDCConstants;
import it.ipzs.qeaaissuer.oidclib.exception.EntityException;
import it.ipzs.qeaaissuer.oidclib.exception.OIDCException;
import it.ipzs.qeaaissuer.util.StringUtil;

public class EntityHelper {

	private static final Logger logger = LoggerFactory.getLogger(EntityHelper.class);

	@SuppressWarnings("unused")
	private final GlobalOptions<?> options;

	/**
	 * Contacts the subject's ".well-known" endpoint to grab its federation metadata
	 *
	 * @param subject the url representing the subject, the federation entity
	 * @return
	 * @throws OIDCException
	 */
	public static String getEntityConfiguration(String subject)
		throws OIDCException {

		String url = StringUtil.ensureTrailingSlash(
				subject
			).concat(
				OIDCConstants.OIDC_FEDERATION_WELLKNOWN_URL
			);

		logger.info("Starting Entity Configuration Request for {}", url);

		return doHttpGet(url);
	}

	/**
	 * Fetches a statement/configuration of a Federation Entity
	 *
	 * @param url
	 * @return
	 * @throws OIDCException
	 */
	public static String getEntityStatement(String url) throws OIDCException {
		logger.info("Starting Entity Statement Request to {}", url);

		return doHttpGet(url);
	}

	public EntityHelper(GlobalOptions<?> options) {
		this.options = options;
	}

	/**
	 *
	 * @param url
	 * @return
	 * @throws OIDCException
	 */
	private static String doHttpGet(String url) throws OIDCException {
		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(url))
				.GET()
				.build();

			HttpResponse<String> response = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build()
				.send(request, BodyHandlers.ofString());

			logger.debug("{} --> {}", url, response.statusCode());

			if (response.statusCode() != 200) {
				throw new EntityException.Generic(url + " gets " + response.statusCode());
			}

			return response.body();
		}
		catch (EntityException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(url);
			throw new EntityException.Generic(e);
		}
	}

}
