
### Container, Dependency and IOC

- What is dependency injection and what are the advantages of using it?

- What is an interface and what are the advantages of making use of them in Java?

- What is an ApplicationContext?

- How are you going to create a new instance of an ApplicationContext?

- Can you describe the lifecycle of a Spring Bean in an ApplicationContext?

- How are you going to create an ApplicationContext in an integration test?

- What is the preferred way to close an application context? Does Spring Boot do this for you?

- Can you describe:
	- Dependency injection using Java configuration?
	- Dependency injection using annotations (@Autowired)?
	- Component scanning, Stereotypes?
	- Scopes for Spring beans? What is the default scope?

- Are beans lazily or eagerly instantiated by default? How do you alter this behavior?

- What is a property source? How would you use @PropertySource?

- What is a BeanFactoryPostProcessor and what is it used for? When is it invoked?
	- Why would you define a static @Bean method when creating your own BeanFactoryPostProcessor?
	- What is a PropertySourcesPlaceholderConfigurer used for?

- What is a BeanPostProcessor and how is it different to a BeanFactoryPostProcessor? What do they do? When are they called?
	- What is an initialization method and how is it declared on a Spring bean?
	- What is a destroy method, how is it declared and when is it called?
	- Consider how you enable JSR-250 annotations like @PostConstruct and @PreDestroy? When/how will they get called?
	- How else can you define an initialization or destruction method for a Spring bean?

- What does component-scanning do?

- What is the behavior of the annotation @Autowired with regards to field injection, constructor injection and method injection?

- How does the @Qualifier annotation complement the use of @Autowired?

- What is a proxy object and what are the two different types of proxies Spring can create?
	- What are the limitations of these proxies (per type)?
	- What is the power of a proxy object and where are the disadvantages?

- What does the @Bean annotation do?

- What is the default bean id if you only use @Bean? How can you override this?

- Why are you not allowed to annotate a final class with @Configuration
	- How do @Configuration annotated classes support singleton beans?
	- Why can’t @Bean methods be final either?

- How do you configure profiles? What are possible use cases where they might be useful?

- Can you use @Bean together with @Profile?

- Can you use @Component together with @Profile?

- How many profiles can you have?

- How do you inject scalar/literal values into Spring beans?
	- What is @Value used for?

- What is Spring Expression Language (SpEL for short)?

- What is the Environment abstraction in Spring?

- Where can properties in the environment come from 
	- there are many sources for properties 
	- check the documentation if not sure. Spring Boot adds even more.

- What can you reference using SpEL?

- What is the difference between $ and # in @Value expressions?

	> ${…​} reads a property value : placeholders are resolved against JVM system properties at runtime
	> #{...} calculates a value as the result of running an expression: 
	> - Manipulate spring beans and variables (an expression resolver looks for bean names), 
	> - Supports operators
	> - Returns a calculated result or assign the value to @Value variable.
	> - Can use `${key_name}` as expression : `#{'${jdbc.user.name}'}`


### Aspect-Oriented Programming (AOP)

- What is the concept of AOP? Which problem does it solve? What is a cross cutting concern?
	- Name three typical cross cutting concerns.
	- What two problems arise if you don’t solve a cross cutting concern via AOP?

- What is a pointcut, a join point, an advice, an aspect, weaving?

- How does Spring implement a cross cutting concern?

	> JDK dynamic proxy built into the JDK or CGLIB proxy, common open-source class definition library (repackaged into `spring-core`)
	> - If the target object timplements at least one interface, a JDK dynamic proxy is used. All of the interfaces implemented by the target type are proxied. 
	> - If the target object does not implement any interfaces, a CGLIB proxy is created.
	
- Which are the limitations of the two proxy-types?
	- What visibility must Spring bean methods have to be proxied using Spring AOP?

- How many advice types does Spring support? Can you name each one?
	- What are they used for?
	- Which two advices can you use if you would like to try and catch exceptions?

	> -   Before : runs before a join point, does not have the ability to prevent execution flow unless it throws an exception.
	> -   After returning advice: runs after a join point completes normally without throwing an exception.
	> -   After throwing advice: executed if a method exits by throwing an exception.
	> -   After (finally) advice: executed regardless of the method exits (normal or exceptional return).
	> -   Around advice: surrounds a method invocation. 
		Perform custom behavior before and after the method invocation.
		Responsible whether to proceed or to shortcut the advised method execution by returning its own return value or throwing an exception.

- If shown pointcut expressions, would you understand them?
	- For example, in the course we matched getter methods on Spring Beans, what would be the correct pointcut expression to match both getter and setter methods?

- What is the JoinPoint argument used for?

- What is a ProceedingJoinPoint? Which advice type is it used with?


### Data Management: JDBC, Transactions

- What is the difference between checked and unchecked exceptions?
	- Why does Spring prefer unchecked exceptions?
	- What is the Spring data access exception hierarchy?

- How do you configure a DataSource in Spring?

- What is the Template design pattern and what is the JDBC template?

- What is a callback? What are the JdbcTemplate callback interfaces that can be used with queries? What is each used for? (You would not have to remember the interface names in the exam, but you should know what they do if you see them in a code sample).

- Can you execute a plain SQL statement with the JDBC template?

- When does the JDBC template acquire (and release) a connection, for every method called or once per template? Why?

- How does the JdbcTemplate support queries? How does it return objects and lists/maps of objects?

- What is a transaction? What is the difference between a local and a global transaction?

- Is a transaction a cross cutting concern? How is it implemented by Spring?

- How are you going to define a transaction in Spring?
	- What does @Transactional do? What is the PlatformTransactionManager?

