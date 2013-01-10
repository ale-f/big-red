package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.NestedBigraphFigure;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;
import it.uniud.bigredit.policy.LayoutableDeletePolicy;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.editors.bigraph.parts.ContainerPart;

//import uniud.bigredit.policy.LayoutPolicy;

public class NestedBigraphPart extends ContainerPart {

	// NestedBigraphFigure figureModel;

	private int outernamePoint=40;
	private int innernamePoint=40;
	
	@Override
	public Bigraph getModel() {
		return (Bigraph) super.getModel();
	}

	@Override
	protected IFigure createFigure() {
		// figureModel=
		// return figureModel;//new NestedBigraphFigure();
		
		return new NestedBigraphFigure();// NestedBigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new LayoutableDeletePolicy());
	}

	/*
	 * @Override public NestedBigraphFigure getFigure(){ return figureModel; }
	 */

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		// NestedBigraphFigure figure = (NestedBigraphFigure)getFigure();

		NestedBigraphFigure figure = (NestedBigraphFigure) getFigure();
		Bigraph model = getModel();

		figure.setName(model.getName());
		Rectangle constraint= new Rectangle (0,0,100,100);
		if (getParent() instanceof BRSPart) {
			
			constraint = ((BRS) getParent().getModel())
					.getChildrenConstraint(model);
			
			
			
			//System.out.println("constraint in refreshVisual"+ constraint.toString());
			figure.setConstraint(constraint);// new Rectangle (100,100,400,300));
			 figure.setInnerLine(constraint.height- innernamePoint);//.getUpperInnerNameBoundary());
			 figure.setOuterLine(outernamePoint);
			
		} else if (getParent() instanceof ReactionPart) {
			constraint = ((Reaction) getParent().getModel())
					.getChildConstraint(model);
			//System.out.println("constraint in refreshVisual" + constraint.toString());
			figure.setConstraint(constraint);// new Rectangle (100,100,400,300));
			 figure.setInnerLine(constraint.height- innernamePoint);//.getUpperInnerNameBoundary());
			 figure.setOuterLine(outernamePoint);
		}


	}

	@Override
	public List<? extends Layoutable> getModelChildren() {
		return new ArrayList<Layoutable>(getModel().getChildren());
	}

	@Override
	public String getToolTip() {

		return "Bigraph ";// + getModel().getName();

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String prop = evt.getPropertyName();
		if (prop.equals(BRS.PROPERTY_LAYOUT)) {
			refreshChildren();
			refreshVisuals();
		}
		if (prop.equals(Reaction.PROPERTY_RULE)) {
			
			refreshVisuals();
			getParent().refresh();
		}
		
		if (prop.equals(Reaction.PROPERTY_RULE_LAYOUT)){
			refreshVisuals();
			getParent().refresh();
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

	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also register to
	 * receive property change notifications from the model object.
	 */
	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
		
		if (getParent() instanceof BRSPart) {
			((BRS) getParent().getModel()).addPropertyChangeListener(this);
		} else if (getParent() instanceof ReactionPart) {
			((Reaction) getParent().getModel()).addPropertyChangeListener(this);
		}
		
	}

	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the model object's property change notifications.
	 */
	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		
		if (getParent() instanceof BRSPart) {
			((BRS) getParent().getModel()).removePropertyChangeListener(this);
		} else if (getParent() instanceof ReactionPart) {
			((Reaction) getParent().getModel()).removePropertyChangeListener(this);
		}
		super.deactivate();
	}

}
