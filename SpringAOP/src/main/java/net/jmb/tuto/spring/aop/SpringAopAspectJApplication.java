package net.jmb.tuto.spring.aop;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import net.jmb.tuto.spring.aop.beans.OperationsInterface;

@Configuration
@ComponentScan(basePackages = "net.jmb.tuto.spring.aop.beans")
@EnableAspectJAutoProxy()
public class SpringAopAspectJApplication {	
	
	@Value("#{environment['test'].concat(' !')}")
	String message;

	public static void main(String[] args) {		
		
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringAopAspectJApplication.class);
		
		applicationContext.getBean(OperationsInterface.class).carre(5d);		
		applicationContext.getBean(OperationsInterface.class).sommeDesCarres(1d, 2d, 3d, 4d, 5d);		
		applicationContext.getBean(OperationsInterface.class).carreDeLaSomme(1d, 2d, 3d, 4d, 5d);

		String message = applicationContext.getBean(SpringAopAspectJApplication.class).message;
		System.out.println(message);
		
		applicationContext.close();
	}
	
	@Aspect
	@Component
	public class AspectExample {
		
		@Pointcut("execution(double net.jmb..Oper*.*(..))")
		public void operationPointcut() {}
		
		@Around(value = "operationPointcut() && (args(Double, ..) || args(Double[], ..))")
//		@Around(value = "within(net.jmb..*)")
//		@Around(value = "target(net.jmb.tuto.spring.aop.SpringAopAPIApplication.OperationsInterface)")
//		@Around(value = "this(net.jmb.tuto.spring.aop.SpringAopAPIApplication.OperationsInterface)")
		public Number wrapOperation(ProceedingJoinPoint pjp) throws Throwable {
			System.out.println("\nJoinPoint: " + pjp);
			System.out.println("Args: " + Arrays.deepToString(pjp.getArgs()));
			
			Double[] array = (Double[]) Arrays.stream(pjp.getArgs())
				.flatMap(arg -> arg.getClass().isArray() ? Arrays.stream((Object[])arg) : Arrays.stream(new Object[] {arg}))
				.map(arg -> (double) arg + 1)
				.collect(Collectors.toList())
				.toArray(new Double[] {});
			
			Object[] args = array.length > 1 ? new Object[] {array} : array;
			Object proceed = pjp.proceed(args);
			
			System.out.println("Retour proceed(): " + proceed);
			return (Number) proceed;
		}		
	}

	
	

}
