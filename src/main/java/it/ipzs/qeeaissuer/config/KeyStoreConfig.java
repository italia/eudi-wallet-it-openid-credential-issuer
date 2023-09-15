package it.ipzs.qeeaissuer.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeyStoreConfig implements CommandLineRunner {

	@Value("${keys.directory-path}")
	private String keyDirectoryPath;

	@Value("${keys.path}")
	private String keyFilePath;

	@Value("${keys.public-jwk-set-path}")
	private String publicKeyFilePath;

	private JWK key;

	private JWKSet jwks;

	@Override
	public void run(String... args) throws Exception {
		Path keyPath = Paths.get(keyDirectoryPath);
		if (!Files.exists(keyPath)) {
			Files.createDirectory(keyPath);
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
		}

		this.key = loadKey();
		this.jwks = loadJWKS();

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

	public JWK getKey() {
		return key;
	}

	public JWKSet getJwks() {
		return jwks;
	}

}
