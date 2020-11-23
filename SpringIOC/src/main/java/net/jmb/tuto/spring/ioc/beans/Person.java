package net.jmb.tuto.spring.ioc.beans;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Person {
	
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	protected String name;
	protected Date birthDate;
	protected Adress adress;
	
	public Person() {
		super();		
	}
	
	public Person(String name, String birthDate, Adress adress) throws ParseException {
		this(name, DATE_FORMAT.parse(birthDate), adress);
	}	
	
	public Person(String name, Date birthDate, Adress adress) {
		super();
		this.name = name;
		this.birthDate = birthDate;
		this.adress = adress;
	}
	
	public Date getBirthDate() {
		return birthDate;
	}
	
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	public void setStrBirthDate(String birthDate) throws ParseException {
		setBirthDate(DATE_FORMAT.parse(birthDate));
	}
	
	public Adress getAdress() {
		return adress;
	}
	
	public void setAdress(Adress adress) {
		this.adress = adress;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Person [" + (name != null ? "name=" + name + ", " : "")
				+ (birthDate != null ? "birthDate=" + birthDate + ", " : "")
				+ (adress != null ? "adress=" + adress : "") + "]";
	}
	
	
	

}
