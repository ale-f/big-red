package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class RootFigure extends AbstractFigure {
	private Label labelName = new Label();    
    
	public RootFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
	       
		labelName.setForegroundColor(ColorConstants.black);
		add(labelName, 0);
		setConstraint(labelName, new Rectangle(10, 10, -1, -1));
		
		setForegroundColor(ColorConstants.black);
		setBackgroundColor(ColorConstants.white);
	}

	public void setName(String name) {
		labelName.setText(name);
	}

	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillRoundRectangle(a, 20, 20);
		} finally {
			stop(graphics);
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setLineStyle(SWT.LINE_DOT);
			graphics.setForegroundColor(getForegroundColor());
			a.width--; a.height--;
			graphics.drawRoundRectangle(a, 20, 20);
		} finally {
			stop(graphics);
		}
	}
}
