package dk.itu.big_red;



import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.ThingFactory;



public class AppTemplateTransferDropTargetListener extends
		TemplateTransferDropTargetListener {

	public AppTemplateTransferDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}
	
	@Override
	protected CreationFactory getFactory(Object template) {
		return new ThingFactory((Class<?>)template);
	}

}
