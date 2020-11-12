package net.jmb.tuto.spring.databinding.beans;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class Adress {

	@NotBlank
	String street;

	String number;

	@Pattern(regexp = "[0-9]{5}", message = "Le code postal doit être numérique sur 5 positions")
	String postalCode;

	@NotBlank
	String city;

	public String getStreet() {
		return street;
	}

	public String getNumber() {
		return number;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCity() {
		return city;
	}

	public Adress setStreet(String street) {
		this.street = street;
		return this;
	}

	public Adress setNumber(String number) {
		this.number = number;
		return this;
	}

	public Adress setPostalCode(String postalCode) {
		this.postalCode = postalCode;
		return this;
	}

	public Adress setCity(String city) {
		this.city = city;
		return this;
	}

}
