package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.NestedBigraphFigure;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.policy.LayoutPolicy;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.figures.BigraphFigure;
import dk.itu.big_red.editors.bigraph.figures.RootFigure;
import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Root;

//import uniud.bigredit.policy.LayoutPolicy;



public class NestedBigraphPart extends ContainerPart{
	
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	protected IFigure createFigure()
	{
		return new NestedBigraphFigure();
	}
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy( EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy() );
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		
		NestedBigraphFigure figure = (NestedBigraphFigure)getFigure();
		Bigraph model = getModel();
		
		figure.setName(model.getName());
		
	}
	
	@Override
	public List<Layoutable> getModelChildren() {
		return getModel().getChildren();
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getModel()) {
			if (evt.getPropertyName().equals(BRS.PROPERTY_LAYOUT))
				refreshChildren();
		}
	}

}
