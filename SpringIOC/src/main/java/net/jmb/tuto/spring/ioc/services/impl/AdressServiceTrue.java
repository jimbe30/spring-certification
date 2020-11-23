package net.jmb.tuto.spring.ioc.services.impl;

import net.jmb.tuto.spring.ioc.beans.Adress;
import net.jmb.tuto.spring.ioc.services.IAdressService;

public class AdressServiceTrue implements IAdressService {

	@Override
	public boolean validateAdress(Adress adress) {
		// TODO Auto-generated method stub
		return true;
	}

}
