# Conteneur Spring IoC  
  
L'inversion de contrôle (IoC) est un processus par lequel les objets définissent leurs dépendances uniquement via :  
- des arguments de constructeur,  
- des arguments d'une méthode de fabrique  
- ou des propriétés définies sur l'instance d'objet après sa construction.  
  
Le conteneur injecte ces dépendances lorsqu'il crée l'objet.  
Ce processus est l'inverse du contrôle des dépendances par l'objet lui-même en utilisant l'instanciation directe ou un mécanisme tel que le `Service Locator`.  
  
L'interface `BeanFactory` fournit un mécanisme de configuration capable de gérer tout type d'objet.  
`ApplicationContext` est une sous-interface de `BeanFactory` qui ajoute:  
- Une intégration avec les fonctionnalités AOP de Spring  
- Une gestion des ressources de messages (avec l'internationalisation)  
- Une publication d'événements  
- Des contextes spécifiques tels que `WebApplicationContext` utilisé dans les applications Web.  
  
Dans Spring, les objets gérés par le conteneur Spring IoC sont appelés beans :  
- Un bean est un objet instancié, assemblé et géré par un conteneur Spring IoC.  
- Les beans et leurs dépendances sont spécifiés dans les métadonnées de configuration utilisées par le conteneur.  
  
## Présentation du conteneur `ApplicationContext`  
  
`org.springframework.context.ApplicationContext` représente le conteneur Spring IoC et est responsable d'instancier, de configurer et d'assembler les beans.  
  
Des métadonnées de configuration spécifient au conteneur les objets qui composent l'application et les interdépendances entre ces objets.  
  
Dans les applications autonomes, une instance de `ClassPathXmlApplicationContext` ou de `FileSystemXmlApplicationContext` est créée en lien avec des métadonnées de configuration au format XML traditionnel.  
    
Les classes de l'application sont combinées avec les métadonnées de configuration de sorte qu'après l'initialisation de `ApplicationContext`, l'application est entièrement configurée et exécutable.  
  
  
### Métadonnées de configuration  
  
Les métadonnées de configuration indiquent au conteneur Spring d'instancier, de configurer et d'assembler les objets dans l'application.  
  
Outre le format XML traditionnel, les autres formes de métadonnées avec le conteneur Spring sont :  
- La configuration basée sur les annotations (Spring 2.5).  
- La configuration Java (Spring 3.0) qui définit des beans à l'extérieur des classes de l'application en utilisant Java plutôt que des fichiers XML.  
  
Les métadonnées de configuration XML configurent les beans comme des éléments `<bean/>` à l'intérieur d'un élément `<beans/>` de niveau supérieur.  
La configuration Java utilise généralement des méthodes annotées `@Bean` dans une classe `@Configuration`.  
  
En général, on définit des objets de service, d'accès aux données (DAO), de présentation ou d'infrastructure.  
On ne configure pas d'objets de domaine à granularité fine dans le conteneur, car c'est la responsabilité de la logique métier de créer ces objets.  
  
  
### Instancier un conteneur  
  
Les chemins d'emplacement fournis à `ApplicationContext` permettent au conteneur de charger des métadonnées de configuration à partir de ressources externes, telles que le système de fichiers local, le classpath java, etc.  
  
```java  
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");  
```
  
Il n'est pas toujours nécessaire d'instancier un conteneur Spring IoC explicitement.  
Par exemple, dans une application Web, quelques lignes de descripteur dans le `web.xml` suffisent.  
  
### Composer plusieurs sources de configuration XML  
  
Il est parfois utile de répartir les définitions de bean dans plusieurs fichiers XML.  
On peut utiliser :  
- Soit le constructeur de `ApplicationContext` pour charger des définitions de bean à partir de plusieurs emplacements XML.  
- Soit l'élément `<import/>` pour charger des définitions de bean à partir d'autres fichiers.  
  
```xml  
<beans>  
    <import resource="services.xml"/>  
    <import resource="resources/messageSource.xml"/>  
    <import resource="/resources/themeSource.xml"/>    
    <bean id="bean1" class="..."/>  
    <bean id="bean2" class="..."/>  
</beans>  
```
  
Les chemins sont toujours relatifs au fichier effectuant l'import et le cas échéant, la présence d'une barre oblique principale est ignorée.  
Le contenu des fichiers importés doit être conforme au schéma Spring.  
  
> On peut utiliser des emplacements pleinement qualifiés au lieu de chemins relatifs: par exemple, `file:C:/config/services.xml` ou `classpath:/config/services.xml`.  
  
### Utilisation du conteneur  
  
`ApplicationContext` est l'interface d'une fabrique qui maintient un registre des beans et de leurs dépendances.  
La méthode `T getBean(String name, Class<T> requiredType)` permet de récupérer une instance de beans et d'y accéder.  
  
```java  
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");  
PetStoreService service = context.getBean("petStore", PetStoreService.class);  
List <String> userList = service.getUsernameList();  
```
  
Une variante est `GenericApplicationContext` qui se combine avec des lecteurs délégués (exemple: `XmlBeanDefinitionReader` pour les fichiers XML):  
  
```java  
GenericApplicationContext context = new GenericApplicationContext();  
new XmlBeanDefinitionReader(context).loadBeanDefinitions("services.xml", "daos.xml");  
context.refresh ();  
```
  
On peut mélanger différents types de lecteurs délégués sur le même `ApplicationContext` qui lit ainsi des définitions de bean depuis diverses sources de configuration.  
  
 
## Présentation des Beans  
  
Dans Spring, les définitions de beans sont représentées comme des objets `BeanDefinition`, qui contiennent les métadonnées suivantes:  
- Un nom de classe qualifié : généralement, la classe d'implémentation réelle du bean en cours de définition.  
- Les éléments comportementaux du bean au sein du conteneur (portée, callbacks de cycle de vie, etc...)  
- Les références à d'autres beans appelés collaborateurs ou dépendances.  
- Les autres paramètres à définir dans l'objet nouvellement créé sous forme de valeurs explicites.  
  
Ces métadonnées sont un ensemble de propriétés qui constituent la définition d'un bean.  
  
En plus des définitions de bean, `ApplicationContext` permet également l'enregistrement programmatique d'objets créés en dehors du conteneur.  
Pour ceci, la méthode `getBeanFactory()` renvoie l'implémentation `DefaultListableBeanFactory` qui définit les méthodes `registerSingleton()` et `registerBeanDefinition()`.  
Les beans référencés manuellement doivent l'être le plus tôt possible pour être traités correctement lors des étapes d'introspection.  
  
  
### Nommer les beans  
  
Chaque bean a un ou plusieurs identifiants qui doivent être uniques dans le conteneur.  
Un bean n'a généralement qu'un seul identifiant. S'il en a plus d'un, les autres sont considérés comme des alias.  
  
Dans les métadonnées de configuration XML:  
- L'attribut `id` permet de spécifier exactement un identifiant.  
- Pour définir d'autres alias, les spécifier dans l'attribut name séparés par une virgule (,), un point-virgule (;) ou un espace blanc.  
  
Si aucun identifiant n'est fourni, le conteneur génère un nom unique pour le bean.  
Cependant, pour faire référence à un bean par son nom via l'élément `ref` ou une recherche de type `Service Locator`, il faut fournir un nom.  
  
#### Conventions de dénomination des Bean  
  
C'est la convention Java standard pour les noms de propriétés de beans.  
Exemples: `accountManager`, `accountService`, `userDao`, `loginController`, etc.  
  
Nommer les beans rend la configuration plus facile à lire et à comprendre.  
De plus, avec Spring AOP, le nommage facilite l'application des greffons à un ensemble de beans selon leur nom.  
  
Pour les composants sans nom explicite, Spring génère un nom automatiquement : c'est le nom de classe simple avec son caractère initial en minuscules.  
Dans le cas où les 2 premiers caractères sont en majuscules, la casse d'origine est conservée.  
  
#### Aliaser un bean en dehors de la définition du bean  
  
Dans une définition de bean, on peut fournir plusieurs noms en combinant un nom unique dans l'attribut `id` et un nombre quelconque d'autres noms dans l'attribut `name`.  
  
Spécifier tous les alias dans la définition de bean n'est pas toujours adéquat et il est parfois souhaitable d'introduire un alias pour un bean défini ailleurs.  
Dans les métadonnées de configuration XML, on utilise l'élément `<alias/>` pour ce faire :  
  
```xml  
<alias name="fromName" alias="toName"/>  
```
  
Dans ce cas, un bean dans le même conteneur nommé `fromName` peut également être appelé `toName`.  
  
### Instancier des beans  
  
Le conteneur examine la spécification du bean lorsqu'il est demandé pour créer ou acquérir l'objet réel.  

En configuration XML, le type d'objet est spécifié dans l'attribut `class` de l'élément `<bean/>`.  
Cet attribut est obligatoire, sauf ces exceptions:  
- Instanciation à l'aide d'une méthode `Instance Factory`  
- Héritage de définition de bean  
  
Utilisation de la propriété `class`:  
- Soit pour spécifier la classe du bean dans le cas où le conteneur l'instancie directement en appelant son constructeur (équivalent à l'opérateur `new`).  
- Soit pour spécifier la classe contenant la méthode de fabrique statique qui crée l'objet, dans le cas moins courant où le conteneur appelle une méthode de fabrique pour créer le bean.  
	Le type d'objet renvoyé par l'appel de la méthode de fabrique statique peut être une tout autre classe.  
  
#### Instanciation avec un constructeur  
  
Pour créer un bean avec l'approche constructeur, toutes les classes normales sont utilisables.  
Selon le type d'IoC utilisé pour ce bean spécifique, on peut avoir besoin d'un constructeur par défaut (vide).  
  
En configuration XML, on spécifie le bean comme suit:  
  
```xml  
<bean id="exampleBean" class="examples.ExampleBean"/>  
```
  
#### Instanciation avec une méthode de fabrique statique  
  
Avec une méthode de fabrique statique, l'attribut `class` spécifie la classe qui contient la méthode de fabrique et l'attribut `factory-method` spécifie le nom de la méthode de fabrique elle-même.  
Le type de l'objet retourné n'est pas spécifié.  
  
```xml  
<bean id="clientService" class="examples.ClientService" factory-method="createInstance"/>  
```

Cette façon de procéder est justifiée pour continuer d'utiliser des méthodes de fabriques existant dans l'ancien code.  
  
#### Instanciation à l'aide d'une méthode de fabrique d'instance   
  
Semblable à l'instanciation via une méthode de fabrique statique, l'instanciation avec une méthode de fabrique d'instance appelle une méthode non statique d'un bean existant du conteneur.  
Pour utiliser ce mécanisme, l'attribut `class` est vide et l'attribut `factory-bean` spécifie le nom d'un bean qui contient la méthode à invoquer pour créer l'objet.  
  
```xml  
<!-- le bean de fabrique, qui contient une méthode appelée createInstance () -->  
<bean id="serviceLocator" class="examples.DefaultServiceLocator"/>  
  
<!-- le bean à créer via le bean de fabrique -->  
<bean id="clientService" factory-bean="serviceLocator" factory-method="createClientServiceInstance"/>  
```
  
#### Déterminer le type d'exécution d'un bean  
  
Le type d'exécution réel d'un bean spécifique n'est pas simple.  
Une classe spécifiée dans la définition du bean est juste une référence de classe qui, à l'exécution, peut conduire à un type différent, ou ne pas être définie du tout dans le cas d'une méthode de fabrique.  
De plus le proxy AOP peut encapsuler une instance de bean dans un proxy qui expose de façon limitée le type réel du bean cible (uniquement ses interfaces implémentées).  
  
Pour connaître le type d'exécution réel d'un bean particulier, appeler `BeanFactory.getType` avec le nom du bean.  
  
  
## Dépendances  
  
L'injection de dépendances fait passer de la définition d'un ensemble de beans autonomes à une application exécutable où les objets collaborent pour atteindre un objectif   
  
### Injection de dépendance  
  
Par ce mécanisme, c'est le conteneur qui injecte ses dépendances lorsqu'il crée le bean.    
- Le code est plus propre, plus lisible et mieux structuré 
- Le découplage est plus efficace  
- Les classes sont plus faciles à tester, en particulier si les dépendances sont des interfaces, ce qui permet d'utiliser des implémentations de simulation dans les tests unitaires.  
  
Deux variantes majeures existent:
- l'injection de dépendances basée sur un constructeur
- et l'injection de dépendances basée sur un Setter.  
  
#### Injection de dépendances par constructeur  
  
Le conteneur appelle un constructeur avec un certain nombre d'arguments, chacun représentant une dépendance.  
L'appel d'une méthode de fabrique statique avec des arguments spécifiques pour construire le bean est équivalent.  
  
```java  
public class SimpleMovieLister {    
    // the SimpleMovieLister has a dependency on a MovieFinder  
    private MovieFinder movieFinder;    
    // a constructor so that the Spring container can inject a MovieFinder  
    public SimpleMovieLister(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
**Résolution d'argument de constructeur**  
  
La résolution d'argument se produit en utilisant le type de l'argument.  
Si aucune ambiguïté n'existe dans les arguments d'une définition de bean, l'ordre des arguments de constructeur fournis dans une définition de bean est l'ordre dans lequel ces arguments sont passés au constructeur lorsque le bean est instancié.  
  
```java  
package x.y;  
public class ThingOne {  
    public ThingOne(ThingTwo thingTwo, ThingThree thingThree) { ... }  
}  
```
  
Aucune ambiguïté n'existe et ainsi, pas besoin de spécifier les index ou les types d'argument du constructeur dans l'élément `<constructor-arg/>`.  
  
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
  
Lorsqu'un type simple est utilisé, tel que `<value>true</value>`, Spring ne peut pas déterminer le type de la valeur et ne peut donc pas faire correspondre le type sans aide.  
  
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
  
**Correspondance de type d'argument du constructeur**  
  
Le conteneur peut utiliser la correspondance de type avec des types simples  
  
```xml  
<bean id="exampleBean" class="examples.ExampleBean">  
    <constructor-arg type="int" value="7500000"/>  
    <constructor-arg type="java.lang.String" value="42"/>  
</bean>  
```
  
**Index des arguments du constructeur**  
  
Utiliser l'attribut `index` pour spécifier explicitement l'index des arguments  
  
```xml  
<bean id="exampleBean" class="examples.ExampleBean">  
    <constructor-arg index="0" value="7500000"/>  
    <constructor-arg index="1" value="42"/>  
</bean>  
```
  
La spécification d'un index résout l'ambiguïté également lorsqu'un constructeur a deux arguments du même type. L'index est basé sur 0.  
  
**Nom de l'argument du constructeur**  
  
Utiliser le nom du paramètre du constructeur pour lever l'ambiguïté  
  
```xml  
<bean id="exampleBean" class="examples.ExampleBean">  
    <constructor-arg name="years" value="7500000"/>  
    <constructor-arg name="ultimateAnswer" value="42"/>  
</bean>  
```
  
Pour que ça fonctionne, le code doit être compilé avec l'indicateur de débogage activé afin de rechercher le nom du paramètre auprès du constructeur.  
On peut aussi utiliser l'annotation `@ConstructorProperties` pour nommer explicitement les arguments de constructeur.  
  
```java  
package examples;  
  
