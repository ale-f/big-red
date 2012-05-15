package dk.itu.big_red.model.assistants;

public interface IPropertyProvider {
	public boolean hasProperty(Object target, String name);
	public Object getProperty(Object target, String name);
	public void setProperty(Object target, String name, Object value);
}