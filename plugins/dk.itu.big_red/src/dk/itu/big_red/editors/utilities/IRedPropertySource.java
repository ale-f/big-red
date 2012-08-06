package dk.itu.big_red.editors.utilities;

import org.bigraph.model.changes.IChange;
import org.eclipse.ui.views.properties.IPropertySource;

public interface IRedPropertySource extends IPropertySource {
	@Override
	Object getPropertyValue(Object id);
	
	/**
	 * Creates and returns an {@link IChange} which will set the value of a
	 * property.
	 * @param id the property's name
	 * @param newValue the new value of the property
	 * @return an {@link IChange}, or <code>null</code> if the property name
	 * was not recognised
	 */
	IChange setPropertyValueChange(Object id, Object newValue);
	
	/**
	 * Creates and returns an {@link IChange} which will reset the value of a
	 * property to its default value.
	 * @param id the property's name
	 * @return an {@link IChange}, or <code>null</code> if the property name
	 * was not recognised
	 */
	IChange resetPropertyValueChange(Object id);
}
