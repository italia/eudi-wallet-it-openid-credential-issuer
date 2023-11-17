package it.ipzs.qeaaissuer.dto;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class VerifiedClaims implements Serializable {

	private static final long serialVersionUID = -8031068776114743283L;

	private Map<String, Object> claims;

	private Map<String, Object> verification;
}
