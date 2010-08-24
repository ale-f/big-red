package dk.itu.big_red.wizards.export.assistants;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class TextExporterLabelProvider extends LabelProvider {
	@Override
	public Image getImage(Object el) {
		if (el instanceof IConfigurationElement) {
			IConfigurationElement element = (IConfigurationElement)el;
			String icon = element.getAttribute("icon");
			if (icon != null) {
				Bundle exb = Platform.getBundle(element.getDeclaringExtension().getNamespaceIdentifier());
				URL l = FileLocator.find(exb, new Path(icon), null);
				if (l != null) {
					try {
						return new Image(null, l.openStream());
					} catch (IOException e) {
						return null;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof IConfigurationElement) {
			return ((IConfigurationElement)element).getAttribute("name");
		} else return super.getText(element);
	}
}
