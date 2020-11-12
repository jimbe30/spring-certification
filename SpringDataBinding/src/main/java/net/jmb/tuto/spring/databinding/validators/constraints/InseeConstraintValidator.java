package net.jmb.tuto.spring.databinding.validators.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InseeConstraintValidator implements ConstraintValidator<Insee, String> {
	
	boolean withKey;
	
	@Override
	public void initialize(Insee annotation) {
		this.withKey = annotation.withKey();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {		
		if (withKey) {
			return value == null || value.matches("[12][0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1-9][0-9])[0-9]{8}");
		}
		return value == null || value.matches("[12][0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1-9][0-9])[0-9]{6}[0-9]{0,2}");
	}
}
