package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.GraphFigure;
import it.uniud.bigredit.figure.NestedBigraphFigure;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;

//import uniud.bigredit.policy.LayoutPolicy;



public class NestedBigraphPart extends ContainerPart{
	
	//NestedBigraphFigure figureModel;
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	protected IFigure createFigure()
	{
		//figureModel=
		//return figureModel;//new NestedBigraphFigure();
		return new NestedBigraphFigure();
	}
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy( EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy() );
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	/*@Override
	public NestedBigraphFigure getFigure(){
		return figureModel;
	}*/
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		
		//NestedBigraphFigure figure = (NestedBigraphFigure)getFigure();
		
		NestedBigraphFigure figure = (NestedBigraphFigure)getFigure();
		Bigraph model = getModel();
		
		figure.setName(model.getName());
		figure.setConstraint(new Rectangle (100,100,50,50));
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
			if (evt.getPropertyName().equals(BRS.PROPERTY_LAYOUT)){
				refreshChildren();
				refreshVisuals();
			}
		}
	}

}
