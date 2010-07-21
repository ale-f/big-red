package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class EdgeFigure extends AbstractFigure {
	
	public EdgeFigure() {
		super();
		
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
	}
	
	public void setToolTip(String content) {
		String labelText = "Edge";
		if (content != null)
			labelText += "\n\n" + content;
		super.setToolTip(labelText);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setAlpha(32);
			graphics.setBackgroundColor(ColorConstants.darkGreen);
			graphics.fillRectangle(a);
		} finally {
			stop(graphics);
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
	}

}
