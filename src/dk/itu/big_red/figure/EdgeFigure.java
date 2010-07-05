package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.XYLayout;

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
		graphics.pushState();
		try {
			graphics.setAlpha(32);
			graphics.setBackgroundColor(ColorConstants.darkGreen);
			graphics.fillRectangle(getClientArea());
		} finally {
			graphics.popState();
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}

}
