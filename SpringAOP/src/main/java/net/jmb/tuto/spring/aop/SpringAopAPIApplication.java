package net.jmb.tuto.spring.aop;

import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import net.jmb.tuto.spring.aop.beans.OperationsInterface;

@Configuration
@ComponentScan(basePackages = "net.jmb.tuto.spring.aop.beans")
public class SpringAopAPIApplication {	

	public static void main(String[] args) {
		
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringAopAPIApplication.class);
		
		applicationContext.getBean(OperationsInterface.class).carre(5d);		
		applicationContext.getBean(OperationsInterface.class).sommeDesCarres(1d, 2d, 3d, 4d, 5d);		
		applicationContext.getBean(OperationsInterface.class).carreDeLaSomme(1d, 2d, 3d, 4d, 5d);
		
		applicationContext.close();
	}
	
//	@Bean
//	@Primary
//	ProxyFactoryBean operationsProxy0(OperationsInterface operations) {
//		ProxyFactoryBean proxyFactory = new ProxyFactoryBean();
//		proxyFactory.setTarget(operations);
//		proxyFactory.setInterceptorNames("operationsAdvisor");		
//		return proxyFactory;
//	}
//	
//	@Bean
//	OperationsInterface operationsProxy1(OperationsInterface operations) {
//		ProxyFactory proxyFactory = new ProxyFactory(operations);
//		proxyFactory.addAdvisor(operationsAdvisor());
//		return (OperationsInterface) proxyFactory.getProxy();
//	}	
	
	@Bean
	BeanNameAutoProxyCreator operationsProxy2() {		
		BeanNameAutoProxyCreator proxyFactory = new BeanNameAutoProxyCreator();
		proxyFactory.setBeanNames("operations");
		proxyFactory.setInterceptorNames("operationsAdvisor");
		return proxyFactory;
	}
	
	/////////////////////////////////////////////////////
	
	@Bean
	Advisor operationsAdvisor() {
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		pointcut.setMappedNames("*Carre*", "*carre*");		
		DefaultBeanFactoryPointcutAdvisor advisor = new DefaultBeanFactoryPointcutAdvisor();
		advisor.setPointcut(pointcut);
		advisor.setAdvice(operationsMethodInterceptor());		
		return advisor;
	}
	
	@Bean
	MethodInterceptor operationsMethodInterceptor() {
		return new MethodInterceptor() {			
			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				System.out.println("\nMethodInvocation=[" + invocation + "]");
				System.out.println("Args: " + Arrays.deepToString(invocation.getArguments()));
		        Object rval = invocation.proceed();
		        System.out.println("Invocation returned : " + rval);
		        return rval;
			}
		};
	}

}
