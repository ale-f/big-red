package dk.itu.big_red.part;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.figure.EdgeTargetFigure;

public class EdgeTargetPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		// TODO Auto-generated method stub
		return new EdgeTargetFigure();
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
		
	}

}
