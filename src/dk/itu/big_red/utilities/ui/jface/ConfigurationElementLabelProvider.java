package dk.itu.big_red.utilities.ui.jface;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.LabelProvider;

public class ConfigurationElementLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		return ((IConfigurationElement)element).getAttribute("name");
	}
}
