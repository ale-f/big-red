package dk.itu.big_red.editors.utilities;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

public interface IRedPropertySource extends IPropertySource {
	@Override
	Object getPropertyValue(Object id);
	
	Resolver getResolver();
	
	/**
	 * Creates and returns an {@link IChangeDescriptor} which will set the
	 * value of a property.
	 * @param id the property's name
	 * @param newValue the new value of the property
	 * @return an {@link IChangeDescriptor}, or <code>null</code> if the
	 * property name was not recognised
	 */
	IChangeDescriptor setPropertyValueChange(Object id, Object newValue);
	
	/**
	 * Creates and returns an {@link IChangeDescriptor} which will reset the
	 * value of a property to its default value.
	 * @param id the property's name
	 * @return an {@link IChangeDescriptor}, or <code>null</code> if the
	 * property name was not recognised
	 */
	IChangeDescriptor resetPropertyValueChange(Object id);
}