public class ExampleBean {    
    @ConstructorProperties({"years", "ultimateAnswer"})  
    public ExampleBean(int years, String ultimateAnswer) {  
        ...
    }  
}  
```
  
#### Injection de dépendances par `setter`  
  
Le conteneur appelle des `setter`s sur les beans après leur instanciation.  
  
```java  
public class SimpleMovieLister {    
    private MovieFinder movieFinder;    
    public void setMovieFinder(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
**DI par constructeur ou par setter**  
  
Il est judicieux d'utiliser des constructeurs pour les dépendances obligatoires et des setters pour les dépendances facultatives.  
L'utilisation de `@Required` sur un setter rend la dépendance obligatoire mais l'injection par constructeur avec validation des arguments reste préférable.  
  
L'injection par constructeur :  
- permet d'implémenter des objets immuables ;  
- garantit que les dépendances requises ne sont pas nulles ;  
- renvoie à l'appelant des composants entièrement initialisés.  
  
> Remarque : un grand nombre d'arguments de constructeur révèle une mauvaise conception, du fait que la classe a trop de responsabilités et devrait être refactorisée pour mieux gérer la séparation des préoccupations.  
  
L'injection par setter est principalement utilisée pour les dépendances facultatives auxquelles des valeurs par défaut peuvent être attribuées.  
Des vérifications de non nullité doivent être effectuées partout où le code utilise la dépendance.  
L'avantage de l'injection par setter est que les dépendances de la classe sont susceptibles d'être réinjectées ultérieurement.  
  
#### Processus de résolution des dépendances  
  
La résolution de dépendances s'effectue comme suit:  
- `ApplicationContext` est créé et initialisé avec les données de configuration décrivant les beans.  
- Pour chaque bean, les dépendances sont exprimées sous la forme de propriétés, d'arguments de constructeur ou d'arguments de méthode de fabrique. Ces dépendances sont fournies au bean.  
- Chaque propriété ou argument de constructeur est une valeur explicite ou une référence à un autre bean.  
	- Chaque argument qui est une valeur est converti de son format spécifié à son type réel avec un `PropertyEditor`.  
	- Spring peut convertir une valeur `String` dans tous les types intégrés (`int`, `long`, `String`, `boolean`, etc).  
- Les propriétés du bean elles-mêmes ne sont définies qu'après la création du bean.  
  
Spring valide la configuration de chaque bean lors de la création du conteneur:  
- Les beans de portée **singleton** et déclarés instantanés (mode par défaut) sont créés au chargement du conteneur.  
- Les autres beans ne sont créés que lorsqu'ils sont invoqués.  
- La création d'un bean provoque la création d'un graphe de beans qui représente l'ensemble des dépendances.  
  
#### Dépendances circulaires  
  
Avec l'injection par constructeur, un scénario de dépendance circulaire insoluble peut se produire.  
Le conteneur Spring détecte cette référence circulaire à l'exécution et lève une exception `BeanCurrentlyInCreationException`.  
  
La solution est de configurer les dépendances circulaires avec l'injection par setter.  
Ceci permet à l'un des beans d'être injecté dans l'autre avant d'être complètement initialisé lui-même.  

Si aucune dépendance circulaire n'existe, chaque bean collaborant est totalement configuré avant d'être injecté dans le bean dépendant.  
Si le bean A a une dépendance sur le bean B, Spring configure complètement le bean B avant d'appeler la méthode setter sur le bean A :  
- le bean est instancié (s'il ne s'agit pas d'un singleton préinstancié),  
- ses dépendances sont ensuite définies  
- et enfin les méthodes de cycle de vie (`init-method` configurée) sont appelées  
 
Généralement les problèmes de configuration, tels que les références à des beans inexistants ou les dépendances circulaires, sont détectés au chargement du conteneur.  
Cependant comme Spring injecte les dépendances exprimées en tant que propriétés une fois le bean réellement créé, le conteneur peut être chargé correctement mais générer ultérieurement une exception lorsqu'un bean est demandé.  
Pour se prémunir contre cette manisfestation tardive des problèmes, les implémentations `ApplicationContext` préinstancient par défaut les beans singletons.  
  
  
### Configuration des dépendances en détail  
  
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

#### Properties  
  
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
La portée et la validation dépendent de si l'ID du collaborateur est spécifié via l'attribut `bean` ou `parent`.  
  
Spécifier le bean cible via l'attribut `bean` de la balise `<ref/>` crée une référence à n'importe quel bean dans le même conteneur ou conteneur parent.  
  
```xml  
<ref bean="someBean"/>  
```
  
Spécifier le bean cible via l'attribut `parent` crée une référence à un bean d'un conteneur parent.  
Cette variante est utilisée souvent dans une hiérarchie de conteneurs pour encapsuler un bean du conteneur parent dans un proxy du même nom que le bean parent.  
  
```xml  
<!-- parent context -->  
<bean id="accountService" class="com.something.SimpleAccountService"/>  
  
<!-- child (descendant) context -->  
<bean id="accountService"> <!-- bean name is the same as the parent bean -->  
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
    <!-- instead of using a reference, simply define the target bean inline -->  
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
  
Les éléments `<list/>`, `<set/>`, `<map/>` et `<props/>` définissent respectivement les propriétés des types Java `List`, `Set`, `Map` et `Properties`.  
  
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
  
```xml  
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
  
> A noter : l'utilisation de l'attribut `merge="true"` sur l'élément `<props/>`
  
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
  
L'attribut `depends-on` peut spécifier à la fois une dépendance d'initialisation et pour les singletons uniquement, une dépendance de destruction correspondante. Les beans qui définissent une relation dépendante avec un bean donné sont détruits en premier. Ainsi, on peut également contrôler l'ordre d'arrêt.  

### Beans à initialisation différéee  

Par défaut, `ApplicationContext` crée instantanément les beans singleton lors du processus d'initialisation.  
En général, cette pré-instanciation est souhaitable afin de à découvrir immédiatement les erreurs de configuration.  
Lorsque ce comportement n'est pas souhaitable, la pré-instanciation d'un bean singleton peut être empêchée en spécifiant un einitialisation différée pour le bean.  
Le conteneur IoC crée dans ce cas une instance de bean lors de sa première demande, plutôt qu'au démarrage.  
Ce comportement est contrôlé par l'attribut `lazy-init` sur l'élément `<bean/>`:  
  
```xml  
  
<bean id="lazy" class="com.something.ExpensiveToCreateBean" lazy-init="true"/>  
<bean name="not.lazy" class="com.something.AnotherBean"/>  
```
  
Toutefois, si un bean initialisé en différé est une dépendance d'un bean singleton qui ne l'est pas, `ApplicationContext` crée ces beans au démarrage, pour satisfaire les dépendances du singleton.  
  
On peut aussi contrôler l'initialisation différée au niveau du conteneur en utilisant l'attribut `default-lazy-init` sur l'élément `<beans/>`:  
  
```xml  
<beans default-lazy-init="true">  
    <!-- no beans will be pre-instantiated... -->  
</beans>>  
```
  
### Câblage automatique (Autowiring)  
  
Spring peut gérer automatiquement les relations entre les beans collaborant et résoudre les dépendances en inspectant le contenu de `ApplicationContext`.  
Le câblage automatique présente les avantages suivants:  
- Réduire considérablement la spécification des propriétés ou des arguments de constructeur.  
- Mettre à jour la configuration à mesure que les objets évoluent.  
	En effet, l'ajout d'une dépendance est satisfaite automatiquement sans modifier la configuration.  
  
Le mode câblage automatique pour une définition de bean est spécifié avec l'attribut `autowire` de l'élément `<bean/>`.  
La fonctionnalité de câblage automatique a quatre modes.  
  
| Mode  | Explication |  
|--     | --          |  
| `no` | (Par défaut) Pas de câblage automatique. Les références de bean doivent être définies par des éléments `ref`. |  
| `byName` | Recherche d'un bean portant le même nom que la propriété qui doit être câblée automatiquement.|  
| `byType` | Câblage automatique si exactement un bean du type de la propriété existe dans le conteneur. S'il en existe plusieurs, une exception fatale est levée. S'il n'y en a pas, la propriété n'est pas définie.|  
| `constructor` | Analogue à `byType` mais s'applique aux arguments du constructeur. S'il n'y a pas exactement un bean du type d'argument constructeur, une erreur fatale est déclenchée.|  
  
Avec le mode de câblage automatique `byType` ou `constructor`, on peut câbler des tableaux et des collections typées.  
Tous les candidats qui correspondent au type attendu sont fournis pour satisfaire la dépendance.  
On peut transférer automatiquement des instances de Map si le type de clé est String.  
Les valeurs d'une Map auto-câblée se composent de tous les beans qui correspondent au type attendu, et les clés de la Map contiennent les noms de bean correspondants.  
  
**Limitates et inconvénients du câblage automatique**  
  
- Les dépendances explicites dans les paramètres de propriété et de constructeur remplacent toujours le câblage automatique.  
- On ne peut pas câbler automatiquement des valeurs explicites de propriétés telles que primitives, chaînes et classes.  
- Les relations entre objets gérés par Spring ne sont plus documentées explicitement.  
- Les informations de câblage ne sont pas disponibles pour les outils qui de génèrent de la doc.  
- Plusieurs définitions de bean dans le conteneur peuvent correspondre au type spécifié. Dans ce  scénario, plusieurs options:  
	- Abandonner le câblage automatique au profit d'un câblage explicite.  
	- Éviter le câblage automatique d'un bean en définissant l'attribut `autowire-candidate` à `false`
	- Désigner une seule définition de bean comme candidat principal en définissant l'attribut `primary`  de son élément `<bean/>` à `true`.  
	- Implémenter le contrôle plus fin disponible avec la configuration basée sur les annotations.  
  
**Exclure un bean de l'autowiring**  
  
Pour exclure un bean de l'autowiring, définir l'attribut `autowire-candidate` à `false`.  
> L'attribut `autowire-candidate` affecte uniquement le câblage automatique basé sur le type.  
> L'autowiring par nom injecte un bean si le nom correspond même s'il n'est pas marqué comme candidat.  
  
L'élément `<beans/>` de niveau supérieur accepte un ou plusieurs modèles dans son attribut `default-autowire-candidates`.  
Exemple pour limiter le statut de candidat à tout bean dont le nom se termine par Repository, indiquer `default-autowire-candidates=*Repository`.  
Pour fournir plusieurs modèles, les définir dans une liste séparée par des virgules.  
Une valeur explicite `autowire-candidate` au niveau d'un bean a toujours la priorité.  
  
  
### Injection de méthode  
  
En général, une dépendance se gère en définissant un bean collaborateur comme propriété de l'autre.  
Un problème survient lorsque les cycles de vie des beans sont différents.  
  
Supposons que le singleton A ait besoin d'utiliser le prototype B.  
Le conteneur crée le singleton A une seule fois, et ne définit ses propriétés qu'une seule fois.  
Il ne fournit donc au bean A qu'une seule instance du bean B, celle créée au moment de son initialisation.  
  
#### Principe de type Service Locator avec `ApplicationContextAware`  

Une solution est de renoncer à l'inversion de contrôle en implémentant l'interface `ApplicationContextAware` et en effectuant un appel `getBean("B")` au conteneur pour demander une nouvelle instance du bean B chaque fois que le bean A en a besoin.  
  
```java  
//uses a stateful Command-style class to perform some processing  
  
import org.springframework.beans.BeansException;  
import org.springframework.context.ApplicationContext;  
import org.springframework.context.ApplicationContextAware;  
  
public class CommandManager implements ApplicationContextAware {   
    private ApplicationContext applicationContext;    
    public Object process(Map commandState) {  
        // grab a new instance of the appropriate Command  
        Command command = createCommand();  
        // set the state on the brand new Command instance  
        command.setState(commandState);  
        return command.execute();  
    }  
    protected Command createCommand() {  
        // notice the Spring API dependency!  
        return this.applicationContext.getBean("command", Command.class);  
    }  
    @Override  
    public void setApplicationContext(  
            ApplicationContext applicationContext) throws BeansException {  
        this.applicationContext = applicationContext;  
    }  
}  
```
  
Ceci crée un couplage fort du code métier sur le framework Spring.    
L'injection de méthode est une fonctionnalité qui permet de gérer proprement ce cas d'utilisation.  
  
#### Injection de méthode de recherche (Lookup method)  
  
L'injection de méthode de recherche consiste à redéfinir une méthode sur un bean en renvoyant le résultat de la recherche d'un autre bean nommé dans le conteneur.  

Spring implémente l'injection de méthode en utilisant la bibliothèque CGLIB pour générer dynamiquement une sous-classe qui redéfinit la méthode.  
  
> Pour que cet héritage dynamique fonctionne, la classe que Spring cherche à sous-classer ne doit pas être finale et la méthode à redéfinir non plus.  
> Une autre limitation est que les méthodes de recherche ne fonctionnent pas avec les méthodes de fabrique statiques ni avec les méthodes `@Bean` statiques car dans ce cas, le conteneur ne fait pas l'instanciation et ne peut donc pas générer de sous-classe à la volée.  
  
Dans le cas de la classe `CommandManager` précédente, le conteneur Spring remplace dynamiquement l'implémentation de la méthode `createCommand()`.  
  
```java  
// no more Spring imports!  
public abstract class CommandManager {    
    public Object process(Object commandState) {  
        // grab a new instance of the appropriate Command interface  
        Command command = createCommand();  
        // set the state on the new Command instance  
        command.setState(commandState);  
        return command.execute();  
    }  
    // where is the implementation of this method ?  
    protected abstract Command createCommand();  
}  
```
  
Dans la classe cliente, la méthode à injecter nécessite une signature de la forme suivante:  
`<public|protected> [abstract] <return-type> theMethodName(no-arguments);`
  
Si la méthode est abstraite, la sous-classe générée dynamiquement implémente la méthode.  
Sinon, la sous-classe générée dynamiquement remplace la méthode concrète définie dans la classe d'origine.  
  
```xml  
<!-- a stateful bean deployed as a prototype (non-singleton) -->  
<bean id="myCommand" class="fiona.apple.AsyncCommand" scope="prototype">  
    <!-- inject dependencies here as required -->  
</bean>  
<!-- commandProcessor uses statefulCommandHelper -->  
<bean id="commandManager" class="fiona.apple.CommandManager">  
    <lookup-method name="createCommand" bean="myCommand"/>  
</bean>  
```
  
Le bean identifié comme `commandManager` appelle sa propre méthode `createCommand()` chaque fois qu'il a besoin d'une nouvelle instance du bean `myCommand`.  
Le bean `myCommand` est déclaré en tant que `prototype` pour renvoyer une nouvelle instance à chaque appel.  
  
Dans le modèle par annotations, une méthode de recherche se déclare avec l'annotation `@Lookup`
  
```java  
public abstract class CommandManager {  
    public Object process(Object commandState) {  
        Command command = createCommand();  
        command.setState(commandState);  
        return command.execute();  
    }  
    @Lookup("myCommand")  
    protected abstract Command createCommand();  
}  
```
  
On peut aussi compter sur la résolution de bean par rapport au type de retour de la méthode de recherche:  
  
```java  
public abstract class CommandManager {  
    public Object process(Object commandState) {  
        MyCommand command = createCommand();  
        command.setState(commandState);  
        return command.execute();  
    }  
    @Lookup  
    protected abstract MyCommand createCommand();  
}  
```
  
A noter qu'il faut généralement déclarer ces méthodes de recherche annotées avec une implémentation concrète, afin qu'elles soient compatibles avec les règles de scannage des composants où les classes abstraites sont ignorées par défaut.  
  
  
## Portées des Beans  
  
Les beans peuvent être déployés dans l'une des six portées prises en charge par Spring (quatre ne sont disponibles que dans un contexte Web)  
  
| Portée    | Description |  
| --        | --          |  
| singleton | (Par défaut) Une seule instance d'objet pour chaque conteneur Spring IoC|  
| prototype | Une définition de bean fournit n'importe quel nombre d'instances d'objet|  
| request   | Une instance de bean est associée au cycle de vie d'une requête HTTP (Web)|  
| session   | Une instance de bean est associée au cycle de vie d'une session HTTP (Web)|  
| application | Une instance de bean est associée au cycle de vie d'une ServletContext HTTP (Web)|  
| websocket | Une instance de bean est associée au cycle de vie d'une WebSocket (Web)|  
  
  
### La portée Singleton  
  
Une seule instance partagée d'un bean singleton est gérée.  
Cette instance unique est stockée dans un cache de singletons, et toutes les demandes et références renvoient l'objet mis en cache.  
  
Le concept de Spring d'un bean singleton diffère du modèle de singleton tel que défini dans le livre de modèles Gang of Four (GoF). Le singleton GoF code en dur la portée d'un objet de telle sorte qu'une et une seule instance d'une classe particulière est créée par ClassLoader.  
  
### La portée prototype  
  
La portée prototype du déploiement de bean entraîne la création d'une nouvelle instance de bean à chaque fois qu'une demande pour ce bean spécifique est effectuée.  
En règle générale, il faut utiliser la portée prototype pour tous les beans à état et la portée singleton pour les beans sans état.  
  
L'exemple suivant définit un bean comme prototype en XML:  
  
```xml  
<bean id="accountService" class="com.something.DefaultAccountService" scope="prototype"/>  
```
  
Contrairement aux autres portées, Spring ne gère pas le cycle de vie complet d'un prototype.  
Le conteneur instancie, configure et assemble un objet prototype et le transmet au client, sans autre enregistrement de cette instance de prototype.  
Ainsi les callback d'initialisation sont appelés quelle que soit la portée mais, dans le cas des prototypes, les callback de destruction ne sont pas appelés.  
  
Le code client doit donc nettoyer les objets à portée prototype et libérer les ressources coûteuses.  
Pour ceci, utiliser un post-processeur personnalisé qui contient une référence aux beans qui doivent être nettoyés.  
  
Le rôle de Spring en ce qui concerne un prototype est semblable à l'opérateur Java `new`.  
Le cycle de vie au-delà de ce point doit être géré par le client.  
  
### Singleton avec dépendances Prototype-bean  
  
Pour les beans singleton avec des dépendances sur des beans prototypes, les dépendances sont résolues au moment de l'instanciation.  
Le prototype injecté à la création du singleton est la seule instance qui soit fournie au singleton.  
  
Pour que le bean à portée singleton acquière une nouvelle instance du prototype à plusieurs reprises lors de l'exécution, il faut utiliser l'injection de méthode de recherche (`lookup method injection`)  
  
### Portées de requête, de session, d'application et WebSocket  
  
Ces portées sont disponibles uniquement pour les implémentations `ApplicationContext` compatibles avec le Web (telle que `XmlWebApplicationContext`).  
Sinon une `IllegalStateException` est levée.  
  
#### Configuration Web initiale  
  
Pour prendre en charge ce type de portée Web, une configuration initiale mineure est requise avant de définir les beans.  
- Pour accéder aux beans d'une requête traitée par Spring `DispatcherServlet` dans Spring Web MVC, aucune configuration spéciale n'est nécessaire. `DispatcherServlet` expose déjà tous les états pertinents.  
- Pour un moteur Servlet 2.5 et des requêtes traitées en dehors du `DispatcherServlet`, il faut référencer un `org.springframework.web.context.request.RequestContextListener`.  
- Pour Servlet 3.0+, ça peut être effectué par programme à l'aide de l'interface `WebApplicationInitializer`.  
  
**web.xml**  
  
```xml  
<web-app>  
    ...  
    <listener>  
        <listener-class>  
            org.springframework.web.context.request.RequestContextListener  
        </listener-class>  
    </listener>  
    ...  
</web-app>  
```
  
`DispatcherServlet`, `RequestContextListener` et `RequestContextFilter` font tous exactement la même chose, à savoir lier l'objet de requête HTTP au thread qui traite cette requête. Cela rend les beans de portée `request` et `session` disponibles plus bas dans la pile d'appels.  
  
#### Portée `request`
  
```xml  
<bean id="loginAction" class="com.something.LoginAction" scope="request"/>  
```
Le conteneur Spring crée une nouvelle instance du bean LoginAction pour chaque requête HTTP. Autrement dit, le bean loginAction a une portée au niveau de la requête HTTP.  
L'état interne de l'instance créée peut être modifié car les autres requêtes HTTP ne voient pas ce changements d'état.  
  
Pour une configuration par annotations ou une configuration Java, l'annotation `@RequestScope` peut être utilisée  
  
```java  
@RequestScope  
@Component  
public class LoginAction {  
    // ...  
}  
```
  
#### Portée `session`
  
```xml  
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>  
```
  
Spring crée une nouvelle instance du bean `UserPreferences` pour la durée de vie d'une session HTTP.  
Comme pour les beans à portée de requête, l'état interne de l'instance créée peut être modifié sans incidence sur les autres sessions  
  
Pour une configuration par annotations ou une configuration Java, utiliser l'annotation `@SessionScope`
  
```java  
@SessionScope  
@Component  
public class UserPreferences {  
    // ...  
}  
```
  
#### Portée `application`
  
```xml  
<bean id="appPreferences" class="com.something.AppPreferences" scope="application"/>  
```
  
Le conteneur Spring crée une nouvelle instance du bean une fois pour toute l'application Web. Le bean `appPreferences` a une portée au niveau `ServletContext` et est stocké comme un attribut. Ceci est similaire à un bean singleton mais diffère de deux manières:  
- il s'agit d'un singleton par `ServletContext`, pas par `ApplicationContext` (qui peuvent être multiples dans une application Web),  
- il est en fait exposé et donc visible en tant qu'attribut du `ServletContext`.  
  
Pour une configuration par annotations ou une configuration Java, utiliser l'annotation `@ApplicationScope`
  
```java  
@ApplicationScope  
@Component  
public class AppPreferences {  
    // ...  
}  
```
  
#### Beans à portée en tant que dépendances  
  
Pour injecter (par exemple) un bean de portée requête HTTP dans un autre bean de portée plus large, il faut injecter un proxy AOP à la place du bean cible.  
Le proxy doit exposer la même interface publique que l'objet cible et également pouvoir récupérer l'objet cible réel ayant la portée appropriée (telle qu'une requête HTTP) pour déléguer les appels de méthode à l'objet réel.  
  
On peut utiliser `<aop:scoped-proxy/>` sur un bean prototype, chaque appel de méthode sur le proxy partagé conduisant à la création d'une nouvelle instance cible vers laquelle l'appel est ensuite transféré.  
  
> Les proxys à portée ne sont pas le seul moyen d'accéder aux beans de portée plus courte en toute sécurité.  
> Le point d'injection (argument du constructeur, du setter ou champ autowired) peut être déclaré comme `ObjectFactory<MyTargetBean>`, permettant un appel à `getObject()` pour récupérer l'instance à chaque fois que cela est nécessaire.  
  
Il est important de comprendre le pourquoi et le comment de la configuration suivante:  
  
```xml  
<beans xmlns="http://www.springframework.org/schema/beans"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
    xmlns:aop="http://www.springframework.org/schema/aop"  
    xsi:schemaLocation="http://www.springframework.org/schema/beans  
        https://www.springframework.org/schema/beans/spring-beans.xsd  
        http://www.springframework.org/schema/aop  
        https://www.springframework.org/schema/aop/spring-aop.xsd">  
  
    <!-- an HTTP Session-scoped bean exposed as a proxy -->  
    <bean id="userPreferences" class="com.something.UserPreferences" scope="session">  
        <!-- instructs the container to proxy the surrounding bean -->  
        <aop:scoped-proxy/>  
    </bean>  
  
    <!-- a singleton-scoped bean injected with a proxy to the above bean -->  
    <bean id="userService" class="com.something.SimpleUserService">  
        <!-- a reference to the proxied userPreferences bean -->  
        <property name="userPreferences" ref="userPreferences"/>  
    </bean>  
</beans>  
```
  
Pour créer un tel proxy, il faut insérer un élément `<aop:scoped-proxy/>` dans une définition de bean.  
Les beans de portée `request`, `session` et `custom` nécessitent cet élément.  
Ainsi, le conteneur crée un objet proxy qui expose la même interface publique que la dépendance `UserPreferences` et qui est capable de récupérer la véritable dépendance dans la portée qui convient (requête, session, ...).  
Le conteneur injecte ce proxy dans le singleton `userManager` qui ignore que cette référence est un proxy.  
L'invocation d'une méthode sur l'objet injecté appelle en fait une méthode sur le proxy.  
Le proxy récupère le véritable objet cible et lui délègue l'invocation de la méthode.  
  
#### Choix du type de proxy à créer  
  
Par défaut, lorsqu'un bean est marqué avec l'élément `<aop:scoped-proxy/>`, le conteneur Spring crée un proxy de classe basé sur CGLIB.  
Les proxies CGLIB n'interceptent que les appels de méthodes publiques  
Les appels de méthodes non publiques ne sont donc pas délégués à l'objet cible réel.  
  
Le conteneur Spring peut aussi créer des proxies standards basés sur l'interface JDK, en spécifiant `false` sur l'attribut `proxy-target-class` de l'élément `<aop:scoped-proxy/>`.  
Cependant, la classe du bean scoped doit pour ceci implémenter au moins une interface et les collaborateurs dans lesquels le bean est injecté doivent référencer le bean via l'une de ses interfaces.  
  
```xml  
<!-- DefaultUserPreferences implements the UserPreferences interface -->  
<bean id="userPreferences" class="com.stuff.DefaultUserPreferences" scope="session">  
    <aop:scoped-proxy proxy-target-class="false"/>  
</bean>  
  
<bean id="userManager" class="com.stuff.UserManager">  
    <property name="userPreferences" ref="userPreferences"/>  
</bean>  
```
  
### Portées personnalisées  
  
Le mécanisme de portée du bean est extensible.  
  
#### Création d'une portée personnalisée  
  
Il faut implémenter l'interface `org.springframework.beans.factory.config.Scope` qui dispose de quatre méthodes pour obtenir des objets, les supprimer et les laisser être détruits à partir de la portée.  
  
L'implémentation de la portée de session, par exemple, retourne le bean de portée de session (s'il n'existe pas, la méthode retourne une nouvelle instance du bean, après l'avoir lié à la session pour référence future).  
  
#### Utilisation d'une étendue personnalisée  
  
Après avoir écrit une implémentation de portée personnalisée, il faut la référencer auprès du conteneur Spring via la méthode `ConfigurableBeanFactory.registerScope(String scopeName, Scope scope)`
Cette méthode est disponible via la propriété `BeanFactory` des implémentations de `ApplicationContext`.  
- Le premier argument est le nom unique associé à une portée.  
- Le deuxième argument est une instance réelle de l'implémentation `Scope` personnalisée.  
  
L'exemple suivant utilise `SimpleThreadScope`, fourni avec Spring mais non enregistré par défaut.  
  
```jav	a  
Scope threadScope = new SimpleThreadScope();  
beanFactory.registerScope("thread", threadScope);  
```
  
Définition de bean qui adhère aux règles de la portée personnalisée:  
  
```xml  
<bean id = "..." class = "..." scope = "thread">  
```
  
## Personnalisation de la nature d'un bean  
  
Spring fournit un certain nombre d'interfaces pour personnaliser la nature d'un bean.  
- Rappels de cycle de vie  
- ApplicationContextAware et BeanNameAware  
- Autres interfaces sensibles  
  
### Rappels de cycle de vie  
  
Pour interagir avec le cycle de vie du bean, implémenter les interfaces :  
- `InitializingBean` : Le conteneur appelle `afterPropertiesSet()` lors de l'initialisation  
- `DisposableBean` : Le conteneur appelle `destroy()` lors de la destruction.  
  
Mieux, les annotations JSR-250 `@PostConstruct` et `@PreDestroy` sont considérées comme les meilleures pratiques pour les rappels de cycle de vie.  
Car alors les beans ne sont pas couplés à des interfaces spécifiques Spring.  
  
Autre solution pour supprimer le couplage, considérer les métadonnées `init-method` et `destroy-method`.  
  
En interne, Spring utilise les implémentations de `BeanPostProcessor` pour traiter les interfaces de rappel et appeler les méthodes appropriées.  
Pour implémenter un comportement de cycle de vie que Spring n'offre pas par défaut, on peut implémenter un `BeanPostProcessor`.  
  
#### Méthodes d'initialisation et de destruction par défaut  
  
Les rappels d'initialisation ou de destruction qui n'utilisent sont généralement des méthodes avec des noms tels que `init()`, `initialize()`, `dispose()`, etc.  
Idéalement, ces noms sont normalisés dans un projet afin que tous les développeurs utilisent les mêmes noms de méthode et garantissent la cohérence.  
  
On peut configurer Spring pour rechercher les méthodes de rappel nommées sur l'ensemble des beans.  
Exemple pour des méthodes de rappel nommées `init()` à l'initialisation et `destroy()` à la destruction:  
  
```java  
public class DefaultBlogService implements BlogService {  
    private BlogDao blogDao;  
    ...  
    public void init() {  
        if (this.blogDao == null) {  
            throw new IllegalStateException("The [blogDao] property must be set.");  
        }  
    }  
}  
```
  
On utilise cette classe dans une définition de bean  
  
```xml  
<beans default-init-method="init">  
    <bean id="blogService" class="com.something.DefaultBlogService">  
        <property name="blogDao" ref="blogDao" />  
    </bean>  
</beans>  
```
  
La présence de l'attribut `default-init-method` sur l'attribut d'élément `<beans/>` de niveau supérieur permet au conteneur Spring de reconnaître la méthode `init` comme rappel d'initialisation.  
  
On peut configurer les rappels de destruction de la même manière avec l'attribut `default-destroy-method`.  
  
Un rappel d'initialisation configuré est appelé immédiatement après qu'un bean est fourni avec toutes les dépendances.  
Ainsi, le rappel d'initialisation est appelé sur la référence du bean avant que les intercepteurs AOP ne soient appliqués au bean.  
Il ne serait pas cohérent d'appliquer les intercepteurs à la méthode `init`, car cela couplerait le cycle de vie du bean cible à son proxy.  
  
#### Combinaison des mécanismes du cycle de vie  
  
Si plusieurs mécanismes de cycle de vie sont configurés pour un bean:  
- si chaque mécanisme est configuré avec un nom de méthode différent, chaque méthode est exécutée dans l'ordre indiqué ci-après.  
- si le même nom de méthode est configuré pour plusieurs de ces mécanismes, cette méthode est exécutée une fois.  
  
Ordre d'appel des méthodes d'initialisation :  
- Méthodes annotées avec `@PostConstruct`
- Méthode `afterPropertiesSet()` définie par l'interface `InitializingBean`
- Méthode `init()` personnalisée  
  
Les méthodes Destroy sont appelées dans l'ordre:  
- Méthodes annotées avec `@PreDestroy`
- Méthode `destroy()` définie par l'interface `DisposableBean`
- Méthode `destroy()` personnalisée  
  
#### Rappels au démarrage et à l'arrêt  
  
L'interface `Lifecycle` définit les méthodes essentielles pour tout objet qui a ses propres exigences de cycle de vie  
  
```java  
public interface Lifecycle {  
    void start();  
    void stop();  
    boolean isRunning();  
}  
```
  
Tout objet géré par Spring peut implémenter l'interface `Lifecycle`.  
Lorsque l'ApplicationContext reçoit des signaux de démarrage et d'arrêt, il cascade ces appels à toutes les implémentations de cycle de vie définies dans ce contexte.  
  
  
#### Arrêt du conteneur Spring IoC proprement dans les applications non Web  
  
Il faut enregistrer un hook d'arrêt avec la JVM pour garantir un arrêt progressif et appeller les méthodes de destruction appropriées sur les beans singletons afin de libérer les ressources.  
  
Enregistrer un hook d'arrêt se fait en appelant la méthode `registerShutdownHook()` déclarée sur l'interface `ConfigurableApplicationContext`
  
```java  
public final class Boot {  
    public static void main(final String[] args) throws Exception {  
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");  
        ctx.registerShutdownHook();  
        // app runs here...  
        // main method exits, hook is called prior to the app shutting down...  
    }  
}  
```
  
### `ApplicationContextAware` et `BeanNameAware`
  
Lorsqu'un ApplicationContext crée une instance d'objet qui implémente l'interface `org.springframework.context.ApplicationContextAware`, l'instance reçoit une référence à cet `ApplicationContext`.  
  
```java  
public interface ApplicationContextAware {  
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;  
}  
}  
```
  
Ainsi, les beans peuvent manipuler par programme l'ApplicationContext qui les a créés, via l'interface ApplicationContext ou en castant la référence à une sous-classe de cette interface (telle `ConfigurableApplicationContext`, qui expose des fonctionnalités supplémentaires).  
  
Le câblage automatique est une autre alternative pour obtenir une référence à ApplicationContext.  
  
Lorsqu'un ApplicationContext crée une classe qui implémente l'interface `org.springframework.beans.factory.BeanNameAware`, la classe reçoit une référence au nom défini dans sa définition d'objet associée.  
Ce rappel est appelé après le remplissage des propriétés normales du bean, mais avant un rappel d'initialisation personnalisé.  
  
### Autres interfaces sensibles  
  
Spring propose une gamme d'interfaces de rappel Aware qui permettent aux beans d'indiquer au conteneur qu'ils nécessitent une certaine dépendance d'infrastructure.  
  
Interfaces sensibles :  
- `ApplicationContextAware`: Déclarer ApplicationContext.  
- `ApplicationEventPublisherAware`: Éditeur d'événements de l'ApplicationContext.  
- `BeanClassLoaderAware`: Chargeur de classe utilisé pour charger les classes de bean.  
- `BeanFactoryAware`: Déclarer BeanFactory.  
- `BeanNameAware`: Nom du bean déclarant.  
- `LoadTimeWeaverAware`: Tisseur défini pour traiter la définition de classe au moment du chargement.  
- `MessageSourceAware`: Stratégie pour la résolution des messages  
- `NotificationPublisherAware`: Éditeur de notifications Spring JMX.  
- `ResourceLoaderAware`: Chargeur configuré pour un accès de bas niveau aux ressources.  
- `ServletConfigAware`: ServletConfig dans lequel le conteneur s'exécute (Spring MVC).  
- `ServletContextAware`: ServletContext dans lequel le conteneur s'exécute (Spring MVC)  
  
L'utilisation de ces interfaces lie le code à l'API Spring et ne suit pas le style Inversion of Control.  
Par conséquent, ils ne sont recommandés que pour les beans d'infrastructure qui nécessitent un accès  au conteneur.  
  
## Héritage de définition de bean  
  
Une définition de bean enfant hérite des données de configuration d'une définition parent.  
La définition enfant peut remplacer certaines valeurs ou en ajouter d'autres si nécessaire.  
L'utilisation de définitions de bean parent et enfant économise beaucoup de saisie.  
  
Dans un `ApplicationContext`, les définitions de bean enfant sont représentées par la classe `ChildBeanDefinition`.  
  
En configuration XML, on indique une définition de bean enfant à l'aide de l'attribut `parent`, en spécifiant le bean parent comme valeur de cet attribut.  
  
```xml  
<bean id="inheritedTestBean" abstract="true"  
        class="org.springframework.beans.TestBean">  
    <property name="name" value="parent"/>  
    <property name="age" value="1"/>  
</bean>  
  
<bean id="inheritsWithDifferentClass"  
        class="org.springframework.beans.DerivedTestBean"  
        parent="inheritedTestBean" init-method="initialize">  
    <property name="name" value="override"/>  
    <!-- the age property value of 1 will be inherited from parent -->  
</bean>  
```
  
Une définition de bean enfant hérite de la portée, des valeurs d'argument constructeur, des valeurs de propriété et des substitutions de méthode du parent, avec la possibilité d'ajouter de nouvelles valeurs.  
Les autres paramètres sont toujours pris à partir de la définition enfant: depends on, autowire mode, dependency check, singleton, and lazy init.  
  
Si la définition parente ne spécifie pas de classe, il est nécessaire de marquer explicitement la définition de bean parent comme abstraite (avec l'attribut `abstract="true"`)  
  
Une définition abstraite n'est utilisable que comme pur modèle qui sert de définition parent pour les définitions enfants.  
Essayer d'utiliser un tel bean parent abstrait seul renvoie une erreur.  
  
ApplicationContext pré-instancie tous les singletons par défaut. Par conséquent, il est important de définir l'attribut `abstract` à `true`, sinon le contexte d'application va tenter de pré-instancier le bean abstrait.  
  
  
## Points d'extension de conteneur  
  
En général, on n'a pas besoin de sous-classer les implémentations `ApplicationContext`.  
Au lieu de cela, le conteneur Spring IoC peut être étendu en connectant des implémentations d'interfaces d'intégration spéciales.  
  
### Personnalisation des beans à l'aide d'un BeanPostProcessor  
  
L'interface `BeanPostProcessor` définit des méthodes de rappel à implémenter pour fournir une logique personnalisée.  
  
Plusieurs instances de `BeanPostProcessor` peuvent être implémentées avec contrôle de l'ordre dans lequel elles s'exécutent en définissant la propriété `order`.  
Cette propriété ne peut être définie que si le `BeanPostProcessor` implémente l'interface `Ordered`.  
  
Les instances de `BeanPostProcessor` ont une portée par conteneur.  
Un `BeanPostProcessor` défini dans un conteneur post-traite uniquement les beans de ce conteneur, même si d'autres conteneurs font partie de la même hiérarchie.  
  
L'interface `BeanPostProcessor` comprend deux méthodes de rappel.  
Le post-processeur peut exécuter n'importe quelle action sur l'instance du bean, y compris l'ignorer complètement.  
  
Un post-processeur peut envelopper un bean avec un proxy. Certaines classes d'infrastructure Spring AOP sont implémentées en tant que post-processeurs de bean afin de fournir une logique d'encapsulation de proxy.  
  
Un `ApplicationContext` détecte automatiquement tous les beans définis dans les métadonnées de configuration qui implémentent l'interface `BeanPostProcessor`.  
`ApplicationContext` enregistre ces beans en tant que post-processeurs afin qu'ils puissent être appelés ultérieurement, lors de la création des autres beans.  
  
A noter que, lors de la déclaration d'un `BeanPostProcessor` par une méthode `@Bean`, le type de retour de la méthode doit être la classe d'implémentation ou au moins l'interface `BeanPostProcessor`, pour indiquer clairement la nature post-processeur.  
Sinon, ApplicationContext ne peut pas le détecter automatiquement, cette détection de type précoce est critique.  
  
**Enregistrement des instances de `BeanPostProcessor` par programme**  
  
L'approche recommandée pour l'inscription `BeanPostProcessor` est la détection automatique, mais il est possible de les inscrire par programme à l'aide de la méthode `ConfigurableBeanFactory.addBeanPostProcessor`.  
Cependant les instances `BeanPostProcessor` ajoutées par programme ne respectent pas l'interface `Ordered`. C'est l'ordre d'enregistrement qui dicte l'ordre d'exécution.  
Les instances de `BeanPostProcessor` enregistrées par programme sont toujours traitées avant celles enregistrées via la détection automatique.  
  
**Instances `BeanPostProcessor` et proxy automatique AOP**  
  
Les classes qui implémentent l'interface BeanPostProcessor sont spéciales.  
- Tous les beans BeanPostProcessor et leurs dépendances sont instanciés dans le cadre de la phase de démarrage spéciale d'ApplicationContext.  
- Ensuite, les instances de BeanPostProcessor sont triées et appliquées à tous les autres beans du conteneur.  
  
> Étant donné que le proxy AOP est implémenté lui-même en tant que `BeanPostProcessor`, ni les instances de `BeanPostProcessor` ni leurs dépendances ne sont éligibles au proxy AOP.  
  
Les beans connectés au `BeanPostProcessor` en utilisant l'autowiring ou `@Resource` sont inéligibles pour le proxy automatique ou d'autres types de post-traitement.  
  
#### Exemple: Hello World BeanPostProcessor  
  
L'exemple montre une implémentation de `BeanPostProcessor` personnalisée qui appelle la méthode toString() de chaque bean et imprime la chaîne résultante sur la console.  
  
```java  
package scripting;  
import org.springframework.beans.factory.config.BeanPostProcessor;  
  
public class InstantiationTracingBeanPostProcessor implements BeanPostProcessor {  
    // simply return the instantiated bean as-is  
    public Object postProcessBeforeInitialization(Object bean, String beanName) {  
        return bean;  
    }  
    public Object postProcessAfterInitialization(Object bean, String beanName) {  
        System.out.println("Bean '" + beanName + "' created : " + bean.toString());  
        return bean;  
    }  
}  
```
  
L'élément beans suivant utilise `InstantiationTracingBeanPostProcessor`:  
  
```xml  
<beans ...>  
	 ...  
    <!--  
    when beans are instantiated, this BeanPostProcessor implementation will output the fact to the system console  
    -->  
    <bean class="scripting.InstantiationTracingBeanPostProcessor"/>  
  
</beans>  
```
  
### Personnalisation des métadonnées de configuration avec un `BeanFactoryPostProcessor`
  
Autre point d'extension, le `BeanFactoryPostProcessor`.  
Cette interface est similaire à `BeanPostProcessor`, mais avec une différence majeure: le conteneur Spring permet à un `BeanFactoryPostProcessor` de lire et de modifier les métadonnées de configuration avant d'instancier les autres beans.  
  
L'ordre dans lequel les instances de `BeanFactoryPostProcessor` s'exécutent peut être contrôlé en définissant la propriété `order` (le BeanFactoryPostProcessor doit implémenter l'interface Ordered).  
  
Pour modifier les instances de bean réelles, il faut à la place utiliser un `BeanPostProcessor`.  
Bien qu'il soit techniquement possible de travailler avec des instances de bean dans un BeanFactoryPostProcessor (par exemple, en utilisant BeanFactory.getBean()), cela provoque une instanciation prématurée du bean, violant le cycle de vie du conteneur standard.  
  
Les instances de `BeanFactoryPostProcessor` ont une portée par conteneur.  
Un post-processeur de fabrique de bean est automatiquement exécuté lorsqu'il est déclaré dans un ApplicationContext, afin d'appliquer des modifications aux métadonnées de configuration.  
Spring fournit un certain nombre de post-processeurs de fabrique prédéfinis, tels que `PropertyOverrideConfigurer` et `PropertySourcesPlaceholderConfigurer`.  
  
Un `BeanFactoryPostProcessor` personnalisé peut être utilisé pour enregistrer des éditeurs de propriétés personnalisées.  
  
Un `ApplicationContext` détecte automatiquement les beans `BeanFactoryPostProcessor`.  
Il les utilise comme post-processeurs au moment opportun.  
  
  
#### Exemple: la propriété de substitution de nom de classe `PropertySourcesPlaceholderConfigurer`
  
`PropertySourcesPlaceholderConfigurer` est utilisé pour externaliser les valeurs de propriété d'un bean dans un fichier distinct à l'aide du format de propriétés Java standard.  
  
Dans le fragment de métadonnées suivant, un DataSource est défini avec des valeurs d'espace réservé:  
  
```xml  
<bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">  
    <property name="locations" value="classpath:com/something/jdbc.properties"/>  
</bean>  
  
<bean id="dataSource" destroy-method="close"  
        class="org.apache.commons.dbcp.BasicDataSource">  
    <property name="driverClassName" value="${jdbc.driverClassName}"/>  
    <property name="url" value="${jdbc.url}"/>  
    <property name="username" value="${jdbc.username}"/>  
    <property name="password" value="${jdbc.password}"/>  
</bean>  
```
  
L'exemple montre les propriétés configurées à partir d'un fichier de propriétés externe.  
Lors de l'exécution, un `PropertySourcesPlaceholderConfigurer` est appliqué aux métadonnées pour remplacer certaines propriétés de DataSource.  
Les valeurs à remplacer sont spécifiées comme des espaces réservés de la forme ${nom-propriété}, qui suit le style EL.  
Les valeurs réelles proviennent d'un autre fichier au format standard des propriétés Java  
  
Avec l'espace de noms `context` introduit dans Spring 2.5, on peut configurer des espaces réservés de propriété avec une configuration dédiée, en fournissant un ou plusieurs emplacements séparés par des virgules dans l'attribut `location`
  
```xml  
<context:property-placeholder location="classpath:com/something/jdbc.properties"/>  
```
  
Le `PropertySourcesPlaceholderConfigurer` ne recherche pas seulement les propriétés dans le fichier Propriétés spécifié. Par défaut, s'il ne trouve pas de propriété dans les fichiers de propriétés spécifiés, il vérifie les propriétés Spring Environment et les propriétés Java System standard.  
  
Si la classe ne peut pas être résolue lors de l'exécution en une classe valide, la résolution du bean échoue au moment de sa création.  
  
### Personnalisation de la logique d'instanciation avec un FactoryBean  
  
On peut implémenter l'interface `org.springframework.beans.factory.FactoryBean` pour les objets qui sont eux-mêmes des fabriques.  
  
L'interface `FactoryBean` est un point de connectivité dans la logique du conteneur Spring IoC. Si on dispose d'un code d'initialisation complexe mieux exprimé en Java qu'en XML, on peut créer un `FactoryBean` personnalisé, y écrire l'initialisation complexe, puis le brancher dans le conteneur.  
  
L'interface `FactoryBean` propose trois méthodes:  
- `Object getObject()`: renvoie une instance de l'objet créé par cette fabrique. L'instance peut éventuellement être partagée, selon que cette fabrique renvoie des singletons ou des prototypes.  
- `boolean isSingleton()`: renvoie `true` si ce `FactoryBean` renvoie des singletons ou `false` dans le cas contraire.  
- `Class getObjectType()`: renvoie le type d'objet retourné par la méthode `getObject()` ou `null` si le type n'est pas connu à l'avance.  
  
Le concept et l'interface `FactoryBean` sont utilisés à plusieurs endroits dans Spring.  
  
Lorsqu'on demande à un conteneur une instance `FactoryBean` elle-même au lieu du bean qu'il produit, il faut faire précéder l'ID du bean avec le symbole esperluette (&) lors de l'appel de la méthode `getBean()` de `ApplicationContext`.  
Ainsi, pour un `FactoryBean` donné avec un id `myBean`, invoquer `getBean("myBean")` sur le conteneur retourne le produit de `FactoryBean`, tandis que l'invocation de `getBean("&myBean")` renvoie l'instance `FactoryBean` elle-même.  
  
  
## Configuration de conteneur basée sur les annotations  
  
L'introduction de la configuration basée sur les annotations a soulevé la question de savoir si cette approche est meilleure que XML. La réponse courte est "ça dépend".  
Chaque approche a ses avantages et ses inconvénients, et, généralement, c'est au développeur de décider quelle stratégie lui convient le mieux.  
  
Elles conduisent à une configuration plus concise.  
Cependant XML excelle dans l'assemblage de composants sans toucher à leur code source.  
Certains développeurs préfèrent avoir la liaison près de la source tandis que d'autres soutiennent que les classes annotées ne sont plus des POJO et que la configuration devient décentralisée et plus difficile à contrôler.  
  
Quel que soit le choix, Spring peut accueillir les deux styles et même les mélanger.  
  
A noter que grâce à son option JavaConfig, Spring permet aux annotations d'être utilisées de manière non invasive, sans toucher au code source des composants cibles.  
  
Au lieu d'utiliser XML pour décrire un câblage de bean, le développeur déplace la configuration dans la classe de composant elle-même en utilisant des annotations sur la classe, la méthode ou le champ appropriée.  
  
Spring 2.5 a suivi cette approche pour piloter l'injection de dépendances.  
Essentiellement, l'annotation `@Autowired` fournit les fonctionnalités de câblage avec un contrôle fin et une applicabilité large.  
Spring 2.5 a également ajouté la prise en charge des annotations JSR-250, telles que `@PostConstruct` et `@PreDestroy`.  
  
Spring 3.0 a ajouté la prise en charge des annotations JSR-330 (Dependency Injection for Java) contenues dans le package `javax.inject` telles que `@Inject` et `@Named`.  
  
> L'injection d'annotation est effectuée avant l'injection XML.  
> Ainsi, la configuration XML remplace les annotations pour les propriétés injectées via les deux approches.  
  
La config par annotations est déclarée en incluant la balise `<context:annotation-config/>` dans une configuration XML (à noter, l'inclusion de l'espace de noms de `context`):  
  
```xml  
<<?xml version="1.0" encoding="UTF-8"?>  
<beans xmlns="http://www.springframework.org/schema/beans"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
    xmlns:context="http://www.springframework.org/schema/context"  
    xsi:schemaLocation="http://www.springframework.org/schema/beans  
        https://www.springframework.org/schema/beans/spring-beans.xsd  
        http://www.springframework.org/schema/context  
        https://www.springframework.org/schema/context/spring-context.xsd">  
    <context:annotation-config/>  
</beans>  
```
  
Les post-processeurs enregistrés implicitement incluent `AutowiredAnnotationBeanPostProcessor`, `CommonAnnotationBeanPostProcessor`, `PersistenceAnnotationBeanPostProcessor` et `RequiredAnnotationBeanPostProcessor`
  
L'élément `<context:annotation-config/>` recherche uniquement les annotations sur les beans dans le même contexte d'application.  
Cela signifie que si c'est utilisé dans un `WebApplicationContext` pour un `DispatcherServlet`, Spring ne recherche que les beans `@Autowired` dans les contrôleurs, et non les services.  
  
### `@Required`
  
L'annotation `@Required` s'applique aux méthodes de définition de propriétés de bean  
  
```java  
public class SimpleMovieLister {  
    private MovieFinder movieFinder;  
    @Required  
    public void setMovieFinder(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
Cette annotation indique que la propriété du bean concerné doit être renseignée au moment de la configuration, via une valeur explicite dans une définition de bean ou via le câblage automatique.  
Le conteneur lève une exception si la propriété du bean concerné n'a pas été renseignée.  
Cela évite les instances `NullPointerException` ou similaires plus tard.  
  
L'annotation `@Required` est obsolète à partir de Spring Framework 5.1, en faveur de l'utilisation de l'injection de constructeur pour les paramètres requis  
  
### Utiliser `@Autowired`
  
L'annotation `@Inject` de JSR 330 peut être utilisée à la place de l'annotation `@Autowired` de Spring dans les exemples inclus dans cette section.  
  
L'annotation `@Autowired` s'applique aux constructeurs  
  
```java  
public class MovieRecommender {  
    private final CustomerPreferenceDao customerPreferenceDao;  
    @Autowired  
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {  
        this.customerPreferenceDao = customerPreferenceDao;  
    }  
}  
```
  
L'annotation `@Autowired` sur un constructeur n'est pas nécessaire si le bean cible définit un seul constructeur.  
Cependant, si plusieurs constructeurs sont disponibles et s'il n'y a pas de constructeur primaire ou par défaut, un unique constructeur doit être annoté avec `@Autowired` pour indiquer au conteneur lequel utiliser.  
  
L'annotation `@Autowired` s'applique aussi aux méthodes de définition traditionnelles  
  
```java  
public class SimpleMovieLister {  
    private MovieFinder movieFinder;  
    @Autowired  
    public void setMovieFinder(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
Et également aux méthodes avec des noms arbitraires et plusieurs arguments  
  
```java  
public class MovieRecommender {  
    private MovieCatalog movieCatalog;  
    private CustomerPreferenceDao customerPreferenceDao;  
    @Autowired  
    public void prepare(MovieCatalog movieCatalog,  
            CustomerPreferenceDao customerPreferenceDao) {  
        this.movieCatalog = movieCatalog;  
        this.customerPreferenceDao = customerPreferenceDao;  
    }  
}  
```
  
`@Autowired` s'applique aussi aux champs en mixant éventuellement avec le contructeur  
  
```java  
public class MovieRecommender {  
    private final CustomerPreferenceDao customerPreferenceDao;  
    @Autowired  
    private MovieCatalog movieCatalog;  
    @Autowired  
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {  
        this.customerPreferenceDao = customerPreferenceDao;  
    }  
}  
```
  
Les composants cibles (`MovieCatalog` ou `CustomerPreferenceDao`) doivent être déclarés avec le type utilisé pour les points d'injection annotés `@Autowired`.  
Sinon, une erreur "aucune correspondance de type trouvée" risque de se produire à l'exécution.  
  
Pour les beans XML ou les composants trouvés via le scan du chemin de classe, le conteneur connaît généralement le type concret à l'avance.  
  
Cependant, pour les méthodes de fabrique `@Bean`, le type de retour déclaré doit être suffisamment explicite. Pour les composants qui implémentent plusieurs interfaces, il vaut mieux déclarer le type de retour le plus spécifique.  
  
Spring peut aussi fournir tous les beans d'un type particulier avec l'annotation `@Autowired` sur un champ ou une méthode qui attend un tableau.  
Il en va de même pour les collections typées  
  
```java  
public class MovieRecommender {  
    @Autowired  
    private MovieCatalog[] movieCatalogs;  
  
    private Set<MovieCatalog> movieCatalogsSet;  
    @Autowired  
    public void setMovieCatalogs(Set<MovieCatalog> movieCatalogs) {  
        this.movieCatalogsSet = movieCatalogs;  
    }  
}  
```
  
Les beans injectés peuvent implémenter l'interface `org.springframework.core.Ordered` ou utiliser l'annotation `@Order` pour trier la liste dans un ordre spécifique.  
Sinon, l'ordre est celui de l'enregistrement des définitions de beans cibles correspondants.r.  
L'annotation `@Order` peut être déclarée sur la classe ou sur les méthodes `@Bean`.  
Les valeurs `@Order` influencent les priorités aux points d'injection, mais pas l'ordre de démarrage des singletons qui est déterminé par les relations de dépendance.n.  
A noter, l'annotation standard `Priority` n'est pas disponible au niveau `@Bean`, car elle ne peut pas être déclarée sur les méthodes.  
  
Les instances de Map typées peuvent être injectées automatiquement si le leur clé est de type `String`.  
Les valeurs de mappage sont tous les beans du type attendu et les clés sont les noms de ces beans  
  
```java  
public class MovieRecommender {  
    private Map<String, MovieCatalog> movieCatalogs;  
    @Autowired  
    public void setMovieCatalogs(Map<String, MovieCatalog> movieCatalogs) {  
        this.movieCatalogs = movieCatalogs;  
    }  
}  
```
  
Par défaut, le câblage automatique échoue lorsqu'aucun beans candidats correspondants n'est disponible pour un point d'injection donné.  
Dans le cas d'un tableau, d'une collection ou d'une map, au moins un élément correspondant est attendu.  
  
Ce comportement peut être modifié en marquant un point d'injection comme non requis  
  
```java  
 	@Autowired(required = false)  
   public void setMovieFinder(MovieFinder movieFinder) {  
       this.movieFinder = movieFinder;  
   }  
```
  
Une méthode marquée non requise n'est pas du tout appelée si l'une de ses dépendances n'est pas disponible.  
Un champ non obligatoire n'est pas renseigné dans ce cas, conservant sa valeur par défaut.  
  
Les arguments de constructeur et de méthode `@Bean` injectés sont un cas spécial.  
L'attribut `required` dans `@Autowired` a une signification différente en raison de l'algorithme de résolution de constructeur qui peut potentiellement traiter plusieurs constructeurs.  
Les arguments de constructeur et de méthode de fabrique sont effectivement requis par défaut.  
Mais dans le cas d'arguments tels que tableaux, collections ou maps, ils sont résolus en instances vides si aucun bean correspondant n'est disponible.  
  
> Un seul constructeur d'une classe donnée peut déclarer `@Autowired` avec l'attribut `required=true`.  
> Si plusieurs constructeurs portent l'annotation, ils devront tous déclarer `required=false` afin d'être considérés comme des candidats pour l'autowiring.  
> Le constructeur avec le plus grand nombre de dépendances pouvant être satisfaites sera choisi.  
> Si aucun des candidats ne peut être satisfait, un constructeur principal / par défaut sera utilisé s'il est présent (de même, s'il y a plusieurs constructeurs mais aucun annoté avec @Autowired).  
> Si une classe ne déclare qu'un seul constructeur, il sera utilisé, même s'il n'est pas annoté.  
> Un constructeur annoté n'a pas besoin d'être public.  
  
La nature non requise d'une dépendance particulière peut aussi s'exprimer via `Optional` de Java 8  
  
```java  
public class SimpleMovieLister {  
    @Autowired  
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {  
        ...  
    }  
}  
```
  
À partir de Spring Framework 5.0, l'annotation `@Nullable` est utilisable (peu importe le package - par exemple, `javax.annotation.Nullable`)  
  
```java  
public class SimpleMovieLister {  
    @Autowired  
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {  
        ...  
    }  
}  
```
  
On peut également utiliser `@Autowired` pour les interfaces bien connues: `BeanFactory`, `ApplicationContext`, `Environment`, `ResourceLoader`, `ApplicationEventPublisher` et `MessageSource`.  
Ces interfaces et leurs extensions sont automatiquement résolues sans configuration.  
  
> Les annotations @Autowired, @Inject, @Value et @Resource sont gérées par les implémentations Spring BeanPostProcessor.  
> On ne peut donc pas appliquer ces annotations dans des BeanPostProcessor ou BeanFactoryPostProcessor personnalisés.  
> Ces types doivent être câblés explicitement par XML ou une méthode Spring `@Bean`.  
  
  
### Optimisation du câblage automatique avec les annotations avec `@Primary`
  
Étant donné que le câblage par type peut conduire à plusieurs candidats, il est nécessaire d'avoir plus de contrôle sur le processus de sélection.  
L'annotation `@Primary` indique qu'un bean particulier doit avoir la préférence lorsque plusieurs beans sont candidats. S'il existe exactement un bean principal parmi les candidats, il devient la valeur injectée.  
  
```java  
@Configuration  
public class MovieConfiguration {  
    @Bean  
    @Primary  
    public MovieCatalog firstMovieCatalog() { ... }  
    @Bean  
    public MovieCatalog secondMovieCatalog() { ... }  
}  
```
  
Avec la configuration précédente, le `MovieRecommender` suivant est câblé avec `firstMovieCatalog`
  
```java  
public class MovieRecommender {  
    @Autowired  
    private MovieCatalog movieCatalog;  
}  
```
Les définitions XML correspondantes sont les suivantes:  
  
```xml  
	<context:annotation-config/>  
   <bean class="example.SimpleMovieCatalog" primary="true">  
       <!-- inject any dependencies required by this bean -->  
   </bean>  
   <bean class="example.SimpleMovieCatalog">  
       <!-- inject any dependencies required by this bean -->  
   </bean>  
   <bean id="movieRecommender" class="example.MovieRecommender"/>  
```
  
### Réglage fin du câblage automatique avec les annotations `@Qualifier`
  
Pour plus de contrôle sur la sélection, on peut utiliser l'annotation `@Qualifier` qui associe des valeurs de qualifieurs à des arguments spécifiques, réduisant ainsi les correspondances de type.  
  
```java  
public class MovieRecommender {  
    @Autowired  
    @Qualifier("main")  
    private MovieCatalog movieCatalog;  
}  
```
  
L'annotation `@Qualifier` peut être placée également sur des arguments de constructeur ou de méthode  
  
```java  
public class MovieRecommender {  
    private MovieCatalog movieCatalog;  
    @Autowired  
    public void prepare(@Qualifier("main") MovieCatalog movieCatalog) {  
        this.movieCatalog = movieCatalog;  
    }  
}  
```
  
Configuration XML des beans correspondants  
  
```xml  
    <context:annotation-config/>  
    <bean class="example.SimpleMovieCatalog">  
        <qualifier value="main"/>  
    </bean>  
    <bean class="example.SimpleMovieCatalog">  
        <qualifier value="action"/>  
    </bean>  
    <bean id="movieRecommender" class="example.MovieRecommender"/>  
```
  
Le bean qualifié avec `<qualifier value="main"/>` est injecté dans l'argument constructeur qualifié avec la même valeur `main`.  
  
Par défaut, le nom du bean est considéré comme une valeur de qualifieur.  
Ainsi, définir le bean avec `id="main"` conduit au même résultat de correspondance.  
Bien qu'on puisse utiliser cette convention, `@Autowired` gère fondamentalement l'injection par type assortie de qualifieurs facultatifs.  
  
Les valeurs de qualifieur, y compris celles avec le nom de bean, ont toujours une sémantique de restriction des correspondances de type.  
Ils n'expriment pas une référence à un identifiant unique de bean.  
Les valeurs recommandées de qualifieur doivent exprimer des caractéristiques du composant indépendantes de l'ID du bean	.  
  
Les qualifieurs s'appliquent également aux collections typées, par exemple à `Set<MovieCatalog>`.  
Dans ce cas les beans correspondant aux qualifieurs déclarés sont injectés en tant que collection.  
Les qualifieurs constituent des critères de filtrage.  
Il est possible de définir plusieurs beans du même type avec la même valeur de qualifieur : ils seront tous injectés dans une collection si elle est annotée avec ce qualifieur.  
  
> La sélection des candidats par rapport au nom de bean cible ne nécessite pas d'annotation `@Qualifier` au point d'injection.  
> En l'absensce d'indicateur de résolution (`@Qualifier` ou `@Primary`) pour une situation de dépendance non unique, Spring fait correspondre le nom du point d'injection (le champ ou le paramètre) aux noms de beans candidats et cible le candidat du même nom, le cas échéant.  
  
Pour exprimer une injection basée sur le nom, `@Autowired` n'est pas recommandée.  
Il est préférable d'utiliser l'annotation `@Resource`, définie sémantiquement pour identifier un composant cible par son nom, sans considération du type déclaré.  
  
Pour les beans définis comme une collection, `@Resource` est une bonne solution, faisant référence au bean de collection ou de tableau spécifique par un nom unique.  
  
`@Autowired` considère également l'auto-référence pour l'injection (la référence au bean source de l'injection).  
  
> L'auto-injection est une solution de secours: Les dépendances sur d'autres composants ont toujours la priorité. L'auto-injection n'est donc jamais primaire.  
> En pratique, on n'utilise l'auto-référence qu'en dernier recours (par exemple, pour appeler des méthodes sur la même instance via un proxy)  
> Dans un tel scénario, il vaut mieux factoriser les méthodes concernées dans un bean délégué distinct.  
  
Injecter le résultat des méthodes `@Bean` sur la même classe de configuration est un scénario d'auto-référence. Il vaut mieux :  
- Soit résoudre paresseusement ces références dans la signature des méthodes qui en ont besoin en tant qu'arguments (par opposition à un champ injecté dans la classe de configuration),  
- Soit déclarer les méthodes `@Bean` concernées comme statiques, ce qui les découple de l'instance de classe de configuration.  
Sinon, ces beans ne sont pris en compte que dans la phase de secours.  
  
`@Qualifier` permet de restreindre la sélection au niveau du paramètre et s'applique aux méthodes multi-arguments.  
En revanche, `@Resource` n'est pris en charge que pour les champs et les méthodes `setter` avec un seul argument.  
  
#### Qualifieurs personnalisées  
  
Pour ce faire, il faut définir une annotation à laquelle on fournit l'annotation `@Qualifier`
  
```java  
@Target({ElementType.FIELD, ElementType.PARAMETER})  
@Retention(RetentionPolicy.RUNTIME)  
@Qualifier  
public @interface Genre {  
    String value();  
}  
```
  
Ensuite, le qualifieur personnalisé peut être fourni sur les champs et paramètres auto-câblés  
  
```java  
public class MovieRecommender {  
    @Autowired  
    @Genre("Action")  
    private MovieCatalog actionCatalog;  
    private MovieCatalog comedyCatalog;  
    @Autowired  
    public void setComedyCatalog(@Genre("Comedy") MovieCatalog comedyCatalog) {  
        this.comedyCatalog = comedyCatalog;  
    }  
}  
```
  
Enfin, fournir l'information de qualifieur personnalisé sur les définitions de bean candidats.  
  
```xml  
    <context:annotation-config/>  
    <bean class="example.SimpleMovieCatalog">  
        <qualifier type="Genre" value="Action"/>  
    </bean>  
    <bean class="example.SimpleMovieCatalog">  
        <qualifier type="example.Genre" value="Comedy"/>  
    </bean>  
    <bean id="movieRecommender" class="example.MovieRecommender"/>  
```
  
L'utilisation d'une annotation sans valeur peut suffire lorsque l'annotation a un objectif générique et peut s'appliquer à plusieurs types de dépendances.  
  
```java  
@Target({ElementType.FIELD, ElementType.PARAMETER})  
@Retention(RetentionPolicy.RUNTIME)  
@Qualifier  
public @interface Offline {}  
```
  
Annotation au point d'injection  
  
```java  
public class MovieRecommender {  
    @Autowired  
    @Offline  
    private MovieCatalog offlineCatalog;  
}  
```
  
Définition du bean qualifié  
  
```xml  
<bean class="example.SimpleMovieCatalog">  
    <qualifier type="Offline"/>  
</bean>  
```
  
Des qualifieurs personnalisés peuvent accepter des attributs nommés autres que l'attribut `value`
  
```java  
@Target({ElementType.FIELD, ElementType.PARAMETER})  
@Retention(RetentionPolicy.RUNTIME)  
@Qualifier  
public @interface MovieQualifier {  
    String genre();  
    String format();  
}  
```
  
Annotation au point d'injection  
  
```java  
public class MovieRecommender {  
    @Autowired  
    @MovieQualifier(format="VHS", genre="Action")  
    private MovieCatalog actionVhsCatalog;  
}  
```
  
Définition du bean qualifié  
  
```xml  
    <context:annotation-config/>  
    <bean class="example.SimpleMovieCatalog">  
        <qualifier type="MovieQualifier">  
            <attribute key="format" value="VHS"/>  
            <attribute key="genre" value="Action"/>  
        </qualifier>  
    </bean>  
```
  
### Utilisation de génériques comme qualifieurs de câblage automatique  
  
En plus de l'annotation `@Qualifier`, les types génériques Java peuvent être utilisés comme forme implicite de qualification.  
Exemple, avec la configuration suivante:  
  
```java  
@Configuration  
public class MyConfiguration {  
    @Bean  
    public StringStore stringStore() {  
        return new StringStore();  
    }  
    @Bean  
    public IntegerStore integerStore() {  
        return new IntegerStore();  
    }  
}  
```
  
Si les beans précédents implémentent une interface générique `Store<String>` et `Store<Integer>`, on peut `@Autowire` l'interface avec le générique utilisé comme qualifieur  
  
```java  
@Autowired  
private Store<String> s1; // <String> qualifier injects stringStore bean  
@Autowired  
private Store<Integer> s2; // <Integer> qualifier injects integerStore bean  
```
  
Les qualifieurs génériques s'appliquent également au câblage automatique des listes et des maps  
  
```java  
@Autowired  
private List<Store<Integer>> s;  
```
  
### Injection avec `@Resource`
  
Spring prend également en charge l'injection à l'aide de l'annotation JSR-250 `@Resource` (`javax.annotation.Resource`) sur les champs ou les méthodes.  
  
`@Resource` prend un attribut `name`.  
Par défaut, Spring interprète cette valeur comme le nom du bean à injecter.  
  
```java  
public class SimpleMovieLister {  
    private MovieFinder movieFinder;  
    @Resource(name="myMovieFinder")  
    public void setMovieFinder(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
Si aucun nom n'est spécifié explicitement, le nom par défaut est dérivé du nom de champ ou de la méthode `setter`
  
```java  
public class SimpleMovieLister {  
    private MovieFinder movieFinder;  
    @Resource  
    public void setMovieFinder(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
Dans le cas de l'utilisation de `@Resource` sans nom explicite, Spring cherche le bean cible ayant une correspondance de nom avec le point d'injection, puis s'il ne l'a pas trouvé il cherche une correspondance de type.  
C'est le processus inverse de la résolution de dépendance avec `@Autowired`.  
  
### Utiliser `@Value`
  
`@Value` est typiquement utilisé pour injecter des propriétés externalisées  
  
```java  
@Component  
public class MovieRecommender {  
    private final String catalog;  
    public MovieRecommender(@Value("${catalog.name}") String catalog) {  
        this.catalog = catalog;  
    }  
}  
```
Avec la configuration suivante:  
  
```java  
@Configuration  
@PropertySource("classpath:application.properties")  
public class AppConfig {}  
```
  
Et le fichier `application.properties` suivant:  
  
```properties  
catalog.name=MovieCatalog  
```
  
Dans ce cas, le paramètre et le champ `catalog` seront égaux à la valeur `MovieCatalog`.  
  
Un résolveur de valeur incorporé par défaut est fourni par Spring.  
Il essaie de résoudre la valeur de la propriété et s'il n'y parvient pas le nom de la propriété (exemple ${catalog.name}) est injecté comme valeur.  
Pour un contrôle strict sur les valeurs inexistantes, il faut déclarer un bean `PropertySourcesPlaceholderConfigurer`.  
  
```java  
@Configuration  
public class AppConfig {  
     @Bean  
     public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {  
           return new PropertySourcesPlaceholderConfigurer();  
     }  
}  
```
  
Lors de la configuration d'un `PropertySourcesPlaceholderConfigurer` à l'aide de JavaConfig, la méthode `@Bean` doit être statique.  
L'utilisation de cette configuration garantit l'échec de l'initialisation de Spring si un espace réservé `${}` ne peut pas être résolu.  
Il est également possible d'utiliser des méthodes telles que `setPlaceholderPrefix`, `setPlaceholderSuffix` ou `setValueSeparator` pour personnaliser les espaces réservés.  
  
Spring Boot configure par défaut un bean `PropertySourcesPlaceholderConfigurer` qui obtiendra les propriétés des fichiers `application.properties` et `application.yml`.  
  
Le support de conversion intégré permet à Spring de convertir automatiquement en types simples (`Integer` ou `int`).  
  
Plusieurs valeurs séparées par des virgules peuvent être automatiquement converties en tableau `String`.  
  
Une valeur par défaut peut également être fournie  
  
```java  
@Component  
public class MovieRecommender {  
    private final String catalog;  
    public MovieRecommender(@Value("${catalog.name:defaultCatalog}") String catalog) {  
        this.catalog = catalog;  
    }  
}  
```
  
Un `BeanPostProcessor` utilise un `ConversionService` en arrière plan pour faire la conversion de la `String` dans `@Value` en type cible.  
Pour fournir une prise en charge de conversion personnalisée, il faut fournir une instance de `ConversionService`
  
```java  
@Configuration  
public class AppConfig {  
    @Bean  
    public ConversionService conversionService() {  
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();  
        conversionService.addConverter(new MyCustomConverter());  
        return conversionService;  
    }  
}  
```
  
Lorsque `@Value` contient une expression SpEL, la valeur est calculée dynamiquement à l'exécution  
  
```java  
@Component  
public class MovieRecommender {  
    private final String catalog;  
    public MovieRecommender(@Value("#{systemProperties['user.catalog'] + 'Catalog' }") String catalog) {  
        this.catalog = catalog;  
    }  
}  
```
  
SpEL permet également l'utilisation de structures de données plus complexes:  
  
```java  
@Component  
public class MovieRecommender {  
    private final Map<String, Integer> countOfMoviesPerCatalog;  
    public MovieRecommender(  
            @Value("#{{'Thriller': 100, 'Comedy': 300}}") Map<String, Integer> countOfMoviesPerCatalog) {  
        this.countOfMoviesPerCatalog = countOfMoviesPerCatalog;  
    }  
}  
```
  
### Utilisation de `@PostConstruct` et `@PreDestroy`
  
`CommonAnnotationBeanPostProcessor` reconnaît l'annotation `@Resource` et aussi les annotations de cycle de vie JSR-250 `javax.annotation.PostConstruct` et `javax.annotation.PreDestroy`.  
Ces annotations offrent une alternative au mécanismes de rappels d'initialisation et les rappels de destruction.  
À condition que `CommonAnnotationBeanPostProcessor` soit inscrit dans Spring `ApplicationContext`, une méthode portant l'une de ces annotations est appelée dans le cycle de vie de la classe  
  
> Les annotations `@Resource`, `@PostConstruct` et `@PreDestroy` faisaient partie des bibliothèques Java standard du JDK 6 à 8.  
> L'ensemble du package `javax.annotation` a été séparé des modules Java de base dans JDK 9 et finalement supprimé dans JDK 11  
> Si nécessaire, l'artefact `javax.annotation-api` doit être obtenu via Maven.  
  
  
## Scannage des chemins de classe et composants gérés  
  
Cette section décrit le mécanisme permettant de détecter les composants candidats en scrutant le chemin de classe.  
Cela supprime le besoin d'utiliser XML pour effectuer l'enregistrement du bean.  
À la place, ce sont des annotations (par exemple, `@Component`), des expressions de type AspectJ ou des critères de filtre personnalisés qui sélectionnent les classes dont les définitions de bean sont enregistrées avec le conteneur.  
  
Depuis Spring 3.0, de nombreuses fonctionnalités fournies par Spring JavaConfig font partie du framework principal.  
  
### `@Component` et autres annotations de stéréotypes  
  
L'annotation `@Repository` est un marqueur pour toute classe qui remplit le rôle ou le stéréotype d'un référentiel (également appelé objet d'accès aux données ou DAO).  
Parmi les utilisations de ce marqueur figure la traduction automatique des exceptions.  
  
Spring fournit d'autres annotations de stéréotypes: `@Component`, `@Service` et `@Controller`.  
- `@Component` est un stéréotype générique pour tout composant géré par Spring.  
- `@Repository`, `@Service` et `@Controller` sont des spécialisations de `@Component` pour des scénarios spécifiques (couches de persistance, de service et de présentation). Les classes annotées avec `@Repository`, `@Service` ou `@Controller` sont plus adaptées au traitement par certains outils ou à l'association avec des aspects. De plus, ces annotations pourront avoir une sémantique supplémentaire dans les futures versions de Spring.  
  
### Utilisation des méta-annotations et des annotations composées  
  
La plupart des annotations fournies par Spring peuvent être utilisées comme méta-annotations.  
Une méta-annotation est une annotation qui peut être appliquée à une autre annotation.  
Par exemple, l'annotation `@Service` est méta-annotée avec `@Component`
  
Combiner des méta-annotations permet également de créer des annotations composées.  
Par exemple, l'annotation Spring MVC `@RestController` se compose de `@Controller` et `@ResponseBody`.  
  
De plus, les annotations composées peuvent redéclarer et personnaliser les attributs des méta-annotations.  
Cela peut être utile pour exposer uniquement un sous-ensemble des attributs de la méta-annotation.  
Exemple : `@SessionScope` code en dur le nom de la portée session mais permet toujours la personnalisation du proxyMode.  
  
```java  
@Target({ElementType.TYPE, ElementType.METHOD})  
@Retention(RetentionPolicy.RUNTIME)  
@Documented  
@Scope(WebApplicationContext.SCOPE_SESSION)  
public @interface SessionScope {  
    @AliasFor(annotation = Scope.class)  
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;  
}  
```
Utilisation de @SessionScope  
  
```java  
@Service  
@SessionScope(proxyMode = ScopedProxyMode.INTERFACES)  
public class SessionScopedUserService implements UserService {  
    // ...  
}  
```
  
### Détection automatique des classes et enregistrement des définitions de bean  
  
Spring peut détecter automatiquement les classes stéréotypées et enregistrer les instances `BeanDefinition` correspondantes dans `ApplicationContext`.  
  
Pour détecter automatiquement ces classes et enregistrer les beans correspondants, il faut ajouter `@ComponentScan` à une classe `@Configuration`, avec l'attribut `basePackages` qui définit le package parent commun (ou bien une liste séparée par des virgules, des points-virgules ou des espaces)  
  
```java  
@Configuration  
@ComponentScan(basePackages="org.example")  
public class AppConfig  {}  
```
  
Une alternative équivalente en configuration XML est l'élément `<context:component-scan base-package="org.example"/>` qui active implicitement la fonctionnalité `<context:annotation-config>`.  
  
Le scannage des packages nécessite la présence des entrées de répertoire correspondantes dans le chemin de classe.  
  
En outre, `AutowiredAnnotationBeanPostProcessor` et `CommonAnnotationBeanPostProcessor` sont tous deux implicitement inclus lors du scannage de composants. Ces deux composants sont détectés automatiquement et câblés ensembles sans aucune métadonnée de configuration XML.  
  
Pour désactiver l'enregistrement de ces composants, il suffit d'inclure l'attribut `annotation-config` avec la valeur `false`.  
  
### Filtres pour personnaliser le scannage  
  
Par défaut, les classes annotées avec `@Component`, `@Repository`, `@Service`, `@Controller`, `@Configuration` ou une annotation personnalisée elle-même annotée avec `@Component` sont les seuls composants candidats détectés.  
  
Cependant, il est possible d'étendre ce comportement avec des filtres ajoutés en tant qu'attributs `includeFilters` ou `excludeFilters` de l'annotation `@ComponentScan`
Ou en config XML, en tant qu'éléments enfants `<context:include-filter/>` ou `<context:exclude-filter/>` de l'élément `<context:component-scan>`
  
Types de filtres  
  
| Type de filtre | Exemple d'expression | Description |  
|-              -|-                    -|-           -|  
| annotation (par défaut) | org.example.SomeAnnotation | Annotation obligatoire au niveau du type dans les composants cibles|  
| assignable | org.example.SomeClass | Une classe (ou interface) attribuable aux composants cibles (héritage)|  
| aspectj | org.example..*Service+ | Expression de type AspectJ correspondant aux composants cibles.|  
| regex | org\.example\.Default.* | Expression regex correspondant aux noms de classe des composants cibles.|  
| custom | org.example.MyTypeFilter | Implémentation personnalisée de l'interface `org.springframework.core.type.TypeFilter`|  
  
L'exemple suivant montre la configuration ignorant toutes les annotations `@Repository` et utilisant à la place des référentiels `stub`
  
```java  
@Configuration  
@ComponentScan(basePackages = "org.example",  
        includeFilters = @Filter(type=FilterType.REGEX, pattern=".*Stub.*Repository"),  
        excludeFilters = @Filter(Repository.class))  
public class AppConfig {  
}  
```
  
La liste suivante montre le XML équivalent:  
  
```xml  
    <context:component-scan base-package="org.example">  
        <context:include-filter type="regex"  
                expression=".*Stub.*Repository"/>  
        <context:exclude-filter type="annotation"  
                expression="org.springframework.stereotype.Repository"/>  
    </context:component-scan>  
```
  
On peut également désactiver les filtres par défaut en définissant `useDefaultFilters=false` sur l'annotation ou en fournissant `use-default-filters="false"` comme attribut de `<component-scan/>`.  
Cela désactive efficacement la détection automatique des classes annotées ou méta-annotées avec @Component, @Repository, @Service, @Controller, @RestController ou @Configuration.  
  
### Définition des métadonnées Bean dans les composants  
  
Les composants Spring peuvent également apporter des définitions de bean au conteneur avec l'annotation `@Bean` aussi utilisée dans les classes `@Configuration`.  
  
```java  
@Component  
public class FactoryMethodComponent {  
    @Bean  
    @Qualifier("public")  
    public TestBean publicInstance() {  
        return new TestBean("publicInstance");  
    }  
}  
```
  
La classe précédente est un composant Spring qui contribue à une définition de bean avec une méthode de fabrique `publicInstance()`.  
L'annotation `@Bean` identifie la méthode de fabrique et d'autres propriétés de définition du bean, telles que le qualifieur via l'annotation `@Qualifier`.  
Les autres annotations au niveau de la méthode pouvant être spécifiées sont `@Scope`, `@Lazy` et les qualifieurs personnalisés.  
  
L'injection automatique est prise en charge au niveau des méthodes `@Bean` comme pour les méthodes `@Autowired`:  
  
```java  
@Component  
public class FactoryMethodComponent {  
    private static int i;  
    @Bean  
    @Qualifier("public")  
    public TestBean publicInstance() {  
        return new TestBean("publicInstance");  
    }  
    // use of a custom qualifier and autowiring of method parameters  
    @Bean  
    protected TestBean protectedInstance(  
            @Qualifier("public") TestBean spouse,  
            @Value("#{privateInstance.age}") String country) {  
        TestBean tb = new TestBean("protectedInstance", 1);  
        tb.setSpouse(spouse);  
        tb.setCountry(country);  
        return tb;  
    }  
    @Bean  
    private TestBean privateInstance() {  
        return new TestBean("privateInstance", i++);  
    }  
    @Bean  
    @RequestScope  
    public TestBean requestScopedInstance() {  
        return new TestBean("requestScopedInstance", 3);  
    }  
}  
```
  
L'exemple attribue automatiquement au paramètre de méthode `String` la valeur de la propriété `age` d'un autre bean nommé `privateInstance`.  
Un SpEL définit la valeur via la notation `#{<expression>}` (pour les annotations `@Value`, un résolveur d'expression préconfiguré recherche les noms de bean).  
  
À partir de Spring 4.3, déclarer un paramètre de méthode de fabrique de type `InjectionPoint` (ou sa sous-classe `DependencyDescriptor`) permet d'accéder au point d'injection demandeur qui déclenche la création du bean.  
Ceci s'applique uniquement à la création de l'instances, pas à l'injection d'une instance existante. Cette fonctionnalité a donc plus de sens pour les fabriques de beans prototypes.  
  
```java  
@Component  
public class FactoryMethodComponent {  
    @Bean @Scope("prototype")  
    public TestBean prototypeInstance(InjectionPoint injectionPoint) {  
        return new TestBean("prototypeInstance for " + injectionPoint.getMember());  
    }  
}  
```
  
Les méthodes `@Bean` d'un `@Component` standard sont traitées différemment de leurs homologues dans une classe `@Configuration`.  
- Les classes `@Configuration` bénéficient du mécansime CGLIB par lequel l'invocation de méthodes à l'intérieur de méthodes `@Bean` passe par le conteneur et appelle les références des objets collaborants (qui peuvent être des intercepteurs proxy).  
- En revanche, l'invocation d'une méthode à l'intérieur d'une méthode `@Bean` d'une classe `@Component` simple a une sémantique Java standard, sans traitement CGLIB spécial.  
  
Les méthodes `@Bean` statiques sont appelées sans instancier leur classe contenante et les beans résultants sont initialisés tôt dans le cycle de vie du conteneur.  
Les post-processeurs (de type `BeanFactoryPostProcessor` ou `BeanPostProcessor`) doivent éviter de déclencher d'autres parties de la configuration s'ils sont initialisés de cette façon.  
  
Les méthodes `@Bean` statiques ne sont jamais interceptées par le conteneur en raison de limitations techniques: l'extension CGLIB ne peut remplacer que les méthodes non statiques.  
En conséquence, un appel d'une méthode `@Bean` à une autre méthode `@Bean` statique a une sémantique Java standard et résulte en une instance indépendante renvoyée par la méthode elle-même.  
  
La visibilité des méthodes `@Bean` n'a pas d'impact sur la définition de bean dans le conteneur.  
Cependant, les méthodes `@Bean` des classes `@Configuration` doivent être remplaçables (elles ne doivent pas être `private` ni `final`).  
  
Les méthodes `@Bean` sont également découvertes dans les superclasses d'un composant de configuration donné, ainsi que sur les méthodes par défaut des interfaces implémentées par le composant.  
  
Enfin, une classe peut contenir plusieurs méthodes `@Bean` pour le même type de bean, à appeler en fonction des dépendances disponibles au moment de l'exécution.  
La variante avec le plus grand nombre de dépendances satisfaites est choisie, de manière analogue à la sélection entre plusieurs constructeurs `@Autowired`.  
  
### Attribution d'un nom aux composants détectés automatiquement  
  
Un composant détecté lors du processus de scannage a son nom généré par la stratégie `BeanNameGenerator` connue de cet analyseur.  
Par défaut, toute annotation de stéréotype (@Component, @Repository, @Service et @Controller) qui contient une valeur de nom fournit ainsi ce nom à la définition de bean correspondante.  
  
Si une telle annotation ne contient aucune valeur de nom, le générateur par défaut renvoie le nom de classe non qualifié et non capitalisé.  
Exemple pour les classes de composants suivantes, les noms seraient `myMovieLister` et `movieFinderImpl`
  
```java  
@Service("myMovieLister")  
public class SimpleMovieLister {}  
  
@Repository  
public class MovieFinderImpl implements MovieFinder {}  
```
  
Pour fournir une stratégie de nommage personnalisée :  
- implémenter l'interface `BeanNameGenerator` avec un constructeur sans argument.  
- ensuite, indiquer le nom de classe complet lors de la configuration du scanner  
  
> À partir de Spring 5.2.3, le `FullyQualifiedAnnotationBeanNameGenerator` peut être utilisé afin d'éliminer les conflits de dénomination en raison de plusieurs composants ayant le même nom de classe non qualifié  
  
```java  
@Configuration  
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)  
public class AppConfig {}  
```
  
En général, il vaut mieux spécifier le nom avec l'annotation si d'autres composants doivent y faire des références explicites.  
Sinon, les noms générés automatiquement conviennent lorsque le conteneur est responsable du câblage.  
  
  
### Etendue des composants détectés automatiquement  
  
La portée par défaut et la plus courante pour les composants détectés automatiquement est singleton.  
Cependant une portée différente peut être spécifiée par l'annotation `@Scope` en fournissant le nom de la portée  
  
```java  
@Scope("prototype")  
@Repository  
public class MovieFinderImpl implements MovieFinder {}  
```
  
Les annotations `@Scope` ne sont introspectées que sur des classes concrètes de composants détectés ou sur les méthodes `@Bean`.  
  
Contrairement aux définitions de bean XML, il n'y a pas de notion d'héritage de définition de bean.  
  
Il est également possible de composer des annotations de portée personnalisée en utilisant l'approche de méta-annotation de Spring.  
> Exemple : une annotation personnalisée méta-annotée avec `@Scope("prototype")`, déclarant éventuellement un proxy-mode personnalisé.  
  
Les configurations suivantes entraîne des proxys dynamiques JDK standard pour les portées prototypes  
  
```java  
@Configuration  
@ComponentScan(basePackages = "org.example", scopedProxy = ScopedProxyMode.INTERFACES)  
public class AppConfig {}  
```
  
```xml  
<beans>  
    <context:component-scan base-package="org.example" scoped-proxy="interfaces"/>  
</beans>  
```
  
### Métadonnées de qualifieur avec les annotations  
  
Cette section illustre l'utilisation de `@Qualifier` et des qualifieurs personnalisées pour un contrôle précis des candidats au câblage automatique.  
Dans le cas  de la détection des composants par scannage du chemin de classe, on peut fournir les métadonnées de qualifieur avec des annotations sur la classe candidate.  
  
```java  
@Component  
@Qualifier("Action")  
public class ActionMovieCatalog implements MovieCatalog {}  
```
  
```java  
@Component  
@Genre("Action")  
public class ActionMovieCatalog implements MovieCatalog {}  
```
  
```java  
@Component  
@Offline  
public class CachingMovieCatalog implements MovieCatalog {}  
```
  
Comme pour la plupart des définitions de beans par annotations, les métadonnées sont liées à la classe elle-même, tandis que l'utilisation de XML permet de fournir plusieurs beans du même type en variant leurs qualifieurs, car les métadonnées sont fournies par instance plutôt que par classe.  
  
### Générer un index sur les composants candidats  
  
Bien que le scannage des chemins de classe soit rapide, les performances de démarrage peuvent être améliorées en créant une liste de candidats à la compilation.  
Dans ce mode, tous les modules cibles du scannage de composants doivent utiliser ce mécanisme.  
  
Pour générer l'index, il faut ajouter la dépendance supplémentaire `spring-context-indexer` aux modules contenant les composants cibles.  
  
```xml  
<dependencies>  
    <dependency>  
        <groupId>org.springframework</groupId>  
        <artifactId>spring-context-indexer</artifactId>  
        <version>5.3.1</version>  
        <optional>true</optional>  
    </dependency>  
</dependencies>  
```
  
Ce processus génère un fichier `META-INF/spring.components` inclus dans le fichier jar.  
L'index est activé automatiquement lorsqu'un `META-INF/spring.components` est trouvé sur le chemin de classe. Si un index est partiellement disponible pour certaines bibliothèques mais n'a pas pu être construit pour l'ensemble de l'application, on peut revenir à un scannage de chemin de classes standard (comme si aucun index n'était présent du tout) en définissant `spring.index.ignore=true` dans un fichier `spring.properties` à la racine du chemin de classe.  
  
  
## Utilisation des annotations standard JSR 330  
  
Spring prend en charge les annotations standard JSR-330 (injection de dépendances).  
Pour les utiliser, il faut avoir les fichiers jar appropriés dans le chemin de classe.  
Avec Maven, l'artefact `javax.inject` est disponible dans le référentiel standard  
  
```xml  
<dependency>  
    <groupId>javax.inject</groupId>  
    <artifactId>javax.inject</artifactId>  
    <version>1</version>  
</dependency>  
```
  
### Injection de dépendances avec `@Inject` et `@Named`
  
Au lieu de `@Autowired`, l'équivalent `@Inject` peut être utilisé  
  
  
`@Inject` peut être utilisée au niveau d'un champ, d'une méthode ou d'un constructeur (idem `Autowired`).  
  
Par ailleurs, le point d'injection peut être déclaré en tant que `Provider`, ce qui permet un accès différé ou à la demande à des beans de portée plus courte via un appel `Provider.get()`.  
  
```java  
import javax.inject.Inject;  
import javax.inject.Provider;  
  
public class SimpleMovieLister {  
    private Provider<MovieFinder> movieFinder;  
    @Inject  
    public void setMovieFinder(Provider<MovieFinder> movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
    public void listMovies() {  
        this.movieFinder.get().findMovies(...);  
    }  
}  
```
  
Pour utiliser un nom qualifié sur la dépendance à injecter, utiliser l'annotation `@Named`
  
```java  
import javax.inject.Inject;  
import javax.inject.Named;  
  
public class SimpleMovieLister {  
    private MovieFinder movieFinder;  
    @Inject  
    public void setMovieFinder(@Named("main") MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
Comme avec `@Autowired`, `@Inject` peut être utilisé avec `java.util.Optional` ou `@Nullable`.  
D'autant plus que `@Inject` n'a pas d'attribut obligatoire.  
  
```java  
public class SimpleMovieLister {  
    @Inject  
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {  
        // ...  
    }  
}  
```
  
```java  
public class SimpleMovieLister {  
    @Inject  
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {  
        // ...  
    }  
}  
```
  
### `@Named` et `@ManagedBean` : équivalents standard à `@Component`
  
```java  
import javax.inject.Inject;  
import javax.inject.Named;  
  
@Named("movieListener")  // @ManagedBean("movieListener") could be used as well  
public class SimpleMovieLister {  
    private MovieFinder movieFinder;  
    @Inject  
    public void setMovieFinder(MovieFinder movieFinder) {  
        this.movieFinder = movieFinder;  
    }  
}  
```
  
Comme pour `@Component`, `@Named` peut s'utiliser sans spécifier le nom  
Avec `@Named` ou `@ManagedBean`, le scannage des composants s'effectue de la même manière qu'avec les annotations Spring.  
  
Contrairement à `@Component`, les annotations `@Named` et `ManagedBean` ne sont pas composables.  
Il faut utiliser le modèle stéréotypé de Spring pour créer des annotations personnalisées.  
  
  
### Limitations des annotations standard JSR-330  
  
Avec les annotations standard, certaines fonctionnalités importantes ne sont pas disponibles  
  
| Spring | javax.inject | Restrictions / commentaires javax.inject |  
|--|--|--|  
| `@Autowired` | `@Inject` | `@Inject` n'a pas d'attribut `required`. Peut s'utiliser `Optional` à la place. |  
| `@Component` | `@Named` | JSR-330 ne fournit pas de modèle composable |  
| `@Scope("singleton")` | `@Singleton` | La portée par défaut du JSR-330 est prototype. Cependant, pour la cohérence, un bean JSR-330 déclaré dans Spring est un singleton par défaut. Pour une étendue autre que singleton, il faut utiliser l'annotation `@Scope` de Spring. `javax.inject` fournit également une annotation `@Scope` mais uniquement destiné à créer des annotations personnalisées. |  
| `@Qualifier` | `@Qualifier` / `@Named` | `javax.inject.Qualifier` est une méta-annotation pour créer des qualifieurs personnalisés. Les qualifieurs concrets sont associés à `javax.inject.Named`.|  
| `@Value` | - | Pas d'équivalent |  
| `@Required` | - | Pas d'équivalent |  
| `@Lazy` | - | Pas d'équivalent |  
| `ObjectFactory` | `Provider` | `javax.inject.Provider` est une alternative à `ObjectFactory`, avec un nom de méthode `get()` plus court. Il peut aussi s'utiliser avec `@Autowired` ou avec des constructeurs et des méthodes de définition non annotés.|  
  
  
## Configuration de conteneur basée sur Java  
  
### Concepts de base: `@Bean` et `@Configuration`
  
L'annotation `@Bean` indique qu'une méthode instancie, configure et initialise un nouvel objet à gérer par le conteneur Spring IoC.  
On peut utiliser des méthodes annotées `@Bean` avec n'importe quel Spring `@Component`.  
Cependant, ils sont le plus souvent utilisés avec les beans `@Configuration`.  
  
L'annotation `@Configuration` sur une classe indique qu'elle est une source de définitions de beans.  
Les classes `@Configuration` permettent de définir les interdépendances en appelant d'autres méthodes `@Bean` de la même classe.  
  
**Mode `@Configuration` complète vs mode `@Bean` allégé**  
  
Les méthodes `@Bean` déclarées dans des classes non annotées avec `@Configuration` sont considérées en mode allégé.  
Contrairement au mode `@Configuration` complète, les méthodes `@Bean` en mode allégé ne doivent pas déclarer de dépendances inter-bean.  
Au lieu de ça, elles peuvent opérer sur l'état interne de leur composant contenant et sur les arguments qu'elles déclarent.  
Une telle méthode `@Bean` ne doit donc pas invoquer d'autres méthodes `@Bean`.  
L'effet secondaire est qu'aucune sous-classification CGLIB ne s'applique à l'exécution, il n'y a donc pas de limitations en termes de conception de classe (la classe contenant peut être finale et ainsi de suite).  
  
Dans les scénarios courants, les méthodes `@Bean` sont déclarées dans les classes de `@Configuration` en mode complet, ce qui garantit que les appels de méthodes croisées soient redirigés vers le conteneur.  
Ceci évite que la même méthode `@Bean` ne soit invoquée plusieurs fois via un appel Java normal et réduit les bogues difficiles à localiser en mode allégé.  
  
  
### Instanciation du conteneur Spring avec `AnnotationConfigApplicationContext`
  
Cette implémentation de `ApplicationContext` accepte en entrée les classes `@Configuration`, les classes `@Component` simples et les classes annotées avec les métadonnées JSR-330.  
- Chaque classe `@Configuration` fournie en entrée ainsi que toutes les méthodes `@Bean` qu'elle déclare sont enregistrées en tant que définitions de beans.  
- Les classes `@Component` et JSR-330 fournies sont également enregistrées en tant que définitions de bean en supposant l'utilisation d'annotations DI telles que `@Autowired` ou `@Inject` dans ces classes si nécessaire.  
  
#### Construction simple  
  
Elle permet une utilisation totalement sans XML du conteneur Spring  
  
```java  
public static void main(String[] args) {  
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);  
    MyService myService = ctx.getBean(MyService.class);  
    myService.doStuff();  
}  
```
  
#### Construction programmatique avec `register(Class<?>...)`
  
`AnnotationConfigApplicationContext` peut être instancié à l'aide d'un constructeur sans argument, puis configuré avec la méthode `register()`.  
Approche particulièrement utile pour créer par programme un `AnnotationConfigApplicationContext`.  
  
```java  
public static void main(String[] args) {  
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();  
    ctx.register(AppConfig.class, OtherConfig.class);  
    ctx.register(AdditionalConfig.class);  
    ctx.refresh();  
    MyService myService = ctx.getBean(MyService.class);  
    myService.doStuff();  
}  
```
  
#### Activation du scannage de composants avec `scan(String...)`
  
`AnnotationConfigApplicationContext` expose la méthode scan(String...) pour permettre la fonctionnalité de détection des composants par scannage des chemins de classes  
  
```java  
public static void main(String[] args) {  
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();  
    ctx.scan("com.acme");  
    ctx.refresh();  
    MyService myService = ctx.getBean(MyService.class);  
}  
```
  
Les classes `@Configuration` étant méta-annotées avec `@Component`, elles sont candidates pour le scannage des composants.  
L'appel à `scan()` suivi de `refresh()` suffit donc à enregistrer ces classes et leurs méthodes `@Bean` en tant que définitions de beans dans le conteneur.  
  
  
#### Prise en charge des applications Web avec `AnnotationConfigWebApplicationContext`
  
Une variante `WebApplicationContext` de `AnnotationConfigApplicationContext` est disponible avec `AnnotationConfigWebApplicationContext`.  
Cette implémentation s'utilise pour la configuration de l'écouteur de servlet Spring `ContextLoaderListener`, la servlet Spring MVC `DispatcherServlet`, ou autres.  
  
L'extrait `web.xml` suivant configure une application Web Spring MVC typique (à noter, l'utilisation du paramètre `contextClass` dans les attributs `<context-param>` et `<init-param>`):  
  
```xml  
<web-app>  
    <!-- Configure ContextLoaderListener to use AnnotationConfigWebApplicationContext  
        instead of the default XmlWebApplicationContext -->  
    <context-param>  
        <param-name>contextClass</param-name>  
        <param-value>  
            org.springframework.web.context.support.AnnotationConfigWebApplicationContext  
        </param-value>  
    </context-param>  
  
    <!-- Configuration locations must consist of one or more comma- or space-delimited  
        fully-qualified @Configuration classes. Fully-qualified packages may also be  
        specified for component-scanning -->  
    <context-param>  
        <param-name>contextConfigLocation</param-name>  
        <param-value>com.acme.AppConfig</param-value>  
    </context-param>  
  
    <!-- Bootstrap the root application context as usual using ContextLoaderListener -->  
    <listener>  
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>  
    </listener>  
  
    <!-- Declare a Spring MVC DispatcherServlet as usual -->  
    <servlet>  
        <servlet-name>dispatcher</servlet-name>  
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>  
        <!-- Configure DispatcherServlet to use AnnotationConfigWebApplicationContext  
            instead of the default XmlWebApplicationContext -->  
        <init-param>  
            <param-name>contextClass</param-name>  
            <param-value>  
                org.springframework.web.context.support.AnnotationConfigWebApplicationContext  
            </param-value>  
        </init-param>  
        <!-- Again, config locations must consist of one or more comma- or space-delimited  
            and fully-qualified @Configuration classes -->  
        <init-param>  
            <param-name>contextConfigLocation</param-name>  
            <param-value>com.acme.web.MvcConfig</param-value>  
        </init-param>  
    </servlet>  
  
    <!-- map all requests for /app/* to the dispatcher servlet -->  
    <servlet-mapping>  
        <servlet-name>dispatcher</servlet-name>  
        <url-pattern>/app/*</url-pattern>  
    </servlet-mapping>  
</web-app>  
```
  
### Utilisation de l'annotation `@Bean`
  
`@Bean` est une annotation de niveau méthode et est analogue à l'élément XML `<bean/>`.  
L'annotation prend en charge certains attributs proposés par `<bean/>`, tels que: `init-method`, `destroy-method`, `autowiring` et `name`.  
  
#### Déclarer un bean  
  
L'annotation `@Bean` enregistre une définition de bean dans un `ApplicationContext` du type spécifié comme valeur de retour de la méthode.  
Par défaut, le nom du bean est le même que le nom de la méthode.  
  
```java  
@Configuration  
public class AppConfig {  
    @Bean  
    public TransferServiceImpl transferService() {  
        return new TransferServiceImpl();  
    }  
}  
```
  
Cette déclaration retourne une instance de bean nommée `transferService` disponible dans `ApplicationContext` et liée au type `TransferServiceImpl`
  
`transferService -> com.acme.TransferServiceImpl`
  
Une méthode `@Bean` peut être déclarée avec un type de retour d'interface mais ça limite la visibilité du bean au type d'interface spécifié.  
Si l'on fait systématiquement référence aux types des dépendances par une interface, les retours `@Bean` peuvent adopter en toute sécurité cette décision de conception.  
Cependant, pour les composants qui implémentent plusieurs interfaces ou qui sont potentiellement référencés par leur type d'implémentation concrète, il est plus sûr de déclarer le type de retour le plus spécifique possible.  
  
#### Dépendances de méthode `@Bean`
  
Une méthode annotée `@Bean` peut avoir un nombre arbitraire de paramètres décrivant les dépendances requises pour construire ce bean.  
Par exemple, si `TransferService` nécessite un `AccountRepository`, cette dépendance se matérialise avec un paramètre de méthode  
  
```java  
@Configuration  
public class AppConfig {  
    @Bean  
    public TransferService transferService(AccountRepository accountRepository) {  
        return new TransferServiceImpl(accountRepository);  
    }  
}  
```
  
Le mécanisme de résolution est identique à l'injection de dépendances basée sur un constructeur.  
  
  
#### Prise en charge de rappels de cycle de vie  
  
Les classes définies avec l'annotation `@Bean` prennent en charge les rappels de cycle de vie normaux et peuvent utiliser les annotations `@PostConstruct` et `@PreDestroy`
  
Les rappels de cycle de vie Spring réguliers sont également entièrement pris en charge.  
Si un bean implémente `InitializingBean`, `DisposableBean` ou `Lifecycle`, leurs méthodes respectives sont appelées par le conteneur.  
  
L'ensemble standard d'interfaces `*Aware` (`BeanFactoryAware`, `BeanNameAware`, `MessageSourceAware`, `ApplicationContextAware`, etc.) sont également pris en charge.  
  
L'annotation `@Bean` permet de spécifier des méthodes de rappel arbitraires, (comme les attributs `init-method` et `destroy-method` de Spring XML)  
  
```java  
@Configuration  
public class AppConfig {  
    @Bean(initMethod = "init")  
    public BeanOne beanOne() {  
        return new BeanOne();  
    }  
    @Bean(destroyMethod = "cleanup")  
    public BeanTwo beanTwo() {  
        return new BeanTwo();  
    }  
}  
```
  
Par défaut, les beans définis avec la configuration Java qui ont une méthode publique de fermeture ou d'arrêt sont automatiquement enrôlés avec un rappel de destruction. Pour ne pas qu'elle soit appelée lorsque le conteneur s'arrête, on peut ajouter `@Bean(destroyMethod = "")` à la définition de bean.  
  
#### Utilisation de l'annotation `@Scope`
  
On peut spécifier une portée spécifique pour les beans définis avec l'annotation `@Bean`.  
La portée par défaut est singleton, mais peut être remplacée avec l'annotation `@Scope`
  
```java  
@Configuration  
public class MyConfiguration {  
    @Bean  
    @Scope("prototype")  
    public Encryptor encryptor() {  
       //...  
    }  
}  
```
  
##### `@Scope` et `scoped-proxy`
  
Spring offre un moyen pratique de travailler avec des dépendances étendues via des proxys étendus.  
Le moyen le plus simple de créer un tel proxy lors de l'utilisation de la configuration XML est l'élément `<aop:scoped-proxy/>`.  
La configuration des beans en Java avec une annotation `@Scope` offre une prise en charge équivalente avec l'attribut `proxyMode`.  
La valeur par défaut est sans proxy (ScopedProxyMode.NO), mais on peut spécifier `ScopedProxyMode.TARGET_CLASS` ou `ScopedProxyMode.INTERFACES`.  
  
```java  
// an HTTP Session-scoped bean exposed as a proxy  
@Bean  
@SessionScope(proxyMode = ScopedProxyMode.INTERFACES)  
public UserPreferences userPreferences() {  
    return new UserPreferences();  
}  
  
```
  
#### Personnalisation de la dénomination des beans  
  
Par défaut, les classes de configuration utilisent le nom de la méthode `@Bean` comme nom du bean résultant. Cette fonctionnalité peut être remplacée par l'attribut `name`
  
```java  
@Configuration  
public class AppConfig {  
    @Bean(name = "myThing")  
    public Thing thing() {  
        return new Thing();  
    }  
}  
```
  
Il est parfois souhaitable de donner à un seul bean plusieurs noms, également appelés alias de bean.  
L'attribut `name` de l'annotation `@Bean` accepte un tableau `String` à cet effet.  
  
```java  
    @Bean ({"dataSource", "subsystemA-dataSource", "subsystemB-dataSource"})  
    public DataSource dataSource () { ... }  
}  
```
  
### Utilisation de l'annotation `@Configuration`
  
Les classes `@Configuration` déclarent des beans via des méthodes publiques annotées `@Bean`.  
  
#### Injection de dépendances inter-beans  
  
Lorsque les beans ont des dépendances les uns sur les autres, exprimer cette dépendance revient à appeler une méthode `@Bean` à partir d'une autre  
  
```java  
@Configuration  
public class AppConfig {  
    @Bean  
    public BeanOne beanOne() {  
        return new BeanOne(beanTwo());  
    }  
    @Bean  
    public BeanTwo beanTwo() {  
        return new BeanTwo();  
    }  
}  
```
  
Dans l'exemple précédent, `beanOne` reçoit une référence à `beanTwo` via l'injection du constructeur.  
  
Cette méthode de déclaration des dépendances inter-bean ne fonctionne que dans une classe `@Configuration`.  
Il est impossible de déclarer des dépendances interbean dans des classes `@Component` simples.  
  
#### Injection de méthode de recherche  
  
L'injection de méthode de recherche est une fonctionnalité avancée rarement utilisée.  
C'est utile dans le cas d'un bean singleton ayant une dépendance sur un bean prototype.  
L'utilisation de Java pour ce type de configuration fournit un moyen naturel d'implémenter ce modèle.  
  
```java  
public abstract class CommandManager {  
    public Object process(Object commandState) {  
        Command command = createCommand();  
        command.setState(commandState);  
        return command.execute();  
    }  
    protected abstract Command createCommand();  
}  
```
  
Avec la configuration Java, une sous-classe de `CommandManager` implémente la méthode abstraite `createCommand()` de telle sorte à fournir systématiquement un nouvel objet (prototype).  
  
```java  
@Bean  
@Scope("prototype")  
public AsyncCommand asyncCommand() {  
    AsyncCommand command = new AsyncCommand();  
    return command;  
}  
@Bean  
public CommandManager commandManager() {  
    // return new anonymous implementation of CommandManager with createCommand()  
    // overridden to return a new prototype Command object  
    return new CommandManager() {  
        protected Command createCommand() {  
            return asyncCommand();  
        }  
    }  
}  
`````
  
#### Informations sur le fonctionnement interne de la configuration Java  
  
L'exemple suivant montre une méthode annotée `@Bean` qui est appelée deux fois:  
  
```java  
@Configuration  
public class AppConfig {  
    @Bean  
    public ClientService clientService1() {  
        ClientServiceImpl clientService = new ClientServiceImpl();  
        clientService.setClientDao(clientDao());  
        return clientService;  
    }  
    @Bean  
    public ClientService clientService2() {  
        ClientServiceImpl clientService = new ClientServiceImpl();  
        clientService.setClientDao(clientDao());  
        return clientService;  
    }  
    @Bean  
    public ClientDao clientDao() {  
        return new ClientDaoImpl();  
    }  
}  
```
  
La méthode `clientDao()` est appelée dans `clientService1()` et dans `clientService2()`.  
Étant donné qu'elle crée et renvoie une nouvelle instance de `ClientDaoImpl`, on peut s'attendre à avoir deux instances (une pour chaque service).  
C'est là que la magie entre en jeu: toutes les classes `@Configuration` sont sous-classées au démarrage avec CGLIB.  
Dans la sous-classe, chaque méthode `@Bean` enfant cherche d'abord dans le conteneur parmi les beans mis en cache avant d'appeler la méthode parente et de créer une nouvelle instance.  
Ce comportement s'applique au beans de portée singleton.  
  
Des restrictions sont dues au fait que CGLIB ajoute dynamiquement des fonctionnalités au démarrage:  
- les classes de `@Configuration` et les méthodes `@Bean` ne doivent pas être `final`.  
- les méthodes `@Bean` dans une classe `@Configuration` ne doivent pas être `private`
  
Pour éviter ces limitations imposées par CGLIB, on peut déclarer les méthodes `@Bean` sur des classes `@Component` simples à la place. Mais les appels de méthodes `@Bean` croisées ne sont pas alors interceptés.  
  
Tous les constructeurs sont autorisés sur les classes de configuration, y compris l'utilisation de `@Autowired` ou d'un constructeur unique avec des arguments injectés par défaut.  
  
  
### Composition de configurations basées sur Java  
  
La configuration Java de Spring permet de composer des annotations, ce qui peut réduire la complexité.  
  
#### Utilisation de l'annotation `@Import`
  
L'annotation `@Import` permet de charger des définitions `@Bean` à partir d'autres classes de configuration  
  
```java  
@Configuration  
public class ConfigA {  
    @Bean  
    public A a() {  
        return new A();  
    }  
}  
  
@Configuration  
@Import(ConfigA.class)  
public class ConfigB {  
    @Bean  
    public B b() {  
        return new B();  
    }  
}  
```
  
Lors de l'instanciation du contexte, seul `ConfigB` doit être fourni explicitement.  
Cette approche simplifie l'instanciation du conteneur, car une seule classe doit être traitée.  
  
`@Import` prend également en charge les références aux classes `@Component` standards, analogues à la méthode `AnnotationConfigApplicationContext.register(...)`.  
Utile si on souhaite éviter le scannage des composants, en utilisant quelques classes comme points d'entrée pour définir tous les composants.  
  
#### Injection de dépendances sur les définitions `@Bean` importées  
  
Dans la plupart des scénarios pratiques, les beans ont des dépendances les uns sur les autres dans les classes de configuration.  
- Avec la config XML, aucun compilateur n'est impliqué et en déclarant `ref="someBean"`, on peut faire confiance à Spring pour résoudre la dépendance lors de l'initialisation du conteneur.  
- Avec les classes `@Configuration`, le compilateur Java impose des contraintes sur le modèle de configuration et les références à d'autres beans doivent respecter une syntaxe Java valide.  
  
Une méthode `@Bean` peut avoir un nombre arbitraire de paramètres décrivant les dépendances.  
On Considère un scénario avec plusieurs classes `@Configuration`, chacune dépendant de beans déclarés dans les autres:  
  
```java  
@Configuration  
public class ServiceConfig {  
    @Bean  
    public TransferService transferService(AccountRepository accountRepository) {  
        return new TransferServiceImpl(accountRepository);  
    }  
}  
  
@Configuration  
public class RepositoryConfig {  
    @Bean  
    public AccountRepository accountRepository(DataSource dataSource) {  
        return new JdbcAccountRepository(dataSource);  
    }  
}  
  
@Configuration  
@Import({ServiceConfig.class, RepositoryConfig.class})  
public class SystemTestConfig {  
    @Bean  
    public DataSource dataSource() {  
        // return new DataSource  
    }  
}  
  
public static void main(String[] args) {  
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);  
    // everything wires up across configuration classes...  
    TransferService transferService = ctx.getBean(TransferService.class);  
}  
```
  
Autre façon d'obtenir le même résultat :  
Les classes `@Configuration` sont des beans dans le conteneur et bénéficient donc de l'injection `@Autowired`, `@Value` et des mêmes fonctionnalités que tout autre bean.  
  
> Les classes @Configuration sont traitées tôt lors de l'initialisation du contexte, et forcer une dépendance à être injectée de cette manière peut conduire à une initialisation précoce inattendue.  
> Dans la mesure du possible, il vaut mieux recourir à l'injection basée sur des paramètres  
  
> Les définitions `BeanPostProcessor` et `BeanFactoryPostProcessor` déclarées via `@Bean` doivent généralement provenir de méthodes `@Bean` statiques, afin d'éviter l'instanciation de leur classe de configuration contenante.  
> Sinon, `@Autowired` et `@Value` risquent de ne pas fonctionner sur la classe de configuration elle-même, car elle est instanciée avant `AutowiredAnnotationBeanPostProcessor`.  
  
Comme il est ambigu de déterminer quels sont les composants qui configurent les beans injectés, on peut envisager d'injecter les classes de configuration elles-mêmes.:  
  
```java  
@Configuration  
public class ServiceConfig {  
    @Autowired  
    private RepositoryConfig repositoryConfig;  
    @Bean  
    public TransferService transferService() {  
        // navigate 'through' the config class to the @Bean method!  
        return new TransferServiceImpl(repositoryConfig.accountRepository());  
    }  
}  
```
  
L'inconvénient est que les classes de configuration sont fortement couplées entre elles.  
  
> Pour influencer l'ordre de création de certains beans, on peut déclarer certains d'entre eux comme `@Lazy` ou comme `@DependsOn` sur certains autres beans  
  
#### Inclure conditionnellement les classes `@Configuration` ou les méthodes `@Bean`
  
Il est souvent utile d'activer ou de désactiver conditionnellement une classe `@Configuration` complète ou même des méthodes `@Bean` individuelles, en fonction d'un état système arbitraire.  
  
Un exemple courant est l'utilisation de l'annotation `@Profile` pour activer les beans uniquement lorsqu'un profil spécifique est actif.  
L'annotation `@Profile` est implémentée à partir d'une annotation flexible appelée `@Conditional`.  
  
L'annotation `@Conditional` spécifie des implémentations `org.springframework.context.annotation.Condition` qui doivent être respectées avant l'enregistrement d'un `@Bean`.  
  
L'interface `Condition` fournit une méthode `matches(...)` qui renvoie vrai ou faux.  
Par exemple, le code suivant montre l'implémentation réelle de la condition utilisée pour `@Profile`
  
```java  
@Override  
public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {  
    MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());  
    if (attrs != null) {  
        for (Object value : attrs.get("value")) {  
            if (context.getEnvironment().acceptsProfiles(((String[]) value))) {  
                return true;  
            }  
        }  
        return false;  
    }  
    return true;  
}  
```
  
#### Mélange de configuration XML et de configuration Java  
  
Dans le cas où XML est pratique ou nécessaire, on peut instancier le conteneur en utilisant `AnnotationConfigApplicationContext` et l'annotation `@ImportResource` pour importer du XML.  
  
Cela permet une approche centrée sur Java de la configuration et réduit le XML au strict minimum.  
L'exemple suivant comprend une classe de configuration, un fichier XML définissant un bean, un fichier de propriétés et la classe principale.  
  
```java  
@Configuration  
@ImportResource("classpath:/com/acme/properties-config.xml")  
public class AppConfig {  
    @Value("${jdbc.url}")  
    private String url;  
    @Value("${jdbc.username}")  
    private String username;  
    @Value("${jdbc.password}")  
    private String password;  
    @Bean  
    public DataSource dataSource() {  
        return new DriverManagerDataSource(url, username, password);  
    }  
}  
```
  
```xml  
<!-- properties-config.xml -->  
<beans>  
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>  
</beans>  
```
  
```properties  
#jdbc.properties  
jdbc.url=jdbc:hsqldb:hsql://localhost/xdb  
jdbc.username=sa  
jdbc.password=  
```
  
```java  
public static void main(String[] args) {  
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);  
    TransferService transferService = ctx.getBean(TransferService.class);  
}  
```
  
## Abstraction de l'environnement  
  
  
  
  
  
