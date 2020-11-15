# spring-certification
Divers projets contenant les fondamentaux pour préparer la certification Spring Framework


# Conteneur Spring IoC

Le principe d'inversion de contrôle (IoC) est également connu sous le nom d'injection de dépendance (DI). 
Processus par lequel les objets définissent leurs dépendances uniquement via :
- des arguments de constructeur, 
- des arguments d'une méthode de fabrique
- ou des propriétés définies sur l'instance d'objet après sa construction ou son retour d'une méthode de fabrique.

Le conteneur injecte ensuite ces dépendances lorsqu'il crée le bean. 
Ce processus est fondamentalement l'inverse du bean contrôlant lui-même ses dépendances en utilisant l'instanciation directe ou un mécanisme tel que le Service Locator.

Les packages `org.springframework.beans` et `org.springframework.context` constituent la base du conteneur IoC de Spring. 
L'interface `BeanFactory` fournit un mécanisme de configuration avancé capable de gérer tout type d'objet. 
`ApplicationContext` est une sous-interface de `BeanFactory` qui ajoute:
- Une intégration plus facile avec les fonctionnalités AOP de Spring
- Une gestion des ressources de messages (pour une utilisation dans l'internationalisation)
- Une publication d'événements
- Des contextes spécifiques à la couche applicative tels que `WebApplicationContext` à utiliser dans les applications Web.

Dans Spring, les objets gérés par le conteneur Spring IoC sont appelés beans : 
- Un bean est un objet instancié, assemblé et géré par un conteneur Spring IoC. 
- Les beans et leurs dépendances sont spécifiés dans les métadonnées de configuration utilisées par le conteneur.

## Présentation du conteneur

`org.springframework.context.ApplicationContext` représente le conteneur Spring IoC et est responsable d'instancier, de configurer et d'assembler les beans. 

Le conteneur obtient ses instructions sur les objets à instancier, configurer et assembler en lisant les métadonnées de configuration. 
Les métadonnées de configuration sont représentées en XML, annotations Java ou code Java. 
Elles spécifient les objets qui composent l'application et les interdépendances entre ces objets.

Dans les applications autonomes, il est courant de créer une instance de `ClassPathXmlApplicationContext` ou `FileSystemXmlApplicationContext`. 
- XML est le format traditionnel pour définir les métadonnées de configuration, 
- L'utilisation des annotations ou du code Java comme format de métadonnées peut être activée de manière déclarative avec un peu de configuration XML.

Dans la plupart des applications, un code explicite n'est pas nécessaire pour instancier un conteneur Spring IoC. 
Par exemple, dans un scénario d'application Web, quelques lignes de descripteur Web dans le `web.xml` suffisent généralement.

> Fonctionnement de Spring :
> Les classes d'application sont combinées avec les métadonnées de configuration de sorte qu'après l'initialisation de `ApplicationContext`, l'application est entièrement configurée et exécutable.


### Métadonnées de configuration

Les métadonnées de configuration représentent la façon dont on indique au conteneur Spring d'instancier, de configurer et d'assembler les objets dans l'application.

Les métadonnées de configuration sont traditionnellement fournies dans un format XML.
Les autres formes de métadonnées avec le conteneur Spring sont :
- Configuration basée sur les annotations (Spring 2.5).
- Configuration Java (Spring 3.0) : Possibilité de définir des beans externes aux classes d'application en utilisant Java plutôt que des fichiers XML.
	(cf : annotations `@Configuration`, `@Bean`, `@Import` et `@DependsOn`).

Les métadonnées de configuration XML configurent les beans comme des éléments `<bean/>` à l'intérieur d'un élément `<beans/>` de niveau supérieur.
La configuration Java utilise généralement des méthodes annotées `@Bean` dans une classe `@Configuration`.

En général, on définit des objets de service, d'accès aux données (DAO), de présentation tels que Struts Action, d'infrastructure tels que Hibernate SessionFactories, de files d'attente JMS. 
On ne configure pas d'objets de domaine à granularité fine dans le conteneur, car c'est la responsabilité des DAO et de la logique métier de créer ces objets.
L’intégration de Spring avec AspectJ peut cependant configurer des objets créés en dehors du conteneur IoC. 


### Instancier un conteneur

Le ou les chemins d'emplacement fournis à un constructeur `ApplicationContext` permettent au conteneur de charger des métadonnées de configuration à partir de ressources externes, telles que le système de fichiers local, Java CLASSPATH, etc.

```java
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");
```

**Composer des métadonnées de configuration basées sur XML**

Il peut être utile que les définitions de bean s'étendent sur plusieurs fichiers XML.
Souvent, chaque fichier de configuration XML représente une couche ou un module logique dans votre architecture.

Vous pouvez utiliser : 
- soit le constructeur de contexte d'application pour charger des définitions de bean à partir de plusieurs emplacements XML. 
- soit l'élément `<import/>` pour charger des définitions de bean à partir d'autres fichiers. 

```xml
<beans>
    <import resource="services.xml"/>
    <import resource="resources/messageSource.xml"/>
    <import resource="/resources/themeSource.xml"/>

    <bean id="bean1" class="..."/>
    <bean id="bean2" class="..."/>
</beans>
```

Tous les chemins sont relatifs au fichier effectuant l'import.
Une barre oblique principale est ignorée et il est préférable de ne pas l'utiliser du tout. 
Le contenu des fichiers importés y compris l'élément `<beans/>`, doit être conforme au schéma Spring.

> On peut utiliser des emplacements pleinement qualifiés au lieu de chemins relatifs: par exemple, `file:C:/config/services.xml` ou `classpath:/config/services.xml`.
> Cependant, ça revient à associer la configuration de l'application à des emplacements absolus spécifiques.
> Il est préférable de conserver une indirection par exemple, via des espaces réservés "${…}" résolus par rapport aux propriétés système JVM à l'exécution.


### Utilisation du conteneur

`ApplicationContext` est l'interface d'une fabrique capable de maintenir un registre de différents beans et de leurs dépendances. 
En utilisant la méthode `T getBean(String name, Class<T> requiredType)`, on récupère des instances de beans.

`ApplicationContext` permet de lire les définitions de bean et d'y accéder, comme le montre l'exemple suivant:

```java
// créer et configurer des beans
ApplicationContext context = new ClassPathXmlApplicationContext ("services.xml", "daos.xml");

// récupérer l'instance configurée
PetStoreService service = context.getBean ("petStore", PetStoreService.class);

// utilise une instance configurée
List <String> userList = service.getUsernameList ();
```

Une variante flexible est `GenericApplicationContext` en combinaison avec des lecteurs délégués (exemple: `XmlBeanDefinitionReader` pour les fichiers XML):

```java
GenericApplicationContext context = new GenericApplicationContext ();
new XmlBeanDefinitionReader(context).loadBeanDefinitions("services.xml", "daos.xml");
context.refresh ();
```

Vous pouvez mélanger et faire correspondre ces lecteurs délégués sur le même `ApplicationContext`, en lisant des définitions de bean à partir de diverses sources de configuration.

Vous pouvez ensuite utiliser `getBean` pour récupérer des instances de vos beans.
Cependant, idéalement l'application ne devrait faire aucun appel à la méthode `ApplicationContext.getBean()` pour n'avoir aucune dépendance vis-à-vis des API Spring.
Par exemple, l'intégration de Spring avec les frameworks Web fournit une injection de dépendances pour divers composants tels que les contrôleurs, qui permet de déclarer une dépendance sur un bean spécifique via des métadonnées (telles qu'une annotation de câblage automatique).

