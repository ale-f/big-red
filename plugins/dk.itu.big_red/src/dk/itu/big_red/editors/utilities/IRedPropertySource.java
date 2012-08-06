package dk.itu.big_red.editors.utilities;

import org.bigraph.model.changes.IChange;
import org.eclipse.ui.views.properties.IPropertySource;

public interface IRedPropertySource extends IPropertySource {
	@Override
	Object getPropertyValue(Object id);
	IChange setPropertyValueChange(Object id, Object newValue);
	IChange resetPropertyValueChange(Object id);
}
