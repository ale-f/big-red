package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class RootFigure extends AbstractFigure {
	public static final int ROOT_FIGURE_DEFWIDTH = 100;
	public static final int ROOT_FIGURE_DEFHEIGHT = 100;
	
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

	public void setNumber(int number) {
		labelName.setText(Integer.toString(number));
	}

	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		try {
			graphics.setBackgroundColor(getBackgroundColor());
			Rectangle nc = getConstraintCopy(getTotalOffset());
			graphics.fillRoundRectangle(nc, 20, 20);
		} finally {
			graphics.popState();
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.pushState();
		try {
			graphics.setLineStyle(SWT.LINE_DOT);
			graphics.setForegroundColor(getForegroundColor());
			Rectangle nc = getConstraintCopy(getTotalOffset());
			nc.width--; nc.height--;
			graphics.drawRoundRectangle(nc, 30, 30);
		} finally {
			graphics.popState();
		}
	}
}
