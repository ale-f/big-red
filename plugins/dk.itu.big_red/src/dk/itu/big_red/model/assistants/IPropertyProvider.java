package dk.itu.big_red.model.assistants;

public interface IPropertyProvider {
	public boolean hasProperty(Object target, String name);
	public Object getProperty(Object target, String name);
}