package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.LayoutUtilities;
import dk.itu.big_red.utilities.ui.ColorWrapper;
import dk.itu.big_red.utilities.ui.UI;

import static java.lang.Boolean.TRUE;
import static org.bigraph.model.ModelObject.require;

/**
 * The AbstractPart is the base class for most of the objects in the bigraph
 * model. It provides sensible default implementations of the abstract methods
 * from {@link AbstractGraphicalEditPart}, and also some generally-useful
 * functionality, like receiving property notifications from model objects.
 * @author alec
 */
public abstract class AbstractPart extends AbstractGraphicalEditPart
		implements PropertyChangeListener, IBigraphPart {
	private IPropertySource propertySource;
	
	public static IPropertySource createPropertySource(EditPart e) {
		EditPartFactory f = e.getViewer().getEditPartFactory();
		return (f instanceof IPropertySourceProvider ?
				((IPropertySourceProvider)f).getPropertySource(e.getModel()) :
					null);
	}
	
	protected final IPropertySource getPropertySource() {
		return (propertySource != null ? propertySource :
			(propertySource = createPropertySource(this)));
	}
	
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
			return getPropertySource();
		} else if (key == SnapToHelper.class) {
			ArrayList<SnapToHelper> helpers = new ArrayList<SnapToHelper>();
			EditPartViewer v = getViewer();
			if (TRUE.equals(
					v.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED)))
				helpers.add(new SnapToGeometry(this));
			if (TRUE.equals(v.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED)))
				helpers.add(new SnapToGrid(this));
			if (helpers.size() > 0) {
				return new CompoundSnapToHelper(
						helpers.toArray(new SnapToHelper[0]));
			} else return null;
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
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy("Snap!", new SnapFeedbackPolicy());
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
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getModel()) {
			String property = evt.getPropertyName();
			if (Layoutable.PROPERTY_NAME.equals(property) ||
				ExtendedDataUtilities.COMMENT.equals(property)) {
				refreshVisuals();
			} else if (LayoutUtilities.LAYOUT.equals(property)) {
				refreshVisuals();
				layoutChange(0);
			}
		}
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
	
	protected void refreshLocation() {
		getFigure().setConstraint(LayoutUtilities.getLayout(getModel()));
	}
	
	@Override
	protected void refreshVisuals() {
		Layoutable model = getModel();
		AbstractFigure figure = getFigure();
		refreshLocation();
		String
			comment = ExtendedDataUtilities.getComment(model),
			tooltip = getToolTip();
		figure.setToolTip(comment == null ?
				tooltip : tooltip + "\n\n" + comment);
	}
	
	public abstract String getToolTip();
	
	/**
	 * Returns the edit part corresponding to a given model object.
	 * @param o a model object
	 * @param klass the class of which the edit part must be an instance
	 * @return an edit part, or <code>null</code> if one couldn't be found
	 * &mdash; or if its type was incompatible with <code>klass</code>
	 */
	protected <T extends EditPart> T getPartFor(Object o, Class<T> klass) {
		if (o == null)
			return null;
		return require(getViewer().getEditPartRegistry().get(o), klass);
	}
	
	void layoutChange(int generations) {
		/* do nothing */
	}
}
