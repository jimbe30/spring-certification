package net.jmb.tuto.spring.ioc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AnnotationConfigApp {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextWithAnnotations.xml");		
		
		Object bean = ctx.getBean("bean");
		System.out.println(bean);
		

	}

}
