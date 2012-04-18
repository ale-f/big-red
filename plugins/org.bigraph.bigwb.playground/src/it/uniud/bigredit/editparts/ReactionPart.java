package it.uniud.bigredit.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import it.uniud.bigredit.figure.ReactionFiguren;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;



public class ReactionPart extends AbstractGraphicalEditPart implements PropertyChangeListener{
	
	@Override
	protected IFigure createFigure()
	{
		
		return new ReactionFiguren();
	}
	
	@Override
	public Reaction getModel() {
		return (Reaction) super.getModel();
	}
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		//installEditPolicy( EditPolicy.COMPONENT_ROLE, new DeletePolicy() );
	}
	
	@Override
	public void refreshChildren()
	{
		super.refreshChildren();
		Reaction model = ( Reaction ) getModel();
		
		Rectangle constraint = ((BRS) getParent().getModel())
				.getChildrenConstraint(model);
		System.out.println("constraint in refreshVisual"
				+ constraint.toString());
		((ReactionFiguren)getFigure()).setConstraint(constraint);// new Rectangle (100,100,400,300));
	}

	public String getToolTip() {
		// TODO Auto-generated method stub
		return "ReactionRule";
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//super.propertyChange(evt);
		String prop = evt.getPropertyName();
		if (prop.equals(BRS.PROPERTY_LAYOUT)) {
			refreshChildren();
			refreshVisuals();
		}
		if (evt.getPropertyName().equals(Container.PROPERTY_CHILD)) {
			refreshChildren();
		}
		if (prop.equals(Bigraph.PROPERTY_BOUNDARY)) {
			refreshChildren();
			refreshVisuals();
			getParent().refresh();
		}

		if (evt.getSource() == getModel()) {
			if (prop.equals(Container.PROPERTY_CHILD)) {
				refreshChildren();
				refreshVisuals();
			} else if (prop.equals(Bigraph.PROPERTY_BOUNDARY)) {
				refreshVisuals();
			}
		}
		
	}
	
	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
		((BRS) getParent().getModel()).addPropertyChangeListener(this);
	}

	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the model object's property change notifications.
	 */
	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		((BRS) getParent().getModel()).removePropertyChangeListener(this);
		super.deactivate();
	}



}
