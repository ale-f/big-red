package dk.itu.big_red.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editpolicies.ILayoutableLayoutPolicy;
import dk.itu.big_red.figure.BigraphFigure;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.interfaces.ILayoutable;

/**
 * BigraphParts represent {@link Bigraph}s, the top-level container of the
 * model.
 * @see Bigraph
 * @author alec
 *
 */
public class BigraphPart extends ThingPart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new BigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ILayoutableLayoutPolicy());
	}
	
	public List<ILayoutable> getModelChildren() {
		return getModel().getChildren();
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
}
