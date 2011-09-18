package dk.itu.big_red.part;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.edit_policies.LayoutableDeletePolicy;
import dk.itu.big_red.figure.SiteFigure;
import dk.itu.big_red.model.Site;

public class SitePart extends AbstractPart {
	@Override
	public Site getModel() {
		return (Site)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new SiteFigure();
	}

	@Override
	protected void createEditPolicies() {
		/*
		 * Sites aren't allowed to contain anything; they're empty holes that
		 * can only be populated by importing another bigraph. As such, the
		 * AppEditLayoutPolicy isn't installed here.
		 * */
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}

	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		SiteFigure figure = (SiteFigure)getFigure();
		Site model = getModel();

		figure.setName(model.getName());
		figure.setToolTip("Site " + model.getName());
		figure.setConstraint(model.getLayout());
	}
}
