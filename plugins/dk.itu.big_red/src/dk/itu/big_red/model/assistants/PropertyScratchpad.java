package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.Map;

public class PropertyScratchpad implements IPropertyProviderProxy {
	private static final class NNPair {
		private IPropertyProvider object;
		private String property;
		
		private NNPair(IPropertyProvider object, String property) {
			this.object = object;
			this.property = property;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NNPair) {
				NNPair p = (NNPair)obj;
				return
					(object.equals(p.object) && property.equals(p.property));
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return object.hashCode() ^ property.hashCode();
		}
	}
	
	private Map<NNPair, Object> changes = new HashMap<NNPair, Object>();
	
	private NNPair getKey(IPropertyProvider m, String property) {
		return new NNPair(m, property);
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
