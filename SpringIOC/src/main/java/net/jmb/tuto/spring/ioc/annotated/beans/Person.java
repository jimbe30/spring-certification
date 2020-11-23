package net.jmb.tuto.spring.ioc.annotated.beans;

import java.text.ParseException;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

public class Person extends net.jmb.tuto.spring.ioc.beans.Person {
	
	@Autowired
	Adress bean;
	
//	@Autowired(required=false)
	public Person() {
		super();		
	}
	
//	@Autowired(required=false)
	public Person(Adress bean) throws ParseException {
		super();
		setAdress(bean);
	}	

//	@Autowired(required=false)
	public Person(String name, String birthDate, Adress bean) throws ParseException {
		super(name, birthDate, bean);
	}
	
//	@Autowired(required=false)	
	public Person(String name, Date birthDate, Adress bean) {
		super(name, birthDate, bean);
	}

	@Override
	public String toString() {
		return "Person [" + (bean != null ? "bean=" + bean + ", " : "") + (name != null ? "name=" + name + ", " : "")
				+ (birthDate != null ? "birthDate=" + birthDate + ", " : "")
				+ (adress != null ? "adress=" + adress : "") + "]";
	}
	
	
}