## Présentation des Beans

Dans le conteneur Spring lui-même, les définitions de beans sont représentées comme des objets `BeanDefinition`, qui contiennent entre autres les métadonnées suivantes:
- Un nom de classe qualifié : généralement, la classe d'implémentation réelle du bean en cours de définition.
- Les éléments comportementaux du bean au sein du conteneur (portée, callbacks de cycle de vie, etc...)
- Les références à d'autres beans appelés collaborateurs ou dépendances.
- Les autres paramètres à définir dans l'objet nouvellement créé (exemple: la taille limite ou le nombre de connexions max dans un bean qui gère un pool de connexions).

Ces métadonnées sont un ensemble de propriétés qui constituent chaque définition de bean.
Le tableau suivant le thème auquel se rattachent ces propriétés:

| Propriété | Thème |
|--|--|
| Class | Instantiating Beans |
| Name | Naming Beans |
| Scope | Bean Scopes |
| Constructor arguments | Dependency Injection |
| Properties | Dependency Injection |
| Autowiring mode | Autowiring Collaborators |
| Lazy initialization mode | Lazy-initialized Beans |
| Initialization method | Initialization Callbacks |
| Destruction method | Destruction Callbacks |


En plus des définitions de bean, `ApplicationContext` permet également l'enregistrement d'objets créés en dehors du conteneur (par les utilisateurs).
Cela se fait en invoquant la méthode `getBeanFactory()` qui renvoie l'implémentation `DefaultListableBeanFactory`.
`DefaultListableBeanFactory` prend en charge cet enregistrement via les méthodes `registerSingleton()` et `registerBeanDefinition()`.
Les métadonnées et singletons fournis manuellement doivent l'être le plus tôt possible, afin que le conteneur puisse les traiter correctement lors des étapes d'introspection.


### Nommer les beans

