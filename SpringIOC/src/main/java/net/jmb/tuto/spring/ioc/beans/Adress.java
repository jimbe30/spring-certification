package net.jmb.tuto.spring.ioc.beans;

public class Adress {
	
	private String adressDetail;
	private String zipCode;
	private String city;
	
	public Adress() {
		super();
	}
	
	public Adress(String adressDetail, String zipCode, String city) {
		super();
		this.adressDetail = adressDetail;
		this.zipCode = zipCode;
		this.city = city;
	}
	
	public String getAdressDetail() {
		return adressDetail;
	}
	public void setAdressDetail(String adressDetail) {
		this.adressDetail = adressDetail;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "Adress [adressDetail=" + adressDetail + ", zipCode=" + zipCode + ", city=" + city + "]";
	}

	
	

}
