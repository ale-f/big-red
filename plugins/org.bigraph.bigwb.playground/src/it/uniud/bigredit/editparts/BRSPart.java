package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.BRSFigure;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.policy.LayoutPolicy;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.bigraph.figures.BigraphFigure;
import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;





public class BRSPart extends ContainerPart {
	
	
	@Override
	public BRS getModel() {
		return (BRS)super.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String prop = evt.getPropertyName();
		if (evt.getSource() == getModel()) {
			if (prop.equals(Container.PROPERTY_CHILD)) {
				System.out.println("refresh children");
				refreshChildren();
			} else if (prop.equals(Bigraph.PROPERTY_BOUNDARY)) {
				refreshVisuals();
			}
		}
	}
	
	@Override
	public List<Layoutable> getModelChildren() {
		List<Layoutable> r = getModel().getChildren();
				//Lists.group(getModel().getChildren(), Bigraph.class);
		Collections.reverse(r);
		return r;
	}
	
	@Override
	protected void refreshVisuals() {
		
		figure.repaint();
	}
	
	@Override
	protected IFigure createFigure()
	{
		IFigure figure = new BRSFigure();
		return figure;
	}
	
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy( EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());// new LayoutPolicy() );
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}



}
