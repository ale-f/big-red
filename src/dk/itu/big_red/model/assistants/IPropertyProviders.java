package dk.itu.big_red.model.assistants;

/**
 * <q>Pay no attention to the man behind the curtain!</q>
 * @author alec
 */
public interface IPropertyProviders {
	/**
	 * Classes implementing <strong>IPropertyProviderProxy</strong> can act as
	 * {@link IPropertyProvider}s for more than one object.
	 * @author alec
	 */
	public interface IPropertyProviderProxy {
		public Object getProperty(IPropertyProvider target, String name);
	}
	
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
}
