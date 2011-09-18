package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.EdgeCreationPolicy;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.InnerNameFigure;
import dk.itu.big_red.editors.bigraph.figures.assistants.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.InnerName;

/**
 * NameParts represent {@link InnerName}s, the model objects which define
 * (along with outer names) a bigraph's interface.
 * @see InnerName
 * @author alec
 *
 */
public class InnerNamePart extends PointPart {
	
	@Override
	protected IFigure createFigure() {
		return new InnerNameFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		((InnerNameFigure)getFigure()).setName(getModel().getName());
	}

	@Override
	public Orientation getAnchorOrientation() {
		return Orientation.NORTH;
	}
}
