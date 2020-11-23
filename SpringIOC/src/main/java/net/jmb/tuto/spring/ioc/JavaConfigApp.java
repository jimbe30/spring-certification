package net.jmb.tuto.spring.ioc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import net.jmb.tuto.spring.ioc.beans.Person;
import net.jmb.tuto.spring.ioc.javaconfig.beans.Adress;
import net.jmb.tuto.spring.ioc.services.IPersonService;
import net.jmb.tuto.spring.ioc.services.impl.AdressServiceFalse;
import net.jmb.tuto.spring.ioc.services.impl.AdressServiceTrue;
import net.jmb.tuto.spring.ioc.services.impl.PersonService;
import net.jmb.tuto.spring.ioc.services.impl.PersonService2;

@Configuration
@ComponentScan(basePackages = "net.jmb.tuto.spring.ioc.javaconfig")
@Import(value = {})
public class JavaConfigApp {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(JavaConfigApp.class);
		Person bean = ctx.getBean(Person.class);
		System.out.println(bean);

		IPersonService service = ctx.getBean(IPersonService.class);
		System.out.println(service.validatePerson(bean));

//		service = ctx.getBean(PersonService2.class);
//		System.out.println(service.validatePerson(bean));
	}

	@Bean()
	@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public Adress adress() {
		return new Adress("73, traverse Régny", "13009", "Marseille");
	}

	@Bean("adressService")
	@Lazy
	AdressServiceTrue adressServiceTrue() {
		AdressServiceTrue adressService = new AdressServiceTrue();
		System.out.println("adressServiceTrue: " + adressService);
		return adressService;
	}

	@Bean
	@Lazy
	AdressServiceFalse adressServiceFalse() {
		AdressServiceFalse adressService = new AdressServiceFalse();
		System.out.println("adressServiceFalse: " + adressService);
		return adressService;
	}
	
	@Bean("personService")
	@Profile(value = "env2")
	public PersonService2 personService2() {
		PersonService2 personService2 = new PersonService2(adressServiceTrue());
		System.out.println("personService2: " + personService2);
		return personService2;
	}

	@Bean("personService")
	public PersonService personService() {
		PersonService personService = new PersonService(adressServiceTrue());
		System.out.println("personService: " + personService);
		return personService;
	}



}