Chaque bean a un ou plusieurs identifiants qui doivent être uniques dans le conteneur. 
Un bean n'a généralement qu'un seul identifiant. S'il en a plus d'un, les autres sont considérés comme des alias.

Dans les métadonnées de configuration XML: 
- L'attribut `id` permet de spécifier exactement un identifiant. 
- Pour définir d'autres alias, les spécifier dans l'attribut name séparés par une virgule (,), un point-virgule (;) ou un espace blanc.

Si vous ne fournissez pas explicitement de nom ou d'identifiant, le conteneur génère un nom unique pour ce bean. 
Cependant, pour faire référence à un bean par son nom via l'élément `ref` ou une recherche de type Service Locator, vous devez fournir un nom. 
Les motivations pour ne pas fournir de nom sont liées à l'utilisation de beans internes et à l'autowiring de collaborateurs.

**Conventions de dénomination des Bean**

C'est la convention Java standard pour les noms de champs lors de la dénomination des beans. 
Exemples: `accountManager`, `accountService`, `userDao`, `loginController`, etc.

Nommer les beans rend la configuration plus facile à lire et à comprendre. 
De plus, si vous utilisez Spring AOP, cela aide beaucoup pour appliquer des greffons à un ensemble de beans liés par leur nom.

Pour les composants sans nom explicite, Spring génère des noms à partir du chemin de classe: il prend le nom de classe simple et transforme son caractère initial en minuscules. 
Dans le cas spécial où les premier et deuxième caractères sont en majuscules, la casse d'origine est conservée. 

**Aliaser un bean en dehors de la définition du bean**

Dans une définition de bean, on fournit plus d'un nom pour le bean en combinant un nom unique spécifié par l'attribut `id` et un nombre quelconque d'autres noms dans l'attribut `name`.

