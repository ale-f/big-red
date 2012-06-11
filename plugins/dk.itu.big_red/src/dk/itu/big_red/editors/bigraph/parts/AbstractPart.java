package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.editors.assistants.Colour;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.editors.utilities.ModelPropertySource;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.utilities.ui.ColorWrapper;
import dk.itu.big_red.utilities.ui.UI;

/**
 * The AbstractPart is the base class for most of the objects in the bigraph
 * model. It provides sensible default implementations of the abstract methods
 * from {@link AbstractGraphicalEditPart}, and also some generally-useful
 * functionality, like receiving property notifications from model objects.
 * @author alec
 *
 */
public abstract class AbstractPart extends AbstractGraphicalEditPart
implements PropertyChangeListener, IBigraphPart {
	/**
	 * Gets the model object, cast to a {@link Layoutable}.
	 */
	@Override
	public Layoutable getModel() {
		return (Layoutable)super.getModel();
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key == IPropertySource.class) {
			return new ModelPropertySource(getModel());
		} else return super.getAdapter(key);
	}
	
	@Override
	public AbstractFigure getFigure() {
		return (AbstractFigure)super.getFigure();
	}
	
	@Override
	public Bigraph getBigraph() {
		return getModel().getBigraph();
	}
	
	private ColorWrapper
		fill = new ColorWrapper(), outline = new ColorWrapper();
	
	protected Color getFill(Colour c) {
		return fill.update(c);
	}
	
	protected Color getOutline(Colour c) {
		return outline.update(c);
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
		getFill(null);
		getOutline(null);
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
				ExtendedDataUtilities.COMMENT.equals(property) ||
				property.equals(ExtendedDataUtilities.LAYOUT)) {
				refreshVisuals();
			}
		}
	}

	/**
	 * Returns an empty list of {@link Link.Connection}s. {@link PointPart}s
	 * should probably override this method!
	 */
	@Override
	protected List<LinkPart.Connection> getModelSourceConnections() {
		return Collections.emptyList();
    }

	/**
	 * Returns an empty list of {@link Link.Connection}s. {@link LinkPart}s
	 * should probably override this method!
	 */
	@Override
	protected List<LinkPart.Connection> getModelTargetConnections() {
		return Collections.emptyList();
    }

	/**
	 * Returns an empty list of {@link ILayoutable}s. Model objects with
	 * children should probably override this method!
	 */
	@Override
	public List<Layoutable> getModelChildren() {
		return Collections.emptyList();
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
	
	@Override
	protected void refreshVisuals() {
		Layoutable model = getModel();
		AbstractFigure figure = getFigure();
		figure.setConstraint(ExtendedDataUtilities.getLayout(model));
		String
			comment = ExtendedDataUtilities.getComment(model),
			tooltip = getToolTip();
		figure.setToolTip(comment == null ?
				tooltip : tooltip + "\n\n" + comment);
	}
	
	public abstract String getToolTip();
}
