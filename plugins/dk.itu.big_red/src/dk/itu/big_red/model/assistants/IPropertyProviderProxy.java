package dk.itu.big_red.model.assistants;

/**
 * Classes implementing <strong>IPropertyProviderProxy</strong> can act as
 * {@link IPropertyProvider}s for more than one object.
 * @author alec
 */
public interface IPropertyProviderProxy {
	public Object getProperty(IPropertyProvider target, String name);
}