package org.bigraph.model.assistants;

public interface IPropertyProvider {
	boolean hasProperty(Object target, String name);
	Object getProperty(Object target, String name);
	void setProperty(Object target, String name, Object value);
}