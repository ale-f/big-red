package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public void setProperty(IPropertyProvider m, String property, Object newValue) {
		if (m != null && property != null)
			changes.put(getKey(m, property), newValue);
	}
	
	public boolean hasProperty(IPropertyProvider m, String property) {
		if (m != null && property != null) {
			return changes.containsKey(getKey(m, property));
		} else return false;
	}
	
	public PropertyScratchpad clear() {
		changes.clear();
		return this;
	}
	
	@Override
	public Object getProperty(IPropertyProvider object, String property) {
		if (object != null && property != null) {
			if (hasProperty(object, property)) {
				return changes.get(getKey(object, property));
			} else return object.getProperty(property);
		} else return null;
	}
	
	protected <T> List<T> getModifiableList(
			IPropertyProvider object, String property) {
		@SuppressWarnings("unchecked")
		List<T> l = (List<T>)getProperty(object, property);
		if (!hasProperty(object, property))
			setProperty(object, property, l = new ArrayList<T>(l));
		return l;
	}
}
