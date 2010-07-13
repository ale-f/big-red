package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class PortFigure extends AbstractFigure {

	public PortFigure() {
		super();
		
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setBackgroundColor(ColorConstants.red);
			graphics.fillOval(a);
		} finally {
			stop(graphics);
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}
}
