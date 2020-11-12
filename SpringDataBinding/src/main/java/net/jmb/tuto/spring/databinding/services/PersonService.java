package net.jmb.tuto.spring.databinding.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

import net.jmb.tuto.spring.databinding.beans.Person;

@Service
public class PersonService {

	@Autowired
	Validator personValidator;

	/**
	 * <code>PropertyEditor</code> sert à convertir une String en valeur 
	 * d'un attribut en fonction du type de ce dernier
	 */
//	@Autowired
//	PropertyEditor dateEditor;

	/**
	 * <code>FormattingConversionService</code> est une alternative efficace à <code>PropertyEditor</code>
	 * qui répertorie des <code>Formatter</Code>'s servant à convertir réciproquement une String en valeur 
	 * d'attribut
	 */
	@Autowired
	ConversionService conversionService;	
	
	@Autowired
	MessageSource messageSource;

	/**
	 * <code>DataBinder</code> est utilisé en conjonction avec des <code>PropertyEditor</code>'s
	 * ou avec des <code>Formatter</code>'s pour alimenter un objet donné à partir des <code>PropertyValues</code> 
	 */
	public <T> T getBeanFromProperties(Map<String, Object> properties, Class<T> clss) {
		
		try {
			T bean = clss.newInstance();
			DataBinder dataBinder = new DataBinder(bean);
//			dataBinder.registerCustomEditor(Date.class, dateEditor);
			dataBinder.setConversionService(conversionService);
			dataBinder.bind(new MutablePropertyValues().addPropertyValues(properties));
			return bean;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public BindingResult validatePerson(Person person) {
		DataBinder dataBinder = new DataBinder(person);
		dataBinder.addValidators(personValidator);
		dataBinder.validate();
		BindingResult bindingResult = dataBinder.getBindingResult();

		bindingResult.getFieldErrors().forEach(error -> {
			List<Object> params = new ArrayList<Object>(Collections.singletonList(error.getField()));
			if (error.getArguments() != null) {
				params.addAll(Arrays.asList(error.getArguments()));
			}
			System.out.println(messageSource.getMessage(error.getCode(), params.toArray(), Locale.getDefault()));
		});
		return bindingResult;
	}

	public Person getPersonFromProperties(Map<String, Object> propertyValues) {
		return getBeanFromProperties(propertyValues, Person.class);
	}

}
