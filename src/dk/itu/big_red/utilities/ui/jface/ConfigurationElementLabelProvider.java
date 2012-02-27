package dk.itu.big_red.utilities.ui.jface;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

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
				URL u = FileLocator.find(exb, new Path(icon), null);
				if (u != null) {
					try {
						return new Image(
							PlatformUI.getWorkbench().getDisplay(),
							u.openStream());
					} catch (IOException e) {
						return null;
					}
				}
			}
		}
		return null;
	}
}
