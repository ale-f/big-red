package dk.itu.big_red.utilities.ui.jface;

import java.io.InputStream;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import dk.itu.big_red.application.plugin.RedPlugin;

public class ConfigurationElementLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof IConfigurationElement) {
			return ((IConfigurationElement)element).getAttribute("name");
		} else return null;
	}
	
	@Override
	public Image getImage(Object el) {
		if (el instanceof IConfigurationElement) {
			IConfigurationElement element = (IConfigurationElement)el;
			String icon = element.getAttribute("icon");
			if (icon != null) {
				Bundle exb = Platform.getBundle(
					element.getDeclaringExtension().getNamespaceIdentifier());
				InputStream is = RedPlugin.getResource(exb, icon);
				if (is != null)
					return new Image(null, is);
			}
		}
		return null;
	}
}
