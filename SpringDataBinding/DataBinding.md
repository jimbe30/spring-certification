
# Validation, Data Binding, and Type Conversion

Validation should be easy to localize and it should be possible to plug in any available validator.
Spring provides a `Validator` contract usable in every layer of an application.

Data binding let user input be dynamically bound to the domain model.
Spring provides `DataBinder` to do exactly that.

`PropertyEditorSupport` implementations are used to parse and format property values.
Packages can be used as alternatives to `PropertyEditorSupport` implementations:
- `org.springframework.core.convert` that provides a general type conversion
- `org.springframework.format` for formatting UI field values.

## Validator interface : validation

Validation errors are reported to the Errors object passed to the validator.
With Spring Web MVC, use the `<spring:bind/>` tag to inspect the error messages
But you can also inspect the `Errors` object.

```java
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

```

## Resolving Error Codes to Error Messages

Output the error messages with a `MessageSource` using the error code provided when rejecting the field.

```java
	String getMessage(String code, Object[] args, String default, Locale loc);
	String getMessage(String code, Object[] args, Locale loc);
```

- ApplicationContext automatically searches for a `MessageSource` bean in the context.
- The bean must have the name **messageSource**.
	- If such a bean is found in the context or a parent, all calls are delegated to the message source.	
	- If not found any source for messages, an empty DelegatingMessageSource is instantiated.

2 MessageSource implementations of `HierarchicalMessageSource` to do nested messaging:

- `ResourceBundleMessageSource`
- `StaticMessageSource` : rarely used, provides programmatic ways to add messages.

```java
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
```

## Bean manipulation with BeanWrapper

`BeanWrapper` usually is not used by application code directly but is used by the `DataBinder` and the `BeanFactory`.

- Offers functionality to set and get property values, get property descriptors, and query properties to determine if they are readable or writable. 
- Offers support for nested properties to an unlimited depth. 
- Supports the ability to add standard JavaBeans PropertyChangeListeners and VetoableChangeListeners. 
- Provides support for setting indexed properties. 

```java
BeanWrapper company = new BeanWrapperImpl(new Company());
// setting the company name..
company.setPropertyValue("name", "Some Company Inc.");
// ... can also be done like this:
PropertyValue value = new PropertyValue("name", "Some Company Inc.");
company.setPropertyValue(value);
// create the director and tie it to the company:
BeanWrapper jim = new BeanWrapperImpl(new Employee());
jim.setPropertyValue("name", "Jim Stravinsky");
company.setPropertyValue("managingDirector", jim.getWrappedInstance());
// retrieving the salary of the managingDirector through the company
Float salary = (Float) company.getPropertyValue("managingDirector.salary");
```

## Type conversion with PropertyEditor 

[reference](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-beans-conversion)

`PropertyEditor` effects the conversion between a `String` and an `Object`.
This behavior can be achieved by registering custom editors on a `BeanWrapper` or `DataBinder` 

- Setting properties on beans is done by using `PropertyEditor` implementations.
- Parsing HTTP request parameters in Spring’s MVC is done by using `PropertyEditor` implementations that can be binded in all subclasses of the `CommandController`.

```java
@Bean 
	PropertyEditor dateEditor() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		CustomDateEditor dateEditor = new CustomDateEditor(dateFormat, true);
		return dateEditor;
	}
```


## Field conversion with Formatter & FormattingConversionService

Un `formatter` implémente 2 méthodes pour la conversion de types :
- print(T obj) -> String
- parse(String str) -> T

Usages :
- Il peut être utilisé par `DataBinder` ou `BeanWrapper` pour convertir des données entrées au format String en valeur d'un champ d'objet
- Il peut être référencé dans un `ConversionService` instancié au chargement de l'appli pour ensuite être utilisé partout où Spring a besoin d'effectuer des conversions
- L'implémentation `FormattingConversionService` est un service Spring qui : 
	- Contient un registre de `Formatter's` et de `Converter's` servant à la conversion de Strings en Objects
	- Peut être injecté dans n'importe quel  `DataBinder` pour convertir des propriétés entrées au format String en attributs du type ciblé

**Exemple Formatter**

```java
public final class DateFormatter implements Formatter<Date> {

    private String preferredPattern;
    private String[] otherPatterns;

    public DateFormatter(String preferredPattern, String... otherPatterns) {
        this.preferredPattern = preferredPattern;
        this.otherPatterns = otherPatterns;
    }

    public String print(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return getDateFormat(locale).format(date);
    }

    public Date parse(String formatted, Locale locale) throws ParseException {
    	Date result = null;
    	if (formatted.length() > 0) {
        	result = getAllDateFormats(locale).stream()
        		.map(dateFormat -> {
        			try {
        				return dateFormat.parse(formatted);
        			} catch (ParseException e) {
        				return null;
        			}        		
        		})
        		.filter(date -> date != null)
        		.findFirst().get();
        }        
        return result;
    }
```

