package org.bigraph.model.assistants;

import org.bigraph.model.ModelObject;

public abstract class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	public static <T> T getProperty(PropertyScratchpad context, ModelObject o,
			String name, Class<T> klass) {
		if (o != null && name != null) {
			try {
				return klass.cast(o.getExtendedData(context, name));
			} catch (ClassCastException ex) {
				return null;
			}
		} else return null;
	}
	
	public static void setProperty(PropertyScratchpad context, ModelObject o,
			String name, Object value) {
		if (o == null || name == null)
			return;
		o.setExtendedData(context, name, value);
	}
}
