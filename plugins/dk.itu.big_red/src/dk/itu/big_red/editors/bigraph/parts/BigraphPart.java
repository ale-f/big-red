package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.BigraphFigure;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;

/**
 * BigraphParts represent {@link Bigraph}s, the top-level container of the
 * model.
 * @see Bigraph
 * @author alec
 *
 */
public class BigraphPart extends ContainerPart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String prop = evt.getPropertyName();
		if (evt.getSource() == getModel()) {
			if (prop.equals(Container.PROPERTY_CHILD)) {
				refreshChildren();
			} else if (prop.equals(Bigraph.PROPERTY_BOUNDARY)) {
				refreshVisuals();
			}
		}
	}
	
	@Override
	protected IFigure createFigure() {
		return new BigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
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
	
	@Override
	protected void refreshVisuals() {
		Bigraph model = getModel();
		BigraphFigure figure = (BigraphFigure)getFigure();
		
		figure.setLowerOuterNameBoundary(model.getLowerOuterNameBoundary());
		figure.setUpperRootBoundary(model.getUpperRootBoundary());
		figure.setLowerRootBoundary(model.getLowerRootBoundary());
		figure.setUpperInnerNameBoundary(model.getUpperInnerNameBoundary());
		figure.repaint();
	}
	
	@Override
	public String getToolTip() {
		return "Bigraph " + getModel().getName();
	}
}