Cependant, spécifier tous les alias où le bean est réellement défini n'est pas toujours adéquat. Il est parfois souhaitable d'introduire un alias pour un bean défini ailleurs. 
(Cas des grands systèmes où la configuration est répartie entre sous-systèmes, chaque sous-système ayant son propre ensemble de définitions d'objet).
Dans les métadonnées de configuration XML, vous pouvez utiliser l'élément `<alias/>` pour ce faire :

```xml
<alias name="fromName" alias="toName"/>
```

Dans ce cas, un bean dans le même conteneur nommé `fromName` peut également être appelé `toName`.

### Instancier des beans

Une définition de bean est essentiellement une spécification pour créer un ou plusieurs objets. 
Le conteneur examine la spécification du bean lorsqu'il est demandé pour créer ou acquérir l'objet réel.

En configuration XML, on spécifie le type d'objet à instancier dans l'attribut `class` de l'élément `<bean/>`.
Cet attribut (qui, en interne, est une propriété sur une instance de `BeanDefinition`) est généralement obligatoire.
Exceptions: 
- Instanciation à l'aide d'une méthode Instance Factory
- Héritage de définition de bean

Utilisation de la propriété `class`:
- Typiquement, pour spécifier la classe de bean dans le cas où le conteneur instancie directement le bean en appelant son constructeur (équivalent à l'opérateur `new`).
- Pour spécifier la classe contenant la méthode de fabrique statique appelée pour créer l'objet, dans le cas moins courant où le conteneur appelle une méthode de fabrique pour créer le bean.
	Le type d'objet renvoyé par l'appel de la méthode de fabrique statique peut être la même classe ou une autre classe entièrement.

*Noms de classe imbriquée*
Pour configurer une définition de bean relative à une classe imbriquée statique, vous devez utiliser le nom binaire de la classe imbriquée.

Exemple : `com.example.SomeThing$OtherThing` pour une classe `OtherThing` imbriquée dans la classe `SomeThing` du package `com.example`

**Instanciation avec un constructeur**

Pour créer un bean par l'approche constructeur, toutes les classes normales sont utilisables. 
Cependant, selon le type d'IoC utilisé pour ce bean spécifique, on peut avoir besoin d'un constructeur par défaut (vide).

En configuration XML, on spécifie le bean comme suit:

```xml
<bean id="exampleBean" class="examples.ExampleBean"/>
```

**Instanciation avec une méthode de fabrique statique**

Pour définir un bean avec une méthode de fabrique statique, l'attribut `class` spécifie la classe qui contient la méthode de fabrique et l'attribut nommé `factory-method` spécifie le nom de la méthode de fabrique elle-même.
Un cas d'utilisation de ce procédé est d'appeler les usines statiques du code ancien.

La définition ne spécifie pas le type de l'objet retourné

```xml
<bean id="clientService" class="examples.ClientService" factory-method="createInstance"/>
```

**Instanciation à l'aide d'une méthode de fabrique d'instance**

Semblable à l'instanciation via une méthode de fabrique statique, l'instanciation avec une méthode de fabrique d'instance appelle une méthode non statique d'un bean existant du conteneur. 
Pour utiliser ce mécanisme, l'attribut `class` est vide et l'attribut `factory-bean` spécifie le nom d'un bean qui contient la méthode à invoquer pour créer l'objet.

```xml
<!-- le bean usine, qui contient une méthode appelée createInstance () -->
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
    <!-- injecter toutes les dépendances requises par ce bean localisateur -->
</bean>

<!-- le bean à créer via le bean usine -->
<bean id="clientService" factory-bean="serviceLocator" factory-method="createClientServiceInstance"/>
```

**Déterminer le type d'exécution d'un bean**

Le type d'exécution réel d'un bean spécifique n'est pas simple. 
Une classe spécifiée dans la définition du bean est juste une référence de classe initiale qui peut conduire à un type d'exécution différent du bean, ou ne pas être définie du tout dans le cas d'une méthode de fabrique.
De plus, le proxy AOP peut encapsuler une instance de bean avec un proxy basé sur une interface avec une exposition limitée du type réel du bean cible (uniquement ses interfaces implémentées).

Pour connaître le type d'exécution réel d'un bean particulier, appeler `BeanFactory.getType` avec le nom du bean. 


## Dépendances

L'injection de dépendances est le moyen de passer de la définition d'un certain nombre de bean autonomes à une application exécutable où les objets collaborent pour atteindre un objectif ?

### Injection de dépendance

L'injection de dépendances (DI) est un processus par lequel les objets définissent leurs dépendances uniquement via des arguments de constructeur, des paramètres d'une méthode de fabrique ou des attributs définis sur l'instance d'objet après sa construction ou renvoyé par une méthode de fabrique. 
Le conteneur injecte ces dépendances lorsqu'il crée le bean. 

Le code est plus propre avec le principe DI, et le découplage est plus efficace.
L'objet ne recherche pas ses dépendances et ne connaît ni l'emplacement ni la classe des dépendances. 
Les classes sont plus faciles à tester, en particulier si les dépendances sont des interfaces, ce qui permet d'utiliser des implémentations de simulation dans les tests unitaires.

DI existe en deux variantes majeures: l'injection de dépendances basée sur un constructeur et l'injection de dépendances basée sur un Setter.

**Injection de dépendances basée sur le constructeur**

Le conteneur appelle un constructeur avec un certain nombre d'arguments, chacun représentant une dépendance.
L'appel d'une méthode de fabrique statique avec des arguments spécifiques pour construire le bean est presque équivalent.

```java
public class SimpleMovieLister {

    // the SimpleMovieLister has a dependency on a MovieFinder
    private MovieFinder movieFinder;

    // a constructor so that the Spring container can inject a MovieFinder
    public SimpleMovieLister(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
    ...
}

```

*Résolution d'argument de constructeur*

La résolution d'argument se produit en utilisant le type de l'argument. 
Si aucune ambiguïté n'existe dans les arguments d'une définition de bean, l'ordre des arguments de constructeur fournis dans une définition de bean est l'ordre dans lequel ces arguments sont passés au constructeur lorsque le bean est instancié. 

```java
package x.y;

public class ThingOne {
    public ThingOne(ThingTwo thingTwo, ThingThree thingThree) {
        // ...
    }
}
```

Si ThingTwo et ThingThree ne sont pas liées par héritage, aucune ambiguïté n'existe.
Ainsi, pas besoin de spécifier explicitement les index ou les types d'argument du constructeur dans l'élément `<constructor-arg/>`.

```xml
<beans>
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg ref="beanTwo"/>
        <constructor-arg ref="beanThree"/>
    </bean>
    <bean id="beanTwo" class="x.y.ThingTwo"/>
    <bean id="beanThree" class="x.y.ThingThree"/>
</beans>
```

Lorsqu'un bean est référencé, le type est connu et la correspondance peut se faire. 

Lorsqu'un type simple est utilisé, tel que `<value>true</value>`, Spring ne peut pas déterminer le type de la valeur et ne peut donc pas correspondre par type sans aide.

```java
package examples;

public class ExampleBean {

    private int years;
    private String ultimateAnswer;

    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
```

*Correspondance du type d'argument du constructeur*

Le conteneur peut utiliser la correspondance de type avec des types simples

```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg type="int" value="7500000"/>
    <constructor-arg type="java.lang.String" value="42"/>
</bean>
```

*Index des arguments du constructeur*

Utiliser l'attribut `index` pour spécifier explicitement l'index des arguments

```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg index="0" value="7500000"/>
    <constructor-arg index="1" value="42"/>
</bean>
```

La spécification d'un index résout l'ambiguïté également lorsqu'un constructeur a deux arguments du même type. L'index est basé sur 0.

*Nom de l'argument du constructeur*

Utiliser le nom du paramètre du constructeur pour lever l'ambiguïté 

```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg name="years" value="7500000"/>
    <constructor-arg name="ultimateAnswer" value="42"/>
</bean>
```

Pour que cela fonctionne, le code doit être compilé avec l'indicateur de débogage activé afin de rechercher le nom du paramètre auprès du constructeur.
On peut aussi utiliser l'annotation @ConstructorProperties pour nommer explicitement les arguments de constructeur. 

```java
package examples;

public class ExampleBean {

    @ConstructorProperties({"years", "ultimateAnswer"})
    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
```

**Injection de dépendances basée sur un setter**

Le conteneur appelle des `setter` sur les beans après leur instanciation.

```java
public class SimpleMovieLister {

    // the SimpleMovieLister has a dependency on the MovieFinder
    private MovieFinder movieFinder;

    // a setter method so that the Spring container can inject a MovieFinder
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

`ApplicationContext` prend en charge les DI basées sur les constructeurs et les setter. 
Les dépendances configurées sous forme de `BeanDefinition` sont utilisées conjointement avec des `PropertyEditor` pour convertir les propriétés d'un format à un autre.

**DI basée sur le constructeur ou sur le setter ?**

Il est judicieux d'utiliser des constructeurs pour les dépendances obligatoires et des setters pour les dépendances facultatives. 
(L'utilisation de `@Required` sur un setter rend la dépendance obligatoire mais l'injection par constructeur avec validation des arguments reste préférable).

L'injection par constructeur :
- permet d'implémenter des objets immuables ;
- garantit que les dépendances requises ne sont pas nulles ;
- renvoie à l'appelant des composants entièrement initialisés. 

> Remarque : un grand nombre d'arguments de constructeur révèle une mauvaise conception, du fait que la classe a trop de responsabilités et devrait être refactorisée pour mieux gérer la séparation des préoccupations.

L'injection par setter est principalement utilisée pour les dépendances facultatives auxquelles des valeurs par défaut peuvent être attribuées.
Des vérifications de non nullité doivent être effectuées partout où le code utilise la dépendance.
L'avantage de l'injection par setter est que les dépendances de la classe sont susceptibles d'être réinjectées ultérieurement.


**Processus de résolution des dépendances**

La résolution de dépendances s'effectue comme suit:
- `ApplicationContext` est créé et initialisé avec les données de config décrivant les beans.
- Pour chaque bean, les dépendances sont exprimées sous la forme de propriétés, d'arguments de constructeur ou d'arguments de la méthode static-factory. Ces dépendances sont fournies au bean.
- Chaque argument de propriété ou de constructeur est une valeur explicite ou une référence à un autre bean.
- Chaque argument qui est une valeur est converti de son format spécifié à son type réel.
	Spring peut convertir une valeur fournie au format chaîne en tous les types intégrés, tels que int, long, String, boolean, etc.

Spring valide la configuration de chaque bean lors de la création du conteneur. 
Les propriétés du bean elles-mêmes ne sont définies qu'après la création du bean.
Les beans de portée **singleton** et déclarés instantanés sont créés au chargement du conteneur (mode par défaut).
Les autres beans ne sont créés que lorsqu'ils sont invoqués.

La création d'un bean provoque potentiellement la création d'un graphe de beans représentant l'ensemble des dépendances. 
Les incompatibilités de résolution entre ces dépendances peuvent apparaître tardivement, lors de la création d'un bean affecté comme dépendance.

**Dépendances circulaires**

Avec l'injection par constructeur, il peut se produire un scénario de dépendance circulaire insoluble.
Le conteneur Spring détecte cette référence circulaire à l'exécution et lève une exception `BeanCurrentlyInCreationException`.

Une solution possible esr de configurer les dépendances circulaires avec l'injection par setter.
Ceci force l'un des beans à être injecté dans l'autre avant d'être complètement initialisé lui-même.

Généralement Spring détecte les problèmes de configuration, tels que les références à des beans inexistants et des dépendances circulaires, au moment du chargement du conteneur.
Mais Spring définit les propriétés et résout les dépendances le plus tard possible, lorsque le bean est réellement créé.
Un conteneur Spring chargé correctement peut donc ultérieurement générer une exception lorsqu'un objet est demandé.
Les implémentations ApplicationContext pré-instancient les beans singleton par défaut pour se prémunir contre cette manisfestation tardive des problèmes.

Si aucune dépendance circulaire n'existe, chaque bean collaborant est totalement configuré avant d'être injecté dans le bean dépendant.
Si le bean A a une dépendance sur le bean B, Spring configure complètement le bean B avant d'appeler la méthode setter sur le bean A.
En d'autres termes :
- le bean est instancié (s'il ne s'agit pas d'un singleton pré-instancié ),
- ses dépendances sont ensuite définies 
- et enfin les méthodes de cycle de vie (`init-method` configurée) sont appelées

### Dépendances et configuration en détail

Les métadonnées de configuration XML prennent en charge les sous-éléments `<property/>` et `<constructor-arg/>` pour définir les propriétés des beans.

#### Valeurs directes (primitives, chaînes, etc.)

L'attribut `value` de l'élément `<property/>` spécifie une propriété sous la forme d'une chaîne lisible. 
Le service de conversion de Spring est utilisé pour convertir ces valeurs au type réel de la propriété. 

```xml
<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <!-- results in a setDriverClassName(String) call -->
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
</bean>
```

L'exemple suivant utilise l'espace de noms p pour une configuration XML plus succincte:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close"
        p:driverClassName="com.mysql.jdbc.Driver"
        p:url="jdbc:mysql://localhost:3306/mydb"/>
</beans>
```

Configurer une instance `java.util.Properties`, comme suit:

```xml
<bean id="mappings"
    class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
    <property name="properties">
        <value>
            jdbc.driver.className=com.mysql.jdbc.Driver
            jdbc.url=jdbc:mysql://localhost:3306/mydb
        </value>
    </property>
</bean>
```

Spring convertit le texte à l'intérieur de `<value/>` en une instance `java.util.Properties`.
C'est l'un des rares cas où Spring privilégie l'utilisation de `<value/>` imbriqué par rapport à l'attribut `value`.

#### L'élément `idref`

L'élément `idref` est un moyen de passer sans erreur l'id d'un bean (une valeur de chaîne, pas une référence) à un élément <constructor-arg/> ou <property/>.

```xml
<bean id="theTargetBean" class="..."/>

<bean id="theClientBean" class="...">
    <property name="targetName">
        <idref bean="theTargetBean"/>
    </property>
</bean>
```

L'utilisation de idref permet au conteneur de valider au chargement que le bean  référencé existe réellement. 

Un endroit courant où l'élément `<idref/>` apporte de la valeur est dans la configuration des intercepteurs AOP dans une définition de bean `ProxyFactoryBean`.
L'utilisation de `<idref/>` pour spécifier un nom d'intercepteur évite de mal l'orthographier.


#### Références à d'autres beans (collaborateurs)

L'élément `ref` est le dernier élément à l'intérieur d'un élément `<constructor-arg/>` ou `<property/>`. 
La valeur de la propriété est spécifiée comme référence à un autre bean (un collaborateur).
La portée et la validation dépendent de si vous spécifiez l'ID de l'autre objet via l'attribut `bean` ou `parent`.

Spécifier le bean cible via l'attribut `bean` de la balise `<ref/>` crée une référence à n'importe quel bean dans le même conteneur ou conteneur parent. 

```xml
<ref bean="someBean"/>
```

Spécifier le bean cible via l'attribut `parent` crée une référence à un bean d'un conteneur parent du conteneur actuel.
Cette variante de référence de bean est utilisée principalement avec une hiérarchie de conteneurs pour encapsuler un bean du conteneur parent dans un proxy du même nom que le bean parent.

```xml
<!-- in the parent context -->
<bean id="accountService" class="com.something.SimpleAccountService"/>


<!-- in the child (descendant) context -->
<bean id="accountService" <!-- bean name is the same as the parent bean -->
    class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="target">
        <ref parent="accountService"/> <!-- notice how we refer to the parent bean -->
    </property>
</bean>
```

#### Beans imbriqués

Un élément `<bean/>` à l'intérieur des éléments `<property/>` ou `<constructor-arg/>` définit un bean interne

```xml
<bean id="outer" class="...">
    <!-- instead of using a reference to a target bean, simply define the target bean inline -->
    <property name="target">
        <bean class="com.example.Person"> <!-- this is the inner bean -->
            <property name="name" value="Fiona Apple"/>
            <property name="age" value="25"/>
        </bean>
    </property>
</bean>
```

Une définition de bean imbriqué ne nécessite pas d'ID ou de nom défini.
- Si spécifié, le conteneur n'utilise pas une telle valeur comme identificateur. 
- Le conteneur ignore également l'indicateur de portée lors de la création, car les beans internes sont toujours anonymes et sont toujours créés avec le bean externe. 
- Il n'est pas possible de les injecter dans des beans autres que le bean englobant.

#### Les collections

Les éléments `<list/>`, `<set/>`, `<map/>` et `<props/>` définissent respectivement les propriétés des types Java List, Set, Map et Properties.

```xml
<bean id="moreComplexObject" class="example.ComplexObject">
    <property name="adminEmails">
        <props>
            <prop key="administrator">administrator@example.org</prop>
            <prop key="support">support@example.org</prop>
            <prop key="development">development@example.org</prop>
        </props>
    </property>
    <property name="someList">
        <list>
            <value>a list element followed by a reference</value>
            <ref bean="myDataSource"/>
        </list>
    </property>
    <property name="someMap">
        <map>
            <entry key="an entry" value="just some string"/>
            <entry key ="a ref" value-ref="myDataSource"/>
        </map>
    </property>
    <property name="someSet">
        <set>
            <value>just some string</value>
            <ref bean="myDataSource"/>
        </set>
    </property>
</bean>
```

La valeur d'une clé ou d'une valeur peut également être l'un des éléments suivants:

`bean | ref | idref | list | set | map | props | value | null`

**Fusion de collections**

On peut définir un élément parent `<list/>`, `<map/>`, `<set/>` ou `<props/>` et avoir des éléments enfants qui héritent ou surchargent les valeurs de la collection parent.
Les valeurs de la collection enfant sont la fusion des éléments parent et enfant, les éléments enfant remplaçant les valeurs de la collection parent.

```
<beans>
    <bean id="parent" abstract="true" class="example.ComplexObject">
        <property name="adminEmails">
            <props>
                <prop key="administrator">administrator@example.com</prop>
                <prop key="support">support@example.com</prop>
            </props>
        </property>
    </bean>
    <bean id="child" parent="parent">
        <property name="adminEmails">
            <!-- the merge is specified on the child collection definition -->
            <props merge="true">
                <prop key="sales">sales@example.com</prop>
                <prop key="support">support@example.co.uk</prop>
            </props>
        </property>
    </bean>
<beans>
```

> A noter : l'utilisation de l'attribut `merge = true` sur l'élément `<props/>`

Ce comportement de fusion s'applique de la même manière aux types `<list/>`, `<map/>` et `<set/>`.
Dans le cas spécifique de l'élément `<list/>`, la notion de collection ordonnée de valeurs est conservée. Les valeurs du parent précèdent toutes les valeurs de la liste enfant.

**Collection fortement typée**
Avec l'introduction de types génériques dans Java 5, il est possible de déclarer un type pour une `Collection`. Afin d'injecter une `Collection` fortement typée dans un bean, on profite de la prise en charge de la conversion de type de Spring

#### Noms de propriété composés

On peut utiliser des noms de propriété composés ou imbriqués dans les définitions de bean, à condition que les composants du chemin, à l'exception du nom de propriété final, ne soient pas nuls.

```xml
<bean id="something" class="things.ThingOne">
     <property name="fred.bob.sammy" value="123"/>
</bean>
```

Le bean `something`  a une propriété `fred`, qui a une propriété `bob`, qui a une propriété `sammy` définie sur une valeur de 123. Pour que cela fonctionne, les propriétés `fred` et `bob` ne doivent pas être nulles après la construction du bean.
Sinon, une `NullPointerException` est levée.


### Utilisation de `depends-on`

Si un bean est une dépendance d'un autre bean, cela signifie qu'il est défini comme une propriété de l'autre.
L'attribut depend-on peut forcer explicitement un ou plusieurs beans à être initialisés avant que le bean utilisant cet élément ne soit initialisé.
Pour exprimer une dépendance sur plusieurs beans, fournir une liste de noms de bean à l'attribut depend-on (virgules, espaces et points-virgules sont des délimiteurs valides):

```xml
<bean id="beanOne" class="ExampleBean" depends-on="manager,accountDao">
    <property name="manager" ref="manager"/>
</bean>
<bean id="manager" class="ManagerBean"/>
<bean id="accountDao" class="x.y.jdbc.JdbcAccountDao"/>
```

L'attribut `depends-on` peut spécifier à la fois une dépendance d'initialisation et pour les singletons uniquement, une dépendance de destruction correspondante. Les beans qui définissent une relation dépendante avec un bean donné sont détruits en premier. Ainsi, on peut également contrôler l'ordre d'arrêt

### Beans à initialisation différé

Par défaut, `ApplicationContext` crée instantanément les beans singleton lors du processus d'initialisation.
En général, cette pré-instanciation est souhaitable afin de à découvrir immédiatement les erreurs de configuration.
Lorsque ce comportement n'est pas souhaitable, la pré-instanciation d'un bean singleton peut être empêchée en spécifiant un einitialisation différée pour le bean. 
Le conteneur IoC crée dans ce cas une instance de bean lors de sa première demande, plutôt qu'au démarrage

Ce comportement est contrôlé par l'attribut `lazy-init` sur l'élément `<bean/>`

```xml
<bean id="lazy" class="com.something.ExpensiveToCreateBean" lazy-init="true"/>
<bean name="not.lazy" class="com.something.AnotherBean"/>
```

Toutefois, si un bean initialisé en différé est une dépendance d'un bean singleton qui ne l'est pas, `ApplicationContext` crée ces beans au démarrage, pour satisfaire les dépendances du singleton.

On peut aussi contrôler l'initialisation différée au niveau du conteneur en utilisant l'attribut `default-lazy-init` sur l'élément `<beans/>`

```xml
<beans default-lazy-init="true">
    <!-- no beans will be pre-instantiated... -->
</beans>
```

### Câblage automatique

Le conteneur Spring peut gérer automatiquement les relations entre les beans collaborant.
Spring peut résoudre automatiquement les dépendances en inspectant le contenu de `ApplicationContext`. 
Le câblage automatique présente les avantages suivants:
- Réduire considérablement la spécification des propriétés ou des arguments de constructeur. 
- Mettre à jour la configuration à mesure que les objets évoluent.
	Par exemple, l'ajout d'une dépendance à une classe peut être satisfaite automatiquement sans modifier la configuration.
	
Le mode câblage automatique pour une définition de bean est spécifié avec l'attribut `autowire` de l'élément `<bean/>`.
La fonctionnalité de câblage automatique a quatre modes.

| Mode  | Explication |
|--     | --          |
| `no` | (Par défaut) Pas de câblage automatique. Les références de bean doivent être définies par des éléments ref. La modification du paramètre par défaut n'est pas recommandée car la spécification explicite des collaborateurs donne plus de contrôle et de clarté. |
| `byName` | Autowiring par nom de propriété. Spring recherche un bean portant le même nom que la propriété qui doit être câblée automatiquement.|
| `byType` | La propriété est câblée automatiquement si exactement un bean du type de propriété existe dans le conteneur. S'il en existe plusieurs, une exception fatale est levée. S'il n'y en a pas, la propriété n'est pas définie.|
| `constructor` | Analogue à `byType` mais s'applique aux arguments du constructeur. S'il n'y a pas exactement un bean du type d'argument constructeur, une erreur fatale est déclenchée.|

Avec le mode de câblage automatique byType ou constructeur, on peut câbler des tableaux et des collections typées.
Tous les candidats qui correspondent au type attendu sont fournis pour satisfaire la dépendance. 
On peut transférer automatiquement des instances de Map si le type de clé est String. 
Les valeurs d'une Map auto-câblée se composent de tous les beans qui correspondent au type attendu, et les clés de la Map contiennent les noms de bean correspondants.

**Limitations et inconvénients du câblage automatique**

Limites et inconvénients du câblage automatique:
- Les dépendances explicites dans les paramètres de propriété et de constructeur remplacent toujours le câblage automatique.
- Onne peut pas câbler automatiquement des propriétés simples telles que primitives, chaînes et  classes (et des tableaux de ces propriétés simples).
- Le câblage automatique est moins exact que le câblage explicite. Les relations entre objets gérés par Spring ne sont plus documentées explicitement.
- Les informations de câblage ne sont pas disponibles pour les outils qui de génèrent de la doc.
- Plusieurs définitions de bean dans le conteneur peuvent correspondre au type spécifié. Dans ce  scénario, plusieurs options:
	- Abandonner le câblage automatique au profit d'un câblage explicite.
	- Éviter le câblage automatique d'un bean en définissant l'attribut `autowire-candidate` à `false`
	- Désigner une seule définition de bean comme candidat principal en définissant l'attribut `primary`  de son élément `<bean/>` à `true`.
	- Implémenter le contrôle plus fin disponible avec la configuration basée sur les annotations.

**Exclure un bean de l'autowiring**

Pour exclure un bean de l'autowiring, définir l'attribut `autowire-candidate` à sur `false`.
Le conteneur rend cette définition de bean indisponible au câblage automatique (y compris pour les configurations par annotation telle que @Autowired).

L'attribut `autowire-candidate` affecte uniquement le câblage automatique basé sur le type.
L'autowiring par nom injecte un bean si le nom correspond même s'il n'est pas marqué comme candidat.

L'élément `<beans/>` de niveau supérieur accepte un ou plusieurs modèles dans son attribut default-autowire-candidats. Par exemple, pour limiter le statut de candidat autowire à tout bean dont le nom se termine par Repository, indiquez la valeur * Repository. Pour fournir plusieurs modèles, définissez-les dans une liste séparée par des virgules. Une valeur explicite de true ou false pour l'attribut autowire-candidate d'une définition de bean a toujours la priorité. Pour ces beans, les règles de correspondance de modèles ne s'appliquent pas.

Ces techniques sont utiles pour les beans que vous ne souhaitez jamais injecter dans d'autres beans par autowiring. Cela ne signifie pas qu'un bean exclu ne peut pas lui-même être configuré à l'aide du câblage automatique. Au contraire, le haricot lui-même n'est pas un candidat pour l'autowiring d'autres haricots.

