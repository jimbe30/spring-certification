package net.jmb.tuto.spring.databinding.validators.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = InseeConstraintValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Insee { 
	
	boolean withKey() default false;
     
	String message() default "{net.jmb.tuto.spring.databinding.validators.constraints.Insee}";
	
    Class<?>[] groups() default {};     
    Class<? extends Payload>[] payload() default {};      
}