**Exemple ConversionService** 

```java
	@Bean
	ConversionService conversionService() {		
		FormattingConversionService formattingConversionService = new FormattingConversionService ();
		formattingConversionService.addFormatter(new DateFormatter("dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "yyyyMMdd"));
		return formattingConversionService;
	}
```
 
## Using DataBinder for bean conversion and validation

Un `DataBinder` a plusieurs usages dont les principaux sont : 
- Alimenter les champs d'un objet donné à partir de `PropertyValues` en conjonction avec des `PropertyEditor's`, des `Formatter's` ou un `ConversionService`
- Valider un objet donné et fournir un `BindingResult` contenant les erreurs de validation en conjonction avec des `Validator's`


```java
@Service
public class PersonService {

	@Autowired
	Validator personValidator;

	/**
	 * ConversionService est une alternative à PropertyEditor qui répertorie des Formatter's 
	 * servant à convertir une String en valeur d'attribut et réciproquement
	 */
	@Autowired
	ConversionService conversionService;	
	
	@Autowired
	MessageSource messageSource;

	/**
	 * DataBinder utilisé en conjonction avec PropertyEditor, Formatter ou ConversionService 
	 * pour alimenter les champs d'un objet donné à partir de PropertyValues 
	 */
	public Person getPersonFromProperties(Map<String, Object> properties) {
		
		try {
			Person bean = new Person();
			DataBinder dataBinder = new DataBinder(bean);
			dataBinder.setConversionService(conversionService);
			dataBinder.bind(new MutablePropertyValues().addPropertyValues(properties));
			return bean;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * DataBinder utilisé en conjonction avec Validator pour valider un objet donné
	 * et fournir ensuite un BindingResult contenant les erreurs de validation
	 */
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
}
```

Invocation du DataBinding pour alimenter et valider un bean Person

```java
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
	
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
```

## JavaBean validation with annotations

### Validator Provider

Configuring a Bean Validation Provider for support of the Bean Validation API :
- Use the `LocalValidatorFactoryBean` to configure a default `Validator` as a Spring bean
- A Bean Validation provider, such as Hibernate Validator, is expected to be present in the classpath and is automatically detected.

```java
	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}
```

### Configuring Custom Constraints

Each bean validation constraint consists of two parts:
- An implementation of the `javax.validation.ConstraintValidator` interface that implements the constraint’s behavior.
- A `@Constraint` annotation that declares the constraint and its configurable properties.
	Each @Constraint annotation references the corresponding `ConstraintValidator` class.

The LocalValidatorFactoryBean lets your custom `ConstraintValidators` benefit from dependency injection like any other Spring bean.

```java
// ConstraintValidator

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

// Annotation

@Constraint(validatedBy = InseeConstraintValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Insee { 
	
	boolean withKey() default false;
     
	String message() default "{net.jmb.tuto.spring.databinding.validators.constraints.Insee}";
	
    Class<?>[] groups() default {};     
    Class<? extends Payload>[] payload() default {};      
}
```

> **{net.jmb.tuto.spring.databinding.validators.constraints.Insee}** est la clé du message configuré dans le fichier `ValidationMessages.properties` qui doit être dans le classpath

### Proceeding validation on annotated beans

Beans declare validation constraints on annotated fields.

```java
public class Person {
	
	@Size(min = 2, max = 30, message = "Le nom doit doit comporter entre 2 et 30 caractères")
	private String name;
	
	@NotNull @Min(16) @Max(130)
    private Integer age;
	
	@NotNull @Valid
    private Adress adress;    
    
	@DateTimeFormat(pattern="dd/MM/yyyy")
    @NotNull @Past()
	private Date birthDate;
	
	@Insee(withKey = true)
	private String insee;
}
```

> **@Valid** marks a property, method parameter or method return type for validation cascading. Constraints defined on the object are validated when the property is validated.
> This behavior is applied recursively.

> **@Insee** is our custom constraint

`Validator` injected in Spring context can be invoked to validate the annotated beans  

```java
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotationValidationApp.class);

		Validator validator = ctx.getBean(Validator.class);

		Person person = new Person().setName("Mathieu").setInsee("1980684012191")
			.setAdress(new Adress()	.setCity("Marseille").setPostalCode("1300B"));

		Set<ConstraintViolation<Person>> violations = validator.validate(person);
		violations.forEach(error -> System.out.println(error.getPropertyPath() + ": " + error.getMessage()));

		ctx.close();
	}
```
