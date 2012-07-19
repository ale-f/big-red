package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class RootFigure extends AbstractFigure {
	private Label labelName = new Label();    
    
	public RootFigure() {
		super();
		
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
	protected void outlineShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setLineStyle(SWT.LINE_CUSTOM);
			graphics.setLineDash(SiteFigure.OUTLINE_DASH);
			a.width--; a.height--;
			graphics.drawRoundRectangle(a, 20, 20);
		} finally {
			stop(graphics);
		}
	}
}