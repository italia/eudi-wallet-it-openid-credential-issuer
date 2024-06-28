package it.ipzs.qeaaissuer.config;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.*;
import it.ipzs.qeaaissuer.oidclib.OidcWrapper;
import it.ipzs.qeaaissuer.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

@Component
@Slf4j
public class KeyStoreConfig implements CommandLineRunner {

	@Value("${keys.directory-path}")
	private String keyDirectoryPath;

	@Value("${keys.path}")
	private String keyFilePath;

	@Value("${keys.public-jwk-set-path}")
	private String publicKeyFilePath;

	@Value("${keys.encr-path}")
	private String encrKeyFilePath;

	@Value("${keys.public-encr-jwk-set-path}")
	private String publicEncrKeyFilePath;

	@Value("${keys.mdoc-path}")
	private String mdocKeyFilePath;

	@Value("${keys.mdoc-public-path}")
	private String publicMdocKeyFilePath;

	@Value("${keys.x5c-path}")
	private String x5cFilePath;

	@Value("${keys.x5c-url}")
	private String certAuthUrl;

	@Autowired
	private OidcWrapper oidcWrapper;

	@Override
	public void run(String... args) throws Exception {
		log.debug("Running KeyStore...");
		boolean reload = false;
		Path keyPath = Paths.get(keyDirectoryPath);
		if (!Files.exists(keyPath)) {
			Files.createDirectory(keyPath);
			reload = true;
		} else {
			log.debug("{} path exists", keyDirectoryPath);
		}

		if (!new File(keyFilePath).exists()) {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			KeyPair keyPair = gen.generateKeyPair();

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, 1);
			Date validityEndDate = cal.getTime();

			JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
					.privateKey((RSAPrivateKey) keyPair.getPrivate()).keyUse(KeyUse.SIGNATURE)
					.keyID(UUID.randomUUID().toString()).issueTime(new Date()).expirationTime(validityEndDate)
					.keyIDFromThumbprint().build();

			try (FileWriter fw = new FileWriter(keyFilePath)) {

				fw.write(jwk.toJSONString());
			} catch (Exception e) {
				log.error("", e);
			}


			JSONArray jsonPublicJwk = new JSONArray().put(new JSONObject(jwk.toPublicJWK().toJSONObject()));

			JSONObject pubKeysJsonObj = new JSONObject().put("keys", jsonPublicJwk);


			try (FileWriter fw = new FileWriter(publicKeyFilePath)) {

				fw.write(pubKeysJsonObj.toString());
			} catch (Exception e) {
				log.error("", e);
			}
			reload = true;
		} else {
			log.debug("{} path exists", keyFilePath);
		}

		if (!new File(mdocKeyFilePath).exists()) {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
			gen.initialize(Curve.P_256.toECParameterSpec());
			KeyPair keyPair = gen.generateKeyPair();

			JWK jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic()).keyUse(KeyUse.SIGNATURE)
					.keyIDFromThumbprint()
					.privateKey((ECPrivateKey) keyPair.getPrivate()).build();

			try (FileWriter fw = new FileWriter(mdocKeyFilePath)) {
				fw.write(jwk.toJSONString());
			} catch (Exception e) {
				log.error("", e);
			}

			JSONArray jsonPublicJwk = new JSONArray().put(new JSONObject(jwk.toPublicJWK().toJSONObject()));

			JSONObject pubKeysJsonObj = new JSONObject().put("keys", jsonPublicJwk);

