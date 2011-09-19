package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.assistants.LinkConnection;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.util.UI;

/**
 * The AbstractPart is the base class for most of the objects in the bigraph
 * model. It provides sensible default implementations of the abstract methods
 * from {@link AbstractGraphicalEditPart}, and also some generally-useful
 * functionality, like receiving property notifications from model objects.
 * @author alec
 *
 */
public abstract class AbstractPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
	/**
	 * Gets the model object, cast to a {@link LayoutableModelObject}.
	 */
	@Override
	public LayoutableModelObject getModel() {
		return (LayoutableModelObject)super.getModel();
	}
	
	@Override
	public AbstractFigure getFigure() {
		return (AbstractFigure)super.getFigure();
	}
	
	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also register to
	 * receive property change notifications from the model object.
	 */
	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the model object's property change notifications.
	 */
	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	/**
	 * Checks to see if this {@link EditPart}'s <code>PRIMARY_DRAG_ROLE</code>
	 * {@link EditPolicy} is a {@link ResizableEditPolicy}, and - if it is -
	 * reconfigures it to allow or forbid resizing.
	 * @param resizable whether or not this Part should be resizable
	 */
	protected void setResizable(boolean resizable) {
		EditPolicy pol = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pol instanceof ResizableEditPolicy) {
			((ResizableEditPolicy)pol).setResizeDirections(
				(resizable ? PositionConstants.NSEW : 0));
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refresh();
	}

	/**
	 * Returns an empty list of {@link LinkConnection}s. {@link PointPart}s
	 * should probably override this method!
	 */
	@Override
	protected List<LinkConnection> getModelSourceConnections() {
        return new ArrayList<LinkConnection>();
    }

	/**
	 * Returns an empty list of {@link LinkConnection}s. {@link LinkPart}s
	 * should probably override this method!
	 */
	@Override
	protected List<LinkConnection> getModelTargetConnections() {
        return new ArrayList<LinkConnection>();
    }

	/**
	 * Returns an empty list of {@link ILayoutable}s. Model objects with
	 * children should probably override this method!
	 */
	@Override
	public List<LayoutableModelObject> getModelChildren() {
		return new ArrayList<LayoutableModelObject>();
	}
	
	/**
	 * Handles {@link RequestConstants#REQ_OPEN} requests by opening the
	 * property sheet.
	 */
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
	
	/**
	 * Sets the tooltip of this {@link AbstractPart}'s associated {@link
	 * AbstractFigure}, appending the model object's comment (if there is one).
	 * @param tooltip
	 */
	protected void setToolTip(String tooltip) {
		if (getModel().getComment() != null)
			tooltip += "\n\n" + getModel().getComment();
		getFigure().setToolTip(tooltip);
	}
	
	@Override
	protected void refreshVisuals() {
		LayoutableModelObject model = getModel();
		AbstractFigure figure = getFigure();
		
		figure.setConstraint(model.getLayout());
	}
}
