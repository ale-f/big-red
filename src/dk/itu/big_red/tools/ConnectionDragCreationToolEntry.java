package dk.itu.big_red.tools;

import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.ConnectionDragCreationTool;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * ConnectionDragCreationToolEntries are very simple wrappers which bind a
 * {@link CreationToolEntry} together with the {@link
 * ConnectionDragCreationTool} class, providing a palette entry for the
 * ConnectionDragCreationTool.
 *
 * @author alec
 *
 */
public class ConnectionDragCreationToolEntry extends CreationToolEntry {

	public ConnectionDragCreationToolEntry(String label, String shortDesc,
			CreationFactory factory, ImageDescriptor iconSmall,
			ImageDescriptor iconLarge) {
		super(label, shortDesc, factory, iconSmall, iconLarge);
		setToolClass(ConnectionDragCreationTool.class);
	}

}
