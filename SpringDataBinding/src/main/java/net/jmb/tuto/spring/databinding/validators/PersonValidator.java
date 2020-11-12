package net.jmb.tuto.spring.databinding.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import net.jmb.tuto.spring.databinding.ApplicationMessages;
import net.jmb.tuto.spring.databinding.beans.Person;

@Component
public class PersonValidator implements Validator, ApplicationMessages {
	
	@Autowired
	Validator adressValidator;

	@Override
	public boolean supports(Class<?> clazz) {
		return Person.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "name", NAME_EMPTY); 
		Person person = (Person) target;
		if (person.getAge() != null && person.getAge() <= 0) {
			errors.rejectValue("age", NEGATIVE_VALUE);
		} else if (person.getAge() != null && person.getAge() > 110) {
			errors.rejectValue("age", TOO_OLD);
		}
		
		ValidationUtils.rejectIfEmpty(errors, "adress", FIELD_REQUIRED); 
		
		// Validation de l'adresse avec ajout d'un préfixe "adress." au champ en erreur
		if (person.getAdress() != null) {
            errors.pushNestedPath("adress");
            ValidationUtils.invokeValidator(this.adressValidator, person.getAdress(), errors);
            errors.popNestedPath();
        } 
	}
}