			try (FileWriter fw = new FileWriter(publicMdocKeyFilePath)) {
				fw.write(pubKeysJsonObj.toString());
			} catch (Exception e) {
				log.error("", e);
			}
			reload = true;
		} else {
			log.debug("{} path exists", mdocKeyFilePath);
		}

		if (!new File(encrKeyFilePath).exists()) {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			KeyPair keyPair = gen.generateKeyPair();

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, 1);
			Date validityEndDate = cal.getTime();

			JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
					.privateKey((RSAPrivateKey) keyPair.getPrivate()).keyUse(KeyUse.ENCRYPTION)
					.algorithm(JWEAlgorithm.RSA_OAEP_256)
					.keyID(UUID.randomUUID().toString()).issueTime(new Date()).expirationTime(validityEndDate)
					.keyIDFromThumbprint().build();

			try (FileWriter fw = new FileWriter(encrKeyFilePath)) {

				fw.write(jwk.toJSONString());
			} catch (Exception e) {
				log.error("", e);
			}

			JSONArray jsonPublicJwk = new JSONArray().put(new JSONObject(jwk.toPublicJWK().toJSONObject()));

			JSONObject pubKeysJsonObj = new JSONObject().put("keys", jsonPublicJwk);

			try (FileWriter fw = new FileWriter(publicEncrKeyFilePath)) {

				fw.write(pubKeysJsonObj.toString());
			} catch (Exception e) {
				log.error("", e);
			}
			reload = true;
		} else {
			log.debug("{} path exists", encrKeyFilePath);
		}

		if (!new File(x5cFilePath).exists()) {
			//CA ref for testing purpose
			RestTemplate restTemplate = new RestTemplate();
			Map<String, String> requestMap = new HashMap<>();
			requestMap.put("subject", "EUDI Wallet IT Issuer");
			requestMap.put("organization", "EUDI Wallet");
			requestMap.put("country", "Italy");
			requestMap.put("locality", "Rome");
			requestMap.put("state", "Rome");
			requestMap.put("email", "ipzssviluppo@gmail.com");

			String x5c = null;

			ResponseEntity<String> response = restTemplate.postForEntity(certAuthUrl.concat("/generate"), requestMap, String.class);
			if (response.hasBody() && !StringUtil.isBlank(response.getBody())) {
				String pemContent = response.getBody();
				String pem = pemContent.replace("-----BEGIN CERTIFICATE-----", "")
						.replace("-----END CERTIFICATE-----", "")
						.replaceAll("\\s", "");

				// Decodifica Base64
				byte[] decoded = Base64.getDecoder().decode(pem);

				// Crea un oggetto X509Certificate da byte array
				CertificateFactory factory = CertificateFactory.getInstance("X.509");
				try (InputStream is = new ByteArrayInputStream(decoded)) {
					X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
					BigInteger serialNumber = cert.getSerialNumber();

					ResponseEntity<String> responseChain = restTemplate.getForEntity(certAuthUrl.concat("/chain?serialNumber=").concat(serialNumber.toString()), String.class);
					if (responseChain.hasBody() && !StringUtil.isBlank(responseChain.getBody())) {
						x5c = responseChain.getBody();
					}
				}
			}

			if (x5c != null) {
				try (FileWriter fw = new FileWriter(x5cFilePath)) {

					fw.write(x5c);
				} catch (Exception e) {
					log.error("", e);
				}

				reload = true;
			}

		} else {
			log.debug("{} path exists", x5cFilePath);
		}

		if (reload) {
			oidcWrapper.reloadKeys();
		}

	}

	public JWK loadKey() {

		if (new File(keyFilePath).exists()) {
			Path path = Paths.get(keyFilePath);

			try {
				String read = Files.readAllLines(path).get(0);
				return JWK.parse(read);
			} catch (IOException | ParseException e) {
				log.error("", e);
			}
		}

		throw new RuntimeException("cannot load key from file");

	}

	public JWKSet loadJWKS() {
		if (new File(publicKeyFilePath).exists()) {
			Path path = Paths.get(publicKeyFilePath);

			try {
				String read = Files.readAllLines(path).get(0);
				return JWKSet.parse(read);
			} catch (IOException | ParseException e) {
				log.error("", e);
			}
		}

		throw new RuntimeException("cannot load key from file");
	}

}
