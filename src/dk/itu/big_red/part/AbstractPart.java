package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.figure.adornments.PortAnchor;
import dk.itu.big_red.model.*;

public abstract class AbstractPart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {
	@Override
	public Thing getModel() {
		return (Thing)super.getModel();
	}
	
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		/* 
		 * TutoGEF suggests putting this in each of the *Part classes, but it
		 * makes much more sense to have it in the base, surely? 
		 */
	    if (evt.getPropertyName().equals(Thing.PROPERTY_LAYOUT) ||
	    	evt.getPropertyName().equals(Thing.PROPERTY_RENAME)) {
	    	refreshVisuals();
	    } else if (evt.getPropertyName().equals(Thing.PROPERTY_CHILD)) {
	    	refreshChildren();
	    } else if (evt.getPropertyName().equals(Thing.PROPERTY_SOURCE_EDGE)) {
	    	refreshSourceConnections();
	    } else if (evt.getPropertyName().equals(Thing.PROPERTY_TARGET_EDGE)) {
	    	refreshTargetConnections();
	    }
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		((AbstractFigure)getFigure()).setToolTip(getModel().getClass().getSimpleName());
	}
	
	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			try {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				page.showView(IPageLayout.ID_PROP_SHEET);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected List<Edge> getModelSourceConnections() {
        return getModel().getSourceEdges();
    }
    
	@Override
	protected List<Edge> getModelTargetConnections() {
        return getModel().getTargetEdges();
    }
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		if (connection instanceof EdgePart) {
			EdgePart part = (EdgePart)connection;
			Edge edge = part.getModel();
			return new PortAnchor(getFigure(), getModel(), edge.getSourceKey());
		} else return null;
    }
    
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new PortAnchor(getFigure(), null, null);
    }
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		if (connection instanceof EdgePart) {
			EdgePart part = (EdgePart)connection;
			Edge edge = part.getModel();
			return new PortAnchor(getFigure(), getModel(), edge.getTargetKey());
		} else return null;
    }
    
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return new PortAnchor(getFigure(), null, null);
    }
	
	@Override
	public List<Thing> getModelChildren() {
		return new ArrayList<Thing>();
	}
}
