package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import dk.itu.big_red.model.interfaces.internal.IPropertyChangeNotifier;
import dk.itu.big_red.util.UI;

public abstract class AbstractTreePart extends AbstractTreeEditPart implements PropertyChangeListener {
	@Override
	public IPropertyChangeNotifier getModel() {
		return (IPropertyChangeNotifier)super.getModel();
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
}
