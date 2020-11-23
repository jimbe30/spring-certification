package net.jmb.tuto.spring.ioc.factories;

public interface IComponentFactory {
	
	<T> T getComponent(Class<T> clss);
	
	Object getComponent(String name);
	
	<T> T getComponent(String name, Class<T> clss);

}
