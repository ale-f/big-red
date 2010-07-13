package dk.itu.big_red.figure;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import dk.itu.big_red.util.Utility;

public class NameFigure extends AbstractFigure {
	public static final int SITE_FIGURE_DEFWIDTH = 100;
	public static final int SITE_FIGURE_DEFHEIGHT = 50;
	
	private static Font italicFont = null;
	private Label label = new Label();
	
	public NameFigure() {
		super();
		
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
		label.setText("?");
		label.setForegroundColor(ColorConstants.white);
		add(label, 0);
		
	}
	
	@Override
	public void addNotify() {
		if (italicFont == null)
			italicFont = Utility.tweakFont(label.getFont(), SWT.ITALIC);
		label.setFont(italicFont);
	}
	
	@Override
	public void setConstraint(Rectangle r) {
		super.setConstraint(r);
		Dimension s = label.getPreferredSize();
		setConstraint(label, new Rectangle(
				(r.width / 2) - (s.width / 2),
				(r.height / 2) - (s.height / 2), -1, -1));
	}
	
	public void setName(String s) {
		label.setText(s);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.setAlpha(128);
			graphics.fillRectangle(a);
		} finally {
			stop(graphics);
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
	}

}
