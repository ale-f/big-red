package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.BigraphFigure;
import dk.itu.big_red.model.BigraphBoundaryState;
import dk.itu.big_red.model.LayoutUtilities;

/**
 * BigraphParts represent {@link Bigraph}s, the top-level container of the
 * model.
 * @see Bigraph
 * @author alec
 */
public class BigraphPart extends ContainerPart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public void activate() {
		super.activate();
		for (ModelObject i : getModel().getChildren())
			i.addPropertyChangeListener(this);
		getViewer().addPropertyChangeListener(this);
		refreshBoundaries();
	}
	
	@Override
	public void deactivate() {
		getViewer().removePropertyChangeListener(this);
		for (ModelObject i : getModel().getChildren())
			i.removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (evt.getSource() == getViewer()) {
			if (AbstractGEFEditor.PROPERTY_DISPLAY_GUIDES.equals(prop))
				refreshVisuals();
			return;
		}
		super.propertyChange(evt);
		if (evt.getSource() == getModel()) {
			if (Container.PROPERTY_CHILD.equals(prop)) {
				ModelObject
					oldValue = (ModelObject)evt.getOldValue(),
					newValue = (ModelObject)evt.getNewValue();
				if (oldValue == null && newValue != null) {
					newValue.addPropertyChangeListener(this);
				} else if (oldValue != null && newValue == null) {
					oldValue.removePropertyChangeListener(this);
				}
				refreshChildren();
				refreshBoundaries();
			}
		} else if (evt.getSource() instanceof Layoutable) {
			Layoutable l = (Layoutable)evt.getSource();
			if (l.getParent() == getModel() &&
					LayoutUtilities.LAYOUT.equals(prop))
					refreshBoundaries();
		}
	}
	
	@Override
	protected IFigure createFigure() {
		return new BigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
	}
	
	@Override
	public List<Layoutable> getModelChildren() {
		List<Layoutable> nc = new ArrayList<Layoutable>();
		for (Layoutable i : getModel().getChildren()) {
			if (i instanceof Edge) {
				nc.add(i);
			} else nc.add(0, i);
		}
		return nc;
	}

	private BigraphBoundaryState bs = new BigraphBoundaryState();
	
	protected void refreshBoundaries() {
		if (bs.refresh(getModel()))
			refreshVisuals();
	}
	
	@Override
	protected void refreshVisuals() {
		BigraphFigure figure = (BigraphFigure)getFigure();
		
		Object displayGuidesObj = getViewer().getProperty(
				AbstractGEFEditor.PROPERTY_DISPLAY_GUIDES);
		boolean displayGuides = (displayGuidesObj instanceof Boolean ?
				(Boolean)displayGuidesObj : true);
		
		figure.setDisplayGuides(displayGuides);
		if (displayGuides) {
			figure.setUpperRootBoundary(bs.getUpperRootBoundary());
			figure.setLowerOuterNameBoundary(bs.getLowerOuterNameBoundary());
			figure.setUpperInnerNameBoundary(bs.getUpperInnerNameBoundary());
			figure.setLowerRootBoundary(bs.getLowerRootBoundary());
		}
		
		figure.repaint();
	}
	
	@Override
	public String getToolTip() {
		return "Bigraph " + getModel().getName();
	}
}
