package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;

public abstract class AbstractPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
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
	protected List<EdgeConnection> getModelSourceConnections() {
        return new ArrayList<EdgeConnection>();
    }
    
	@Override
	protected List<EdgeConnection> getModelTargetConnections() {
        return new ArrayList<EdgeConnection>();
    }
	
	@Override
	public List<ILayoutable> getModelChildren() {
		return new ArrayList<ILayoutable>();
	}
}
