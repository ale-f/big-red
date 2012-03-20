package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

public class PortFigure extends AbstractFigure {
	public PortFigure() {
		super();

		setBackgroundColor(ColorConstants.red);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.fillOval(a);
		} finally {
			stop(graphics);
		}
	}
}
