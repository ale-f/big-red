package dk.itu.big_red.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.edit_policies.ILayoutableDeletePolicy;
import dk.itu.big_red.editors.edit_policies.ILayoutableLayoutPolicy;
import dk.itu.big_red.figure.RootFigure;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

/**
 * RootParts represent {@link Root}s, the containers immediately below the
 * {@link Bigraph}.
 * @see Root
 * @author alec
 *
 */
public class RootPart extends ContainerPart {
	@Override
	public Root getModel() {
		return (Root)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new RootFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ILayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
	}

	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		RootFigure figure = (RootFigure)getFigure();
		Root model = getModel();

		figure.setName(model.getName());
	}
	
	@Override
	public List<ILayoutable> getModelChildren() {
		return getModel().getChildren();
	}
}
