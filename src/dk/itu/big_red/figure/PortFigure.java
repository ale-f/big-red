package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;

public class PortFigure extends AbstractFigure {

	public PortFigure() {
		super();
		
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		try {
			graphics.setBackgroundColor(ColorConstants.red);
			graphics.fillOval(getConstraintCopy(getTotalOffset()));
		} finally {
			graphics.popState();
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}
}
