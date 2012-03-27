package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.Reaction;
import it.uniud.bigredit.figure.ReactionFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

//import uniud.bigredit.policy.LayoutPolicy;

import dk.itu.big_red.editors.bigraph.parts.ContainerPart;


public class ReactionPart extends ContainerPart {
	
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

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}


}
