package net.jmb.tuto.spring.databinding;

import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.validation.BindingResult;

import net.jmb.tuto.spring.databinding.beans.Person;
import net.jmb.tuto.spring.databinding.formatters.DateFormatter;
import net.jmb.tuto.spring.databinding.services.PersonService;

@Configuration
@ComponentScan()
public class DataBinderValidationApp {
	
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DataBinderValidationApp.class);
	
		PersonService personService = ctx.getBean(PersonService.class);
		
		Map<String, Object> propertyValues = new HashMap<String, Object>() {{
			put("name", "Thomas");
			put("birthDate", "1998-06-21");
			put("adress.city", "Marseille");
			put("adress.postalCode", "1300B");
		}};
		
		// Utilise un DataBinder associé à un ConversionService pour convertir les propriétés en attributs
		Person person = personService.getPersonFromProperties(propertyValues);
		
		// Utilise un DataBinder pour valider les champs et retourner un BindingResult
		BindingResult bindingResult = personService.validatePerson(person);
		System.out.println(bindingResult);
		
		ctx.close();
	}	

	/**
	 * <code>FormattingConversionService</code> est un service Spring qui :
	 * <ul>
	 * <li>Contient un registre de <code>Formatter</code>'s et de <code>Converter</code>'s 
	 * servant à la conversion (réciproque) de Strings en Objects de types ciblés (nombres, dates, ...)
	 * <li>Peut être injecté dans n'importe quel <code>DataBinder</code> pour effectuer la conversion 
	 * de propriétés fournies au format String en attributs du type ciblé
	 * <li>Est utilisé par le framework Spring pour effectuer ces mêmes conversions quand nécessaires
	 * </ul>
	 */
	@Bean
	ConversionService conversionService() {		
		FormattingConversionService formattingConversionService = new FormattingConversionService ();
		formattingConversionService.addFormatter(new DateFormatter("dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "yyyyMMdd"));
		return formattingConversionService;
	}
	
	@Bean 
	PropertyEditor dateEditor() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		CustomDateEditor dateEditor = new CustomDateEditor(dateFormat, true);
		return dateEditor;
	}
	
	@Bean
	MessageSource messageSource() {	
		
		StaticMessageSource messageSource = new StaticMessageSource();		
		Locale locale = Locale.getDefault();
		ApplicationMessages.MESSAGES.forEach(
			(code, msg) -> messageSource.addMessage((String) code, locale, (String) msg)
		);		
		// Mieux si c'est à partir de fichiers de propriétés via un ResourceBundle 
		//		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		//		messageSource.setBasenames("ValidationMessages");

		return messageSource;		
	}
	
	

}
