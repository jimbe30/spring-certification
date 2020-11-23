package net.jmb.tuto.spring.ioc.javaconfig.beans;

import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.jmb.tuto.spring.ioc.beans.Adress;

@Component
public class Person extends net.jmb.tuto.spring.ioc.beans.Person {
	
//	@Autowired(required=false)
//	public Person() {
//		super();		
//	}
	
//	@Autowired(required=false)
	public Person(Adress bean) throws ParseException {
		super("Arthur Shelby", "1892-06-30", bean);
	}	

////	@Autowired(required=false)
//	public Person(String name, String birthDate, Adress bean) throws ParseException {
//		super(name, birthDate, bean);
//	}
//	
////	@Autowired(required=false)	
//	public Person(String name, Date birthDate, Adress bean) {
//		super(name, birthDate, bean);
//	}

	@Override
	public String toString() {
		return "Person [" + (name != null ? "name=" + name + ", " : "")
				+ (birthDate != null ? "birthDate=" + birthDate + ", " : "")
				+ (adress != null ? "adress=" + adress : "") + "]";
	}
	
	
}
