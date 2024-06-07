package it.ipzs.qeaaissuer.dto;

public class CedDto {

	private String cognome;
	private String nome;
	private String dataNascita;
	private String scadenzaCarta;
	private String serialeCarta;
	private Integer dirittoAccompangatore;
	private String resultCode;
	private String resultDesc;

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(String dataNascita) {
		this.dataNascita = dataNascita;
	}

	public String getScadenzaCarta() {
		return scadenzaCarta;
	}

	public void setScadenzaCarta(String scadenzaCarta) {
		this.scadenzaCarta = scadenzaCarta;
	}

	public String getSerialeCarta() {
		return serialeCarta;
	}

	public void setSerialeCarta(String serialeCarta) {
		this.serialeCarta = serialeCarta;
	}

	public Integer getDirittoAccompangatore() {
		return dirittoAccompangatore;
	}

	public void setDirittoAccompangatore(Integer dirittoAccompangatore) {
		this.dirittoAccompangatore = dirittoAccompangatore;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CedDto [cognome=");
		builder.append(cognome);
		builder.append(", nome=");
		builder.append(nome);
		builder.append(", dataNascita=");
		builder.append(dataNascita);
		builder.append(", scadenzaCarta=");
		builder.append(scadenzaCarta);
		builder.append(", serialeCarta=");
		builder.append(serialeCarta);
		builder.append(", dirittoAccompangatore=");
		builder.append(dirittoAccompangatore);
		builder.append(", resultCode=");
		builder.append(resultCode);
		builder.append(", resultDesc=");
		builder.append(resultDesc);
		builder.append("]");
		return builder.toString();
	}
}
