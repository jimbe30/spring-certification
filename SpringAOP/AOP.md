
> Objectif : améliorer la structuration du code afin de faciliter son écriture et sa maintenance.

Le développement orienté aspects applique principe de séparation des préoccupations à des traitements transverses.
Souvent de bas niveau (techniques ou architectaux).

Principaux traitements pris en charge :
- monitoring (log),
- habilitations,
- gestion de transactions

Externalisation des traitements transverses dans des entités nommées aspect
Regroupement du code dans des greffons insérés à la compilation ou à l'exécution.

## Concepts

- Aspect : encapsule une fonctionnalité transverse et se compose d'un ou plusieurs points de coupe et greffons
- Greffon (advice) : contient les traitements techniques qui seront insérés à des jointcuts et exécutés
- Point de coupe (pointcut) : ou point de greffe, endroit où le greffon sera invoqué lors du tissage. C'est un prédicat faisant correspondre un sous ensemble de points de jonctions.
- Point de jonction (joinpoint) : ou point d'exécution, endroit où il est possible d'invoquer un greffon dans le flot de traitement des composants (exemple : méthode, constructeur, attribut, ...). L'AOP propose un ensemble bien défini de points d'exécution utilisables.
- Tissage (weaving) : insertion des aspects à la compilation ou à l'exécution
- Tisseur (weaver) : outil qui réalise le tissage des aspects, responsable de la mise en oeuvre de l'AOP
- Inter-type declarations : permet de déclarer de nouveau membres dans une classe

## Mise en oeuvre

Le tissage insère du code à des points d'exécution de l'application.

Peut être réalisé de trois manières :
- A la compilation : Cette compilation peut se faire au niveau du code source ou au niveau du bytecode. Dans ce dernier cas, il est possible de tisser n'importe quelle application sans avoir son code source.
- Au chargement : lors du chargement de la classe avec un classloader dédié.
- A l'exécution : en utilisant des mécanismes reposant sur des proxys ou des interceptions.

## Spring AOP

**Target object:** 
An object being advised by one or more aspects.
Also referred to as the “advised object”. 
Since Spring AOP is implemented by using runtime proxies, this object is always a proxied object.

**AOP proxy:** 
An object created in order to implement the aspect contracts. 
AOP proxy is a JDK dynamic proxy or a CGLIB proxy.

**Weaving:** 
Linking aspects with other application types or objects to create an advised object. 
Spring AOP performs weaving at runtime.

### Pointcut Designators

Spring AOP supports the following AspectJ pointcut designators (PCD) for use in pointcut expressions:
- `execution`: For matching method execution join points. This is the primary pointcut designator to use when working with Spring AOP.
- `within`: Limits matching to join points within certain types.
- `this`: Limits matching to join points where the referenced bean being proxied is an instance of the given type (not matches subclasses).
- `target`: Limits matching to join points where the object being proxied is an instance of the given type (matches subclasses).
- `args`: Limits matching to join points where the arguments are instances of the given types.

### Combining Pointcut Expressions

By using &&, || and ! and refer to pointcut expressions by name.

```java
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {} // matches if a join point represents the execution of any public method.

@Pointcut("within(com.xyz.myapp.trading..*)") 
private void inTrading() {} 	// matches if a method execution is in the trading package

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {} // matches if a join point represents any public method in the trading package
```

### Expression format

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)
```

- Returning type pattern * is most frequently used. It matches any return type. 
- The name pattern matches the method name. Use the * wildcard as all or part of a name pattern.
- If you specify a declaring type pattern, include a trailing . to join it to the name pattern. 
- The parameters pattern is slightly more complex: 
	- () matches a method that takes no parameters, 
	- (..) matches any number (zero or more) of parameters. 
	- (*) pattern matches a method that takes one parameter of any type. 
	- (*,String) matches a method that takes two parameters. The first can be of any type, while the second must be a String


### Declaring Advice

Advice is associated with a pointcut expression and runs before, after, or around method executions matched by the pointcut.
The pointcut expression may be either a simple reference to a named pointcut or a pointcut expression declared in place.

Exemple

```java

@Aspect
public class AdviceExample {

    @Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doAccessCheckPointCutReference() {
        // ...
    }
    
    @Before("execution(* com.xyz.myapp.dao.*.*(..))")
    public void doAccessCheckPoincutDefinition() {
        // ...
    }
    
    @AfterReturning(
        pointcut="com.xyz.myapp.CommonPointcuts.dataAccessOperation()",
        returning="retVal")
    public void doAccessCheckAfter(Object retVal) {
        // The returning attribute must correspond to the name of a 
        //parameter. 
        // The return value is passed to the advice method argument. 
        // A returning clause also restricts matching to methods that
        // return a value of the specified type.
    }
    
    @AfterThrowing(
        pointcut="com.xyz.myapp.CommonPointcuts.dataAccessOperation()",
        throwing="ex")
    public void doRecoveryActions(DataAccessException ex) {
        // The throwing attribute must correspond to the name of a 
        // parameter. 
        // When a method exits by throwing an exception, the exception is
        // passed to the advice
		  // A throwing clause restricts matching to only method that throw 
		  //an exception of the specified type.
    }
    
    @Around(value = "execution(* net.jmb..Oper*.*(..))")
    public Object wrapMethodExecution(ProceedingJoinPoint pjp) throws Throwable {
			System.out.println("\nJoinPoint: " + pjp);
			System.out.println("Args: " + Arrays.deepToString(pjp.getArgs()));
			Object proceed = pjp.proceed();
			System.out.println("proceed() result: " + proceed);
			return proceed;
		}
}

```
