package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class SiteFigure extends AbstractFigure {
	public static final int SITE_FIGURE_DEFWIDTH = 100;
	public static final int SITE_FIGURE_DEFHEIGHT = 100;
	
	private Label labelName = new Label();    
    
	public SiteFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
	       
		labelName.setForegroundColor(ColorConstants.black);
		add(labelName, 0);
		setConstraint(labelName, new Rectangle(10, 10, -1, -1));
		
		setForegroundColor(ColorConstants.black);
		setBackgroundColor(ColorConstants.lightGray);
	}

	public void setNumber(int number) {
		labelName.setText(Integer.toString(number));
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