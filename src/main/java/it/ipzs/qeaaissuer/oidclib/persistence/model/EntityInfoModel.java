package it.ipzs.qeaaissuer.oidclib.persistence.model;

import java.time.LocalDateTime;

import it.ipzs.qeaaissuer.oidclib.model.CachedEntityInfo;
import it.ipzs.qeaaissuer.oidclib.util.GetterUtil;
import it.ipzs.qeaaissuer.oidclib.util.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
	name = "fetched_entity_statement",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"iss", "sub"})
	}
)
public class EntityInfoModel {

	public static EntityInfoModel of(CachedEntityInfo source) {
		EntityInfoModel target = new EntityInfoModel();

		target.setId(source.getStorageId());
		target.setCreated(source.getCreateDate());
		target.setModified(source.getModifiedDate());
		target.setExp(source.getExpiresOn());
		target.setIat(source.getIssuedAt());
		target.setIss(source.getIssuer());
		target.setSub(source.getSubject());
		target.setJwt(source.getJwt());
		target.setStatement(source.getStatement());

		return target;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public LocalDateTime getModified() {
		return modified;
	}

	public String getIss() {
		return iss;
	}

	public String getSub() {
		return sub;
	}

	public LocalDateTime getExp() {
		return exp;
	}

	public LocalDateTime getIat() {
		return iat;
	}

	public String getStatement() {
		return statement;
	}

	public String getJwt() {
		return jwt;
	}

	public boolean isExpired() {
		return exp.isBefore(LocalDateTime.now());
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public void setModified(LocalDateTime modified) {
		this.modified = modified;
	}

	public void setExp(LocalDateTime exp) {
		this.exp = exp;
	}

	public void setIat(LocalDateTime iat) {
		this.iat = iat;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	public CachedEntityInfo toCachedEntityInfo() {
		CachedEntityInfo target = new CachedEntityInfo();

		target.setStorageId(getStorageId());
		target.setCreateDate(getCreated());
		target.setModifiedDate(getModified());
		target.setIssuer(getIss());
		target.setSubject(getSub());
		target.setExpiresOn(getExp());
		target.setIssuedAt(getIat());
		target.setJwt(getJwt());
		target.setStatement(getStatement());

		return target;
	}

	protected void setId(String storageId) {
		if (!Validator.isNullOrEmpty(storageId)) {
			setId(GetterUtil.getLong(storageId));
		}
	}

	private String getStorageId() {
		if (id > 0) {
			return String.valueOf(id);
		}

		return null;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime created;

	@Column(nullable = false)
	private LocalDateTime modified;

	@Column(nullable = false)
	private String iss;

	@Column(nullable = false)
	private String sub;

	@Column(nullable = false)
	private LocalDateTime exp;

	@Column(nullable = false)
	private LocalDateTime iat;

	@Column(nullable = false)
	private String statement;

	@Column(nullable = false)
	private String jwt;


}