- Is the JDBC template able to participate in an existing transaction?

- What is @EnableTransactionManagement for?

- How does transaction propagation work?

- What happens if one @Transactional annotated method is calling another @Transactional annotated method inside a same object instance?

- Where can the @Transactional annotation be used? What is a typical usage if you put it at class level?

- What does declarative transaction management mean?

- What is the default rollback policy? How can you override it?

- What is the default rollback policy in a JUnit test, when you use the @RunWith(SpringJUnit4ClassRunner.class) in JUnit 4 or @ExtendWith(SpringExtension.class) in JUnit 5, and annotate your @Test annotated method with @Transactional?

- Are you able to participate in a given transaction in Spring while working with JPA?

- Which PlatformTransactionManager(s) can you use with JPA?

- What do you have to configure to use JPA with Spring? How does Spring Boot make this easier?


### Spring Data JPA

- What is a Spring Data Repository interface?

- How do you define a Spring Data Repository interface? Why is it an interface not a class?

- What is the naming convention for finder methods in a Spring Data Repository interface?

- How are Spring Data repositories implemented by Spring at runtime?

- What is @Query used for?


### Spring MVC and The Web Layer

- What is the @Controller annotation used for?

- How is an incoming request mapped to a controller and mapped to a method?

- What is the difference between @RequestMapping and @GetMapping?

- What is @RequestParam used for?

- What are the differences between @RequestParam and @PathVariable?

- What are the ready-to-use argument types you can use in a controller method?

- What are some of the valid return types of a controller method?


### REST

- What does REST stand for?

- What is a resource?

- Is REST secure? What can you do to secure it?

- Is REST scalable and/or interoperable?

- Which HTTP methods does REST use?

- What is an HttpMessageConverter?

- Is @Controller a stereotype? Is @RestController a stereotype?
	- What is a stereotype annotation? What does that mean?

- What is the difference between @Controller and @RestController?

- When do you need to use @ResponseBody?

- What are the HTTP status return codes for a successful GET, POST, PUT or DELETE operation?

- When do you need to use @ResponseStatus?

- Where do you need to use @ResponseBody? What about @RequestBody?

- If you saw example Controller code, would you understand what it is doing? Could you tell if it was annotated correctly?

- What Spring Boot starter would you use for a Spring REST application?

- If you saw an example using RestTemplate, would you understand what it is doing?


### Security

- What are authentication and authorization? Which must come first?

- Is security a cross cutting concern? How is it implemented internally?

- What is the delegating filter proxy?

- What is the security filter chain?

- What is a security context?

- What does the ** pattern in an antMatcher or mvcMatcher do?

- Why is the usage of mvcMatcher recommended over antMatcher?

- Does Spring Security support password encoding?

- Why do you need method security? What type of object is typically secured at the method level (think of its purpose not its Java type).

- What do @PreAuthorized and @RolesAllowed do? What is the difference between them?

- How are these annotations implemented?

- In which security annotation, are you allowed to use SpEL?


### Testing

- What type of tests typically use Spring?

- How can you create a shared application context in a JUnit integration test?

- When and where do you use @Transactional in testing?

- How are mock frameworks such as Mockito or EasyMock used?

- How is @ContextConfiguration used?

- How does Spring Boot simplify writing tests?

- What does @SpringBootTest do? How does it interact with @SpringBootApplication and
@SpringBootConfiguration?
Spring Boot Intro

- What is Spring Boot?

- What are the advantages of using Spring Boot?

- What things affect what Spring Boot sets up?

- What is a Spring Boot starter? Why is it useful?

- Spring Boot supports both properties and YML files. Would you recognize and
understand them if you saw them?

- Can you control logging with Spring Boot? How?

- Where does Spring Boot look for application.properties file by default?

- How do you define profile specific property files?

- How do you access the properties defined in the property files?

- What properties do you have to define in order to configure external MySQL?

- How do you configure default schema and initial data?

- What is a fat jar? How is it different from the original jar?

- What embedded containers does Spring Boot support? 
CERTIFICATION STUDY GUIDE | 1 0
Spring Professional Certification Study Guide
Spring Boot Auto Configuration

- How does Spring Boot know what to configure?

- What does @EnableAutoConfiguration do?

- What does @SpringBootApplication do?

- Does Spring Boot do component scanning? Where does it look by default?

- How are DataSource and JdbcTemplate auto-configured?

- What is spring.factories file for?

- How do you customize Spring Boot auto configuration?

- What are the examples of @Conditional annotations? How are they used?
Spring Boot Actuator

- What value does Spring Boot Actuator provide?

- What are the two protocols you can use to access actuator endpoints?

- What are the actuator endpoints that are provided out of the box?

- What is info endpoint for? How do you supply data?

- How do you change logging level of a package using loggers endpoint?

- How do you access an endpoint using a tag?

- What is metrics for?

- How do you create a custom metric?

- What is Health Indicator?

- What are the Health Indicators that are provided out of the box?

- What is the Health Indicator status?

- What are the Health Indicator statuses that are provided out of the box?

- How do you change the Health Indicator status severity order?

- Why do you want to leverage 3rd-party external monitoring system?
Spring Boot Testing

- When do you want to use @SpringBootTest annotation?

- What does @SpringBootTest auto-configure?

- What dependencies does spring-boot-starter-test brings to the classpath?

- How do you perform integration testing with @SpringBootTest for a web application?

- When do you want to use @WebMvcTest? What does it auto-configure?

- What are the differences between @MockBean and @Mock?

- When do you want @DataJpaTest for? What does it auto-configure?
