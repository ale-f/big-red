package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.Map;

import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProviderProxy;

public class BigraphScratchpad2 implements IPropertyProviderProxy {
	private Map<String, Object> changes = new HashMap<String, Object>();
	
	private String getKey(IPropertyProvider m, String property) {
		return "!" + System.identityHashCode(m) + "!" + property + "!";
	}
	
	public void setValue(IPropertyProvider m, String property, Object newValue) {
		changes.put(getKey(m, property), newValue);
	}
	
	protected Object getValue(IPropertyProvider m, String property) {
		return changes.get(getKey(m, property));
	}
	
	public boolean hasValue(IPropertyProvider m, String property) {
		return changes.containsKey(getKey(m, property));
	}
	
	public BigraphScratchpad2 clear() {
		changes.clear();
		return this;
	}
	
	public final class ModelObjectProxy implements IPropertyProvider {
		private IPropertyProvider object;
		
		public ModelObjectProxy(IPropertyProvider object) {
			this.object = object;
		}

		@Override
		public Object getProperty(String property) {
			return (hasValue(object, property) ? getValue(object, property) :
				object.getProperty(property));
		}
	}
	
	@Override
	public IPropertyProvider getProvider(IPropertyProvider o) {
		return new ModelObjectProxy(o);
	}
}
