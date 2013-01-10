package dk.itu.big_red.editors.bigraph.parts;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Root;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.RootFigure;

/**
 * RootParts represent {@link Root}s, the containers immediately below the
 * {@link Bigraph}.
 * @see Root
 * @author alec
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
		super.createEditPolicies();
		
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		RootFigure figure = (RootFigure)getFigure();
		Root model = getModel();

		figure.setName(model.getName());
	}
	
	@Override
	public List<? extends Layoutable> getModelChildren() {
		return new ArrayList<Layoutable>(getModel().getChildren());
	}
	
	@Override
	public String getToolTip() {
		return "Root " + getModel().getName();
	}
}
