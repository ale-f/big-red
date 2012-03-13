package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.Map;


public class PropertyScratchpad implements IPropertyProviderProxy {
	private Map<String, Object> changes = new HashMap<String, Object>();
	
	private String getKey(IPropertyProvider m, String property) {
		return System.identityHashCode(m) + "!" + property;
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
	
	public PropertyScratchpad clear() {
		changes.clear();
		return this;
	}
	
	@Override
	public Object getProperty(IPropertyProvider object, String property) {
		return (hasValue(object, property) ? getValue(object, property) :
			object.getProperty(property));
	}
}
