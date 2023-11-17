package it.ipzs.qeaaissuer.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.google.iot.cbor.CborArray;
import com.google.iot.cbor.CborByteString;
import com.google.iot.cbor.CborConversionException;
import com.google.iot.cbor.CborInteger;
import com.google.iot.cbor.CborMap;
import com.google.iot.cbor.CborObject;
import com.google.iot.cbor.CborParseException;
import com.google.iot.cbor.CborTextString;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.upokecenter.cbor.CBORObject;

import COSE.Attribute;
import COSE.CoseException;
import COSE.HeaderKeys;
import COSE.Message;
import COSE.MessageTag;
import COSE.OneKey;
import co.nstant.in.cbor.CborException;
import it.ipzs.qeaaissuer.dto.IssuerSignedDto;
import it.ipzs.qeaaissuer.dto.IssuerSignedItemDto;
import it.ipzs.qeaaissuer.dto.MdocCborDto;
import it.ipzs.qeaaissuer.dto.MdocDocument;
import it.ipzs.qeaaissuer.dto.MobileSecurityObjectPayload;
import it.ipzs.qeaaissuer.oidclib.OidcWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MdocCborService {

	private final SRService srService;
	private final OidcWrapper oidcWrapper;

	public String generateMdocCbor(MdocCborDto cborDto, JWK deviceKey)
			throws CborParseException, IOException, CborException, CoseException, CborConversionException,
			NoSuchAlgorithmException, ParseException, JOSEException {
		List<String> dateParams = List.of("birth_date", "issue_date", "expiry_date");
		ObjectMapper mapper = new CBORMapper();
		JWK jwk = oidcWrapper.getCredentialIssuerJWK();
		CborMap cmap = CborMap.create();
		CborTextString version = CborTextString.create(cborDto.getVersion());
		cmap.put("version", version);
		CborInteger status = CborInteger.create(cborDto.getStatus());
		cmap.put("status", status);
		List<MdocDocument> documents = cborDto.getDocuments();
		CborArray doclist = CborArray.create();
		for (MdocDocument tmp : documents) {
			CborMap documentMap = CborMap.create();
			documentMap.put("docType", CborTextString.create(tmp.getDocType()));

			CborMap issuerSignedMap = CborMap.create();

			CborMap nameSpacesMap = CborMap.create();
			Map<String, List<IssuerSignedItemDto>> nameSpaces = tmp.getIssuerSigned().getNameSpaces();
			for (Entry<String, List<IssuerSignedItemDto>> e : nameSpaces.entrySet()) {
				List<IssuerSignedItemDto> list = e.getValue();
				CborArray array = CborArray.create();
				for (IssuerSignedItemDto item : list) {
					CborMap tmpItem = CborMap.create();
					tmpItem.put("digestID", CborTextString.create(item.getDigestID()));
					tmpItem.put("elementIdentifier", CborTextString.create(item.getElementIdentifier()));
					tmpItem.put("random", CborByteString.create(srService.generateRandomByte(16)));
					if (dateParams.contains(item.getElementIdentifier())) {
						byte[] writeValueAsBytes = mapper.writeValueAsBytes(item.getElementValue());
						CborByteString byteString = CborByteString.create(writeValueAsBytes, 0,
								writeValueAsBytes.length, 1004);
						tmpItem.put("elementValue", byteString);
					} else if ("driving_privileges".equals(item.getElementIdentifier())) {
						CborArray dpArray = CborArray.create();
						CborMap tmpMap = CborMap.create();
						@SuppressWarnings("unchecked")
						Map<String, String> dpObj = (Map<String, String>) item.getElementValue();
						String vcc = dpObj.get("vehicle_category_code");
						CborObject vccCbor = CborObject.createFromCborByteArray(mapper.writeValueAsBytes(vcc));
						tmpMap.put("vehicle_category_code", vccCbor);

						String d1 = dpObj.get("issue_date");
						byte[] writeValueAsBytes = mapper.writeValueAsBytes(d1);
						CborByteString byteString = CborByteString.create(writeValueAsBytes, 0,
								writeValueAsBytes.length, 1004);
						tmpMap.put("issue_date", byteString);

						String d2 = dpObj.get("expiry_date");
						byte[] d2bytes = mapper.writeValueAsBytes(d2);
						CborByteString edBs = CborByteString.create(d2bytes, 0, d2bytes.length, 1004);
						tmpMap.put("expiry_date", edBs);
						dpArray.add(tmpMap);
						tmpItem.put("driving_privileges", dpArray);

					} else {
						tmpItem.put("elementValue",
								CborObject.createFromCborByteArray(mapper.writeValueAsBytes(item.getElementValue())));
					}

					CborByteString elem = CborByteString.create(tmpItem.toCborByteArray(), 0,
							tmpItem.toCborByteArray().length, 24);
					array.add(elem);
				}

				nameSpacesMap.put(e.getKey(), array);

			}

			CborByteString ctagNamespaces = CborByteString.create(nameSpacesMap.toCborByteArray(), 0,
					nameSpacesMap.toCborByteArray().length, 24);
			issuerSignedMap.put("nameSpaces", ctagNamespaces);

			CborMap mobileSecurityObj = CborMap.create(24);
			mobileSecurityObj.put("docType", CborTextString.create("eu.europa.ec.eudiw.pid.1"));
			mobileSecurityObj.put("version", version);
			CborMap validityMap = CborMap.create();
			LocalDateTime currentDateTime = LocalDateTime.now();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			byte[] nowDateBytes = mapper.writeValueAsBytes(currentDateTime.format(formatter));
			validityMap.put("signed",
					CborByteString.create(nowDateBytes, 0, nowDateBytes.length, 0));
			validityMap.put("validFrom",
					CborByteString.create(nowDateBytes, 0, nowDateBytes.length, 0));
			LocalDateTime expr = currentDateTime.plusYears(1);
			byte[] expDateBytes = mapper.writeValueAsBytes(expr.format(formatter));
			validityMap.put("validUntil",
					CborByteString.create(expDateBytes, 0, expDateBytes.length, 0));
			mobileSecurityObj.put("validityInfo", validityMap);
			mobileSecurityObj.put("digestAlgorithm", CborTextString.create("SHA-256"));
			CborMap digestValues = CborMap.create();
			MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
			for (Entry<CborObject, CborObject> entry : nameSpacesMap.entrySet()) {
				CborArray listValue = (CborArray) entry.getValue();
				int id = 1;
				Map<Integer, Object> tmpMap = new HashMap<>();
				for (CborObject ob : listValue) {
					byte[] hashBytes = sha256Digest.digest(ob.toCborByteArray());
					tmpMap.put(id, hashBytes);
					id++;
				}
				CborMap entryMap = CborMap.createFromJavaObject(tmpMap);
				digestValues.put((String) entry.getKey().toJavaObject(), entryMap);
			}
			mobileSecurityObj.put("valueDigests", digestValues);
			OneKey key = new OneKey(jwk.toRSAKey().toPublicKey(), jwk.toRSAKey().toPrivateKey());
			PublicKey publicKey = deviceKey.toECKey().toPublicKey();
			if(publicKey instanceof ECPublicKey ecKey) {
				byte[] xByte = ecKey.getW().getAffineX().toByteArray();
				byte[] yByte = ecKey.getW().getAffineY().toByteArray();
				Map<Integer, Object> tmpMap = new LinkedHashMap<>();
				tmpMap.put(1, 2);
				tmpMap.put(-1, 1);
				tmpMap.put(-2, xByte);
				tmpMap.put(-3, yByte);
				CborMap devicekey = CborMap.createFromJavaObject(tmpMap);
				CborMap devKeyMap = CborMap.create();
				devKeyMap.put("deviceKey", devicekey);
				mobileSecurityObj.put("deviceKeyInfo", devKeyMap);
				
			}
				
			
			CborByteString payload = CborByteString.create(mobileSecurityObj.toCborByteArray(), 0,
					mobileSecurityObj.toCborByteArray().length, 24);
			try {

				COSE.Sign1Message co = new COSE.Sign1Message();
				co.addAttribute(HeaderKeys.Algorithm, CBORObject.FromObject(-37), Attribute.PROTECTED);
				co.SetContent(payload.byteArrayValue());
				CborByteString x5chain = CborByteString.create(getX509().getEncoded(), 0,
						getX509().getEncoded().length);
				co.addAttribute(CBORObject.FromObject(33),
						CBORObject.FromObject(
								x5chain.toString()),
						Attribute.UNPROTECTED);
				
				co.sign(key);
				CborObject issuerAuthObj = CborObject.createFromCborByteArray(co.EncodeToBytes());
				CborArray issuerAuth = CborArray.create();
				issuerAuth.add(issuerAuthObj);
				issuerSignedMap.put("issuerAuth", issuerAuth);
			}
			 catch (Exception e1) {
					log.error("", e1);
				}

			documentMap.put("issuerSigned", issuerSignedMap);
			doclist.add(documentMap);

		}
		cmap.put("documents", doclist);
		byte[] cborByteArray = cmap.toCborByteArray();
		String encodeHexString = encodeHexString(cborByteArray);
		log.info("mdoc cbor {}", encodeHexString);

		return encodeHexString;

	}

	public MdocCborDto parseMdocCbor(String hexCborTest)
			throws IOException, CborException, CborParseException, CoseException {

		ObjectMapper mapper = new CBORMapper();

		byte[] byteCborTest = decodeHexString(hexCborTest);

		CborMap cmap = CborMap.createFromCborByteArray(byteCborTest);

		MdocCborDto cborDto = new MdocCborDto();

		CborInteger cborStatus = (CborInteger) cmap.get("status");
		cborDto.setStatus(cborStatus.toJavaObject().intValue());

		CborTextString cborVer = (CborTextString) cmap.get("version");
		cborDto.setVersion(cborVer.toJavaObject());

		List<MdocDocument> documentList = new ArrayList<>();

		CborArray carr = (CborArray) cmap.get("documents");
		Iterator<CborObject> iterator = carr.iterator();
		while (iterator.hasNext()) {
			MdocDocument tmpDoc = new MdocDocument();

			CborObject cob = iterator.next();
			CborMap doc = CborMap.createFromCborByteArray(cob.toCborByteArray());
			CborObject docType = doc.get("docType");
			log.info("doctype {}", docType);

			String doctypeString = docType.toString();
			tmpDoc.setDocType(doctypeString.replace("\"", ""));

			IssuerSignedDto tmpIs = new IssuerSignedDto();

			CborMap issSigned = (CborMap) doc.get("issuerSigned");
			log.info("issSigned {}", issSigned);
			CborObject issuerAuth = issSigned.get("issuerAuth");
			log.info("issuerAuth {}", issuerAuth);
			

			COSE.Sign1Message coseMessage = (COSE.Sign1Message) Message
					.DecodeFromBytes(issuerAuth.toCborByteArray(), MessageTag.Sign1);
			log.info("{}", coseMessage);
			byte[] getContent = coseMessage.GetContent();
			CborByteString payload = (CborByteString) CborObject.createFromCborByteArray(getContent);
			log.info("payload {}", payload);
			MobileSecurityObjectPayload mobileSecurityObject = mapper.readValue(payload.toJavaObject(),
					MobileSecurityObjectPayload.class);
			log.info("Payload parsed {}", mobileSecurityObject);
			tmpIs.setIssuerAuth(mobileSecurityObject);

			CborMap nameSpaces = (CborMap) issSigned.get("nameSpaces");
			log.info("namespaces {}", nameSpaces);

			Map<String, List<IssuerSignedItemDto>> nsMap = new LinkedHashMap<>();
			if (nameSpaces.areAllKeysStrings()) {
				Set<Entry<CborObject, CborObject>> entrySet = nameSpaces.entrySet();

				for (Entry<CborObject, CborObject> entry : entrySet) {
					CborObject key = entry.getKey();
					String keyString = key.toString().replace("\"", "");
					nsMap.put(keyString, new ArrayList<>());
					if (entry.getValue() instanceof CborArray arr) {
						Iterator<CborObject> it2 = arr.iterator();
						while (it2.hasNext()) {
							CborByteString next = (CborByteString) (it2.next());
							log.info("byteString {}", next);
							IssuerSignedItemDto readValue2 = mapper.readValue(next.toJavaObject(),
									IssuerSignedItemDto.class);
							log.info("object {}", readValue2);
							List<IssuerSignedItemDto> list = nsMap.get(keyString);
							list.add(readValue2);
						}
					}
				}
			}
			tmpIs.setNameSpaces(nsMap);
			tmpDoc.setIssuerSigned(tmpIs);
			documentList.add(tmpDoc);
		}

		cborDto.setDocuments(documentList);

		log.info("MdocCbor obj {}", cborDto);

		return cborDto;
	}

	private String encodeHexString(byte[] byteArray) {
		StringBuilder hexStringBuffer = new StringBuilder();
		for (int i = 0; i < byteArray.length; i++) {
			hexStringBuffer.append(byteToHex(byteArray[i]));
		}
		return hexStringBuffer.toString();
	}

	private String byteToHex(byte num) {
		char[] hexDigits = new char[2];
		hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		hexDigits[1] = Character.forDigit((num & 0xF), 16);
		return new String(hexDigits);
	}

	public byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	public byte hexToByte(String hexString) {
		int firstDigit = toDigit(hexString.charAt(0));
		int secondDigit = toDigit(hexString.charAt(1));
		return (byte) ((firstDigit << 4) + secondDigit);
	}

	private int toDigit(char hexChar) {
		int digit = Character.digit(hexChar, 16);
		if (digit == -1) {
			throw new IllegalArgumentException("Invalid Hexadecimal Character: " + hexChar);
		}
		return digit;
	}

	// TODO self signed for testing purpose, read from file in config
	public X509Certificate getX509() throws Exception {
		String pemCertificateString = """
				-----BEGIN CERTIFICATE-----
				MIIDzzCCAregAwIBAgIUUWfJ9xUj0wTkKp1lJQysECCcVFowDQYJKoZIhvcNAQEL
				BQAwdzELMAkGA1UEBhMCSVQxDTALBgNVBAgMBFJvbWExDTALBgNVBAcMBFJvbWEx
				DTALBgNVBAoMBElQWlMxFDASBgNVBAMMC3FlYWEtaXNzdWVyMSUwIwYJKoZIhvcN
				AQkBFhZpcHpzc3ZpbHVwcG9AZ21haWwuY29tMB4XDTIzMTExNzA4MzcyMloXDTI0
				MTExNjA4MzcyMlowdzELMAkGA1UEBhMCSVQxDTALBgNVBAgMBFJvbWExDTALBgNV
				BAcMBFJvbWExDTALBgNVBAoMBElQWlMxFDASBgNVBAMMC3FlYWEtaXNzdWVyMSUw
				IwYJKoZIhvcNAQkBFhZpcHpzc3ZpbHVwcG9AZ21haWwuY29tMIIBIjANBgkqhkiG
				9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx40unkdttwPy0/4JneOVWFQGzo0DW4Clsiue
				M6nuIC5nVZUf8cuwjgW6Z8edkKIlFuNWdiFAhgCtEOdzvi5SBY5JB6SNey8aOqaz
				dW3CxrkUgyRGNK9yIHvfleFl1s1d0apOkvzaGoEfC12iDiY8dAwhKyiHoETjIZfh
				FiKYeTtt4pV8/9MOmr5SaEwsTnnj9mpuT3mByfUvFdMQcEmHpa8jQ8xWTU4w1RB+
				QXBXQOFMAFcttg63ZKfyAe7QZv/IV9VG6/oIxDiV05oZZCKwtpVjwYLEe5sWnV5D
				dCg19kfg8jDTF4EA4zBI3igmdK9tTUfe0YMLqyed+mvqnwnRoQIDAQABo1MwUTAd
				BgNVHQ4EFgQU2DOTSQKngGhnOdgY/8EhQNYUsMMwHwYDVR0jBBgwFoAU2DOTSQKn
				gGhnOdgY/8EhQNYUsMMwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOC
				AQEAG0t6onFlGdjp7DTOaROc8qfkPvuM6wZPk5VXewP+Mvv4eQe1M2S5SjX9qHal
				oKsh2oHL3it2GAYlL0HeE5UQHrov7I5v2RKXv7VPLzvhvj/3UDS0OHTYwj0xHwS5
				EnA7Ui6dSSN0tx0PyvBdiSAD7o4VTLB850l49KxzlmRYEDT/DaTw37/ZtSS9CpOY
				OfZ/zAfNa5/ndqlz4bpu3LxhDavkDPoQ2gZ/25b/V/07aYBSlKcpDUAXwrurf/h1
				MoHQ7fE0uRtEaXdW2tqoRTjD4gOnSQVx5KoN/Hk30eHGLefSpjBaVSLddIFA+Tok
				iRgRmjYk5jiMhTzrbnGpEfgzcA==
				-----END CERTIFICATE-----
								""";

		try {
			X509Certificate certificate = convertStringToX509Cert(pemCertificateString);

			return certificate;
		} catch (CertificateException | IOException e) {
			log.error("", e);
			throw e;
		}
	}

	private X509Certificate convertStringToX509Cert(String certificate) throws Exception {
		InputStream targetStream = new ByteArrayInputStream(certificate.getBytes());
		return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(targetStream);
	}
}
