package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

public class EdgeFigure extends AbstractFigure {
	public EdgeFigure() {
		super();
	}
	
	@Override
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
			graphics.fillRectangle(a);
		} finally {
			stop(graphics);
		}
	}
}
