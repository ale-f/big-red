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
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.utilities.ui.UI;

/**
 * The AbstractPart is the base class for most of the objects in the bigraph
 * model. It provides sensible default implementations of the abstract methods
 * from {@link AbstractGraphicalEditPart}, and also some generally-useful
 * functionality, like receiving property notifications from model objects.
 * @author alec
 *
 */
public abstract class AbstractPart extends AbstractGraphicalEditPart implements PropertyChangeListener, IBigraphPart {
	/**
	 * Gets the model object, cast to a {@link Layoutable}.
	 */
	@Override
	public Layoutable getModel() {
		return (Layoutable)super.getModel();
	}
	
	@Override
	public AbstractFigure getFigure() {
		return (AbstractFigure)super.getFigure();
	}
	
	@Override
	public Bigraph getBigraph() {
		return getModel().getBigraph();
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
		if (evt.getSource() == getModel()) {
			String property = evt.getPropertyName();
			if (property.equals(Layoutable.PROPERTY_NAME) ||
				property.equals(ModelObject.PROPERTY_COMMENT) ||
				property.equals(Layoutable.PROPERTY_LAYOUT)) {
				refreshVisuals();
			}
		}
	}

	/**
	 * Returns an empty list of {@link Link.Connection}s. {@link PointPart}s
	 * should probably override this method!
	 */
	@Override
	protected List<Link.Connection> getModelSourceConnections() {
        return new ArrayList<Link.Connection>();
    }

	/**
	 * Returns an empty list of {@link Link.Connection}s. {@link LinkPart}s
	 * should probably override this method!
	 */
	@Override
	protected List<Link.Connection> getModelTargetConnections() {
        return new ArrayList<Link.Connection>();
    }

	/**
	 * Returns an empty list of {@link ILayoutable}s. Model objects with
	 * children should probably override this method!
	 */
	@Override
	public List<Layoutable> getModelChildren() {
		return new ArrayList<Layoutable>();
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
		Layoutable model = getModel();
		AbstractFigure figure = getFigure();
		
		figure.setConstraint(model.getLayout().getDraw2DRectangle());
	}
	
	public abstract String getDisplayName();
}
