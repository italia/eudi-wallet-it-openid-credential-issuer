package it.ipzs.qeaaissuer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import it.ipzs.qeaaissuer.service.SRService;

class SRServiceTest {

	@Test
	void testGenerateRandomByByteLength() {

		// Test
		SRService srService = new SRService();
		String randomString = srService.generateRandomByByteLength(16);

		// Verify
		assertNotNull(randomString);
		assertEquals(22, randomString.length());
	}

	@Test
	void testGenerateRandomFromString() {
		// Test
		SRService srService = new SRService();
		String randomString = srService.generateRandomFromString("source");

		// Verify
		assertNotNull(randomString);
		assertEquals(8, randomString.length());
	}

	@Test
	void testGenerateRandomByByteLengthActual() {
		// Test
		SRService srService = new SRService();
		String randomString = srService.generateRandomByByteLength(16);

		// Verify
		assertNotNull(randomString);
		assertEquals(22, randomString.length());

		// Decode and verify byte length
		byte[] decodedBytes = Base64.decodeBase64(randomString);
		assertEquals(16, decodedBytes.length);
	}

}
