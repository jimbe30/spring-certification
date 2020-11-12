package net.jmb.tuto.spring.databinding;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import net.jmb.tuto.spring.databinding.beans.Adress;
import net.jmb.tuto.spring.databinding.beans.Person;

@Configuration
@ComponentScan()
public class AnnotationValidationApp {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationValidationApp.class);

		Validator validator = ctx.getBean(Validator.class);

		Person person = new Person().setName("Mathieu").setInsee("1981184012191")
			.setAdress(new Adress()	.setCity("Marseille").setPostalCode("1300B"));

		Set<ConstraintViolation<Person>> violations = validator.validate(person);
		violations.forEach(error -> System.out.println(error.getPropertyPath() + ": " + error.getMessage()));

		ctx.close();
	}

	/**
	 * Configuring a Bean Validation Provider for support of the Bean Validation API 
	 * Use the LocalValidatorFactoryBean to configure a default Validator as a Spring bean 
	 * A Bean Validation provider, such as Hibernate Validator, is expected to be present 
	 * in the classpath and is automatically detected.
	 * 
	 * @return
	 */
	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

}
