package dk.itu.big_red.model.assistants;

/**
 * <q>Pay no attention to the man behind the curtain!</q>
 * @author alec
 */
public interface IPropertyProviders {
	public interface IPropertyProviderProxy {
		public IPropertyProvider getProvider(IPropertyProvider o);
	}
	
	public interface IPropertyProvider {
		public Object getProperty(String name);
	}
}
