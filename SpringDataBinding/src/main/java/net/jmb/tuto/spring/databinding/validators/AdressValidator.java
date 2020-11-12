package net.jmb.tuto.spring.databinding.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import net.jmb.tuto.spring.databinding.ApplicationMessages;
import net.jmb.tuto.spring.databinding.beans.Adress;

@Component
public class AdressValidator implements Validator, ApplicationMessages {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Adress.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "street", FIELD_REQUIRED);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "number", FIELD_REQUIRED);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "postalCode", FIELD_REQUIRED);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "city", FIELD_REQUIRED);
        
        Adress adress = (Adress) target;
        if (adress.getPostalCode() != null && !adress.getPostalCode().matches("[0-9]{5}")) {
        	errors.rejectValue("postalCode", NUMBER_FORMAT, new Integer[] {5}, null);
        }

		
	}

}
