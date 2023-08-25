package it.ipzs.pidprovider.service;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

@Service
public class SRService {

	public String generateRandomByByteLength(int length) {
		SecureRandom sr = new SecureRandom();
		byte[] randomBytes = new byte[length];
		sr.nextBytes(randomBytes);

		return Base64.encodeBase64URLSafeString(randomBytes);
	}

	public String generateRandomFromString(String source) {
		SecureRandom sr = new SecureRandom();
		byte[] randomBytes = source.getBytes();
		sr.nextBytes(randomBytes);

		return Base64.encodeBase64URLSafeString(randomBytes);
	}
}
