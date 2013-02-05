package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class EdgeFigure extends AbstractFigure {
	public EdgeFigure() {
		super();
	}
	
	private boolean single = false;
	
	public void setSingle(boolean single) {
		this.single = single;
	}
	
	private boolean render = true;
	
	public void setRender(boolean render) {
		this.render = render;
	}
	
	@Override
	public void setToolTip(String content) {
		String labelText = "Edge";
		if (content != null)
			labelText += "\n\n" + content;
		super.setToolTip(labelText);
	}
	
	@Override
	public void setBackgroundColor(Color bg) {
		super.setBackgroundColor(bg);
		super.setForegroundColor(bg);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		if (!render)
			return;
		Rectangle a = start(graphics);
		try {
			graphics.setAlpha(32);
			graphics.fillRectangle(a);
			
			if (single) {
				graphics.setAlpha(255);
				graphics.drawLine(
						a.getLeft().translate(0, 3),
						a.getRight().translate(0, -3));
			}
		} finally {
			stop(graphics);
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		if (!render)
			return;
		Rectangle a = start(graphics);
		try {
			graphics.setAlpha(64);
			graphics.setLineWidth(2);
			graphics.setLineStyle(SWT.LINE_SOLID);
			graphics.drawRectangle(a.shrink(1, 1));
		} finally {
			stop(graphics);
		}
	}
}
