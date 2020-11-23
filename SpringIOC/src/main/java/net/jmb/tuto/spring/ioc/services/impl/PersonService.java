package net.jmb.tuto.spring.ioc.services.impl;

import net.jmb.tuto.spring.ioc.beans.Person;
import net.jmb.tuto.spring.ioc.services.IAdressService;
import net.jmb.tuto.spring.ioc.services.IPersonService;

public class PersonService implements IPersonService {
	
	IAdressService adressService;
	
	public PersonService(IAdressService adressService) {
		super();
		this.adressService = adressService;
	}

	@Override
	public boolean validatePerson(Person person) {
		return person.getBirthDate() != null && adressService.validateAdress(person.getAdress());
	}

}
