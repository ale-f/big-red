package dk.itu.big_red.model.assistants;

/**
 * Classes implementing <strong>IPropertyProvider</strong> have a generic
 * interface for retrieving their properties.
 * @author alec
 */
public interface IPropertyProvider {
	/**
	 * Gets a named property's value from this {@link IPropertyProvider}.
	 * @param name a property name
	 * @return the value of the named property (which can be
	 * <code>null</code>), or <code>null</code> if this object does not
	 * have the named property
	 */
	public Object getProperty(String name);
}