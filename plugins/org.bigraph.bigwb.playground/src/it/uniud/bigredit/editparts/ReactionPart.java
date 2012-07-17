package it.uniud.bigredit.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import it.uniud.bigredit.figure.ReactionFiguren;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;
import it.uniud.bigredit.policy.LayoutableDeletePolicy;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;






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
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
		//installEditPolicy( EditPolicy.COMPONENT_ROLE, new DeletePolicy() );
	}
	
	@Override
	public void refreshVisuals()
	{
		Reaction model = getModel();
		
		Rectangle constraint = ((BRS) getParent().getModel())
				.getChildrenConstraint(model);
		//System.out.println("constraint in refreshVisual" + constraint.toString());
		((ReactionFiguren)getFigure()).setConstraint(constraint);// new Rectangle (100,100,400,300));
	}
	
	@Override
	public List<Bigraph> getModelChildren() {
		List<Bigraph> r = new ArrayList<Bigraph>();
		if(getModel().getReactum() !=null){
			r.add(getModel().getReactum());
		}
		if(getModel().getRedex() !=null){
			r.add(getModel().getRedex());
		}
				//Lists.group(getModel().getChildren(), Bigraph.class);
		//Collections.reverse(r);
		return r;
	}
	
	

	public String getToolTip() {
		// TODO Auto-generated method stub
		return "ReactionRule";
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//super.propertyChange(evt);
		String prop = evt.getPropertyName();
		if (prop.equals(Reaction.PROPERTY_RULE)) {
			
			refreshVisuals();
			refreshChildren();
		}
		
		if (prop.equals(Reaction.PROPERTY_RULE_LAYOUT)){
			refreshChildren();
			refreshVisuals();
		}
		
		
		if (prop.equals(BRS.PROPERTY_LAYOUT)) {
			refreshChildren();
			refreshVisuals();
		}
		if (evt.getPropertyName().equals(Container.PROPERTY_CHILD)) {
			refreshChildren();
		}
		/*if (prop.equals(Bigraph.PROPERTY_BOUNDARY)) {
			refreshChildren();
			refreshVisuals();
			getParent().refresh();
		}*/

		if (evt.getSource() == getModel()) {
			if (prop.equals(Container.PROPERTY_CHILD)) {
				refreshChildren();
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
