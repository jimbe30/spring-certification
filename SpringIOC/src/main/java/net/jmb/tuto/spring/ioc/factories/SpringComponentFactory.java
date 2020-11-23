package net.jmb.tuto.spring.ioc.factories;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringComponentFactory implements IComponentFactory, ApplicationContextAware {
	
	private ApplicationContext applicationContext;
		

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public <T> T getComponent(Class<T> clss) {
		return applicationContext.getBean(clss);
	}

	@Override
	public Object getComponent(String name) {
		return applicationContext.getBean(name);
	}

	@Override
	public <T> T getComponent(String name, Class<T> clss) {
		return applicationContext.getBean(name, clss);
	}
	
	

	
}
