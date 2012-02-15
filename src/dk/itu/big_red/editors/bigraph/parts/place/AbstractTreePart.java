package dk.itu.big_red.editors.bigraph.parts.place;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;

import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.utilities.ui.UI;

public abstract class AbstractTreePart extends AbstractTreeEditPart implements PropertyChangeListener {
	@Override
	public ModelObject getModel() {
		return (ModelObject)super.getModel();
	}
	
	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
    	refresh();
	}
	
	@Override
	public DragTracker getDragTracker(Request req) {
		return new SelectEditPartTracker(this);
	}
	
	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			try {
				UI.getWorkbenchPage().showView(IPageLayout.ID_PROP_SHEET);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract ImageDescriptor getImageDescriptor();
	
	@Override
	protected Image getImage() {
		return (Image)getImageDescriptor().createResource(UI.getDisplay());
	}
}
