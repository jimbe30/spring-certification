package net.jmb.tuto.spring.aop.beans;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class Operations implements OperationsInterface {
	
	@Value("classpath:application.properties")
	Resource applicationProperties;
	
	protected void showResource() throws IOException {
		System.out.println("applicationProperties: " + applicationProperties);
		System.out.println(applicationProperties.getFile().getAbsolutePath());
	}
	
	public double carre(Double nombre) {		
		try {
			showResource();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nombre * nombre;
	}		
	public double sommeDesCarres(Double... nombres) {
		return Arrays.stream(nombres).reduce((sum, nombre) -> sum + nombre * nombre).get();
	}		
	public double carreDeLaSomme(Double... nombres) {
		return Math.pow(Arrays.stream(nombres).reduce((sum, nombre) -> sum + nombre).get(), 2);
	}
}
