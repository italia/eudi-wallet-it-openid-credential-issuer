package it.ipzs.qeaaissuer.service;

import COSE.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.google.iot.cbor.*;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.upokecenter.cbor.CBORObject;
import it.ipzs.qeaaissuer.dto.*;
import it.ipzs.qeaaissuer.exception.MdocCborX5CGenerationException;
import it.ipzs.qeaaissuer.oidclib.OidcWrapper;
import it.ipzs.qeaaissuer.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

@Service
@Slf4j
@RequiredArgsConstructor
public class MdocCborService {

	private final SRService srService;
	private final OidcWrapper oidcWrapper;

	public String generateMdocCbor(MdocCborDto cborDto, JWK deviceKey)
			throws CborParseException, IOException, CoseException, CborConversionException,
			NoSuchAlgorithmException, ParseException, JOSEException {
		List<String> dateParams = List.of("birthdate", "issue_date", "expiry_date");
		ObjectMapper mapper = new CBORMapper();
		JWK jwk = oidcWrapper.getMdocCredentialIssuerJWK();
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
					tmpItem.put("digestID", CborObject.createFromJavaObject(item.getDigestID()));
					tmpItem.put("elementIdentifier", CborTextString.create(item.getElementIdentifier()));
					tmpItem.put("random", CborByteString.create(srService.generateRandomByte(16)));
					if (dateParams.contains(item.getElementIdentifier())) {
						CborObject date = cborDateFieldGeneration((String) item.getElementValue());
						tmpItem.put("elementValue", date);
					} else if ("driving_privileges".equals(item.getElementIdentifier())) {
						CborArray dpArray = CborArray.create();
						CborMap tmpMap = CborMap.create();
						@SuppressWarnings("unchecked")
						Map<String, String> dpObj = (Map<String, String>) item.getElementValue();
						String vcc = dpObj.get("vehicle_category_code");
						CborObject vccCbor = CborObject.createFromCborByteArray(mapper.writeValueAsBytes(vcc));
						tmpMap.put("vehicle_category_code", vccCbor);
						String d1 = dpObj.get("issue_date");
						CborObject issueDate = cborDateFieldGeneration(d1);
						tmpMap.put("issue_date", issueDate);

						String d2 = dpObj.get("expiry_date");
						CborObject expDate = cborDateFieldGeneration(d2);
						tmpMap.put("expiry_date", expDate);
						dpArray.add(tmpMap);
						tmpItem.put("elementValue", dpArray);

					} else if ("portrait".equals(item.getElementIdentifier())) {
						CborObject portrait = CborByteString
								.create(item.getElementValue().toString().getBytes());
						tmpItem.put("elementValue", portrait);

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

			issuerSignedMap.put("nameSpaces", nameSpacesMap);

			CborMap mobileSecurityObj = CborMap.create(24);
			mobileSecurityObj.put("docType", CborTextString.create("eu.europa.ec.eudiw.pid.1"));
			mobileSecurityObj.put("version", version);
			CborMap validityMap = CborMap.create();
			LocalDateTime currentDateTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			CborObject signedDate = cborDateFieldGeneration(currentDateTime.format(formatter));
			validityMap.put("signed",
					signedDate);
			validityMap.put("validFrom",
					signedDate);
			LocalDateTime expr = currentDateTime.plusYears(1);
			CborObject validUntilDate = cborDateFieldGeneration(expr.format(formatter));
			validityMap.put("validUntil",
					validUntilDate);
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
			OneKey key = new OneKey(jwk.toECKey().toPublicKey(), jwk.toECKey().toPrivateKey());
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

				COSE.Sign1Message co = new COSE.Sign1Message(false);
				co.addAttribute(HeaderKeys.Algorithm, CBORObject.FromObject(-7), Attribute.PROTECTED);
				co.SetContent(payload.byteArrayValue());
				byte[] x5Chain = getX5Chain();
				CborByteString x5chain = CborByteString.create(x5Chain, 0,
						x5Chain.length);
				
				co.addAttribute(CBORObject.FromObject(33),
						CBORObject.FromObject(x5chain.byteArrayValue()),
						Attribute.UNPROTECTED);
				
				co.sign(key);
				CborObject issuerAuthObj = CborObject.createFromCborByteArray(co.EncodeToBytes());
				issuerSignedMap.put("issuerAuth", issuerAuthObj);
			}
			 catch (Exception e1) {
					log.error("issuerAuth COSE object creation failed", e1);
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

	private CborObject cborDateFieldGeneration(String dateString) throws CborConversionException {

		CborObject t = CborObject.createFromJavaObject(dateString);
		String ts = t.toString().replace("\"", "");
		CborObject date = CborTextString.create(ts.getBytes(), 0, ts.getBytes().length, 1004);
		return date;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MdocCborDto parseMdocCbor(String hexCborTest)
			throws IOException, CborParseException, CoseException {

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
			CborArray issuerAuth = (CborArray) issSigned.get("issuerAuth");
			log.info("issuerAuth {}", issuerAuth);
			
			COSE.Sign1Message coseMessage = (COSE.Sign1Message) Message.DecodeFromBytes(issuerAuth.toCborByteArray(),
					MessageTag.Sign1);
			log.info("{}", coseMessage);
			byte[] getContent = coseMessage.GetContent();
			CborMap payload = (CborMap) CborObject.createFromCborByteArray(getContent);
			log.info("payload {}", payload);
			MobileSecurityObjectPayload mobileSecurityObject = mapper.readValue(payload.toCborByteArray(),
					MobileSecurityObjectPayload.class);
			log.info("Payload parsed {}", mobileSecurityObject);
			tmpIs.setIssuerAuth(mobileSecurityObject);

			CborMap nameSpaces = (CborMap) issSigned.get("nameSpaces");

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
							if (readValue2.getElementValue().toString().startsWith("[")
									&& !readValue2.getElementIdentifier().equals("driving_privileges")) {
								ObjectMapper mp = new ObjectMapper();
								String writeValueAsString = mp.writeValueAsString(readValue2.getElementValue());
								readValue2.setElementValue(writeValueAsString);
							} else if (readValue2.getElementIdentifier().equals("driving_privileges")) {
								ObjectMapper mp = new ObjectMapper();

								if (readValue2.getElementValue() instanceof List e) {
									Object map = e.get(0);
									if (map instanceof Map m) {
										Object idObj = m.get("issue_date");
										Object edObj = m.get("expiry_date");

										String writeValueAsString = mp.writeValueAsString(idObj);
										writeValueAsString = writeValueAsString.replace("\"", "");
										m.put("issue_date", writeValueAsString);

										writeValueAsString = mp.writeValueAsString(edObj);
										writeValueAsString = writeValueAsString.replace("\"", "");
										m.put("expiry_date", writeValueAsString);
									}
								}

							}
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

	public byte[] getX5Chain() throws Exception {

		String chainString = oidcWrapper.getCredentialIssuerMdocX5Chain();

		if (StringUtil.isBlank(chainString)) {
			log.error("Error in X5Chain retrieval");
			throw new MdocCborX5CGenerationException("Error in X5Chain retrieval");
		} else {
			return chainString.getBytes();
		}
	}

}
