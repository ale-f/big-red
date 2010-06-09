package dk.itu.big_red.part;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.figure.NodeFigure;
import dk.itu.big_red.figure.PortFigure;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;

public class PortPart extends AbstractGraphicalEditPart {
	@Override
	public Port getModel() {
		return (Port)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new PortFigure();
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		Port model = getModel();
		PortFigure figure = (PortFigure)getFigure();
		
		figure.setLayout(model.getLayout());
	}
}
