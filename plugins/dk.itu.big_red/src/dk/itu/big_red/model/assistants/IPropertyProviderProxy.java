package dk.itu.big_red.model.assistants;

/**
 * Classes implementing <strong>IPropertyProviderProxy</strong> can act as
 * {@link IPropertyProvider}s for more than one object.
 * @author alec
 */
public interface IPropertyProviderProxy {
	public boolean hasProperty(Object target, String name);
	public Object getProperty(Object target, String name);
}