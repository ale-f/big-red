package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.figures.SiteFigure;
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
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getModel())
			if (evt.getPropertyName().equals(ExtendedDataUtilities.ALIAS))
		    	refreshVisuals();
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		SiteFigure figure = (SiteFigure)getFigure();
		Site model = getModel();

		if (ExtendedDataUtilities.getAlias(model) == null) {
			figure.setName(model.getName(), false);
		} else figure.setName(ExtendedDataUtilities.getAlias(model), true);
	}
	
	@Override
	public String getToolTip() {
		return "Site " + getModel().getName();
	}
}
