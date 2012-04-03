package it.uniud.bigredit.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import it.uniud.bigredit.figure.ReactionFigure;
import it.uniud.bigredit.model.Reaction;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

//import uniud.bigredit.policy.LayoutPolicy;

import dk.itu.big_red.editors.bigraph.parts.ContainerPart;


public class ReactionPart extends AbstractGraphicalEditPart implements PropertyChangeListener{
	
	@Override
	protected IFigure createFigure()
	{
		IFigure figure = new ReactionFigure();
		return figure;
	}
	
	@Override
	protected void createEditPolicies()
	{
	//	installEditPolicy( EditPolicy.LAYOUT_ROLE, new LayoutPolicy() );
		//installEditPolicy( EditPolicy.COMPONENT_ROLE, new DeletePolicy() );
	}
	
	@Override
	public void refreshChildren()
	{
		super.refreshChildren();
		Reaction model = ( Reaction ) getModel();
		( ( ReactionFigure )getFigure() ).setChildren( model.getRedex(), model.getReactum() );
	}

	public String getToolTip() {
		// TODO Auto-generated method stub
		return "ReactionRule";
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
