package net.jmb.tuto.spring.aop.beans;

public interface OperationsInterface {
	
	double carre(Double nombre);
	double sommeDesCarres(Double... nombres);		
	double carreDeLaSomme(Double... nombres);
}
