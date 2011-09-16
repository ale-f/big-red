package dk.itu.big_red.part;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.edit_policies.ILayoutableLayoutPolicy;
import dk.itu.big_red.figure.BigraphFigure;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.util.Utility;

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
	protected IFigure createFigure() {
		return new BigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ILayoutableLayoutPolicy());
	}
	
	@Override
	public List<ILayoutable> getModelChildren() {
		List<ILayoutable> r =
				Utility.groupListByClass(getModel().getChildren(), Edge.class, Object.class);
		Collections.reverse(r);
		return r;
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
