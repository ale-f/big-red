package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import dk.itu.big_red.utilities.ui.UI;

public class NameFigure extends AbstractFigure {
	private static Font italicFont = null;
	private Label label = new Label();
	
	public NameFigure() {
		super();
		
		label.setText("?");
		label.setForegroundColor(ColorConstants.white);
		add(label, 0);
	}
	
	@Override
	public void addNotify() {
		if (italicFont == null)
			italicFont = UI.tweakFont(label.getFont(), SWT.ITALIC);
		label.setFont(italicFont);
	}
	
	private void repositionLabel() {
		Rectangle r = getConstraint();
		if (r == null)
			return;
		Dimension s = label.getPreferredSize();
		setConstraint(label, new Rectangle(
				(r.width / 2) - (s.width / 2),
				(r.height / 2) - (s.height / 2), -1, -1));
	}
	
	@Override
	public void setConstraint(Rectangle r) {
		super.setConstraint(r);
		repositionLabel();
	}
	
	public void setName(String s) {
		label.setText(s);
		repositionLabel();
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setAlpha(128);
			graphics.fillRectangle(a);
		} finally {
			stop(graphics);
		}
	}
}
