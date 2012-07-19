package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class SiteFigure extends AbstractFigure {
	private Label labelName = new Label();    
    
	public SiteFigure() {
		super();
		
		labelName.setForegroundColor(ColorConstants.black);
		add(labelName, 0);
		setConstraint(labelName, new Rectangle(10, 10, -1, -1));
		
		setForegroundColor(ColorConstants.black);
		setBackgroundColor(ColorConstants.lightGray);
	}
	
	public void setName(String name, boolean alias) {
		labelName.setText(name);
		labelName.setForegroundColor(alias ?
				ColorConstants.white : ColorConstants.black);
	}

	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.fillRoundRectangle(a, 20, 20);
		} finally {
			stop(graphics);
		}
	}
	
	static final float OUTLINE_DASH[] = new float[] { 4, 4 };
	
	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setLineStyle(SWT.LINE_CUSTOM);
			graphics.setLineDash(OUTLINE_DASH);
			a.width--; a.height--;
			graphics.drawRoundRectangle(a, 20, 20);
		} finally {
			stop(graphics);
		}
	}
}