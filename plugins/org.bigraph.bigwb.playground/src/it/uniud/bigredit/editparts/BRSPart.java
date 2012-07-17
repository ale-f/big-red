package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.BRSFigure;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.policy.LayoutPolicy;
import it.uniud.bigredit.policy.LayoutableLayoutPolicy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.editors.bigraph.figures.BigraphFigure;
import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.editors.utilities.ModelPropertySource;





public class BRSPart extends AbstractGraphicalEditPart implements PropertyChangeListener{
	
	
	@Override
	public void activate() {
		super.activate();
		((BRS)getModel()).addPropertyChangeListener(this);
	}
	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the model object's property change notifications.
	 */
	@Override
	public void deactivate() {
		((BRS)getModel()).removePropertyChangeListener(this);
		super.deactivate();
	}
	
	
	
	@Override
	public BRS getModel() {
		return (BRS)super.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (evt.getSource() == getModel()) {
			//System.out.println("evt.getSource() == getModel() " + prop);
			if (prop.equals(Container.PROPERTY_CHILD)) {
				refreshChildren();
			} else if(prop.equals(BRS.PROPERTY_LAYOUT)){
				refreshChildren();
				refreshVisuals();
			}else if(prop.equals(BRS.PROPERTY_PARENT)){
				refreshChildren();
				refreshVisuals();
			}
		}
	}
	
	@Override
	public List<ModelObject> getModelChildren() {
		List<ModelObject> r = ((BRS)getModel()).getChildren();
				//Lists.group(getModel().getChildren(), Bigraph.class);
		//Collections.reverse(r);
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

	
	public String getToolTip() {
		return "BRS " + ((BRS)getModel()).getName();
	}
	
	

}
