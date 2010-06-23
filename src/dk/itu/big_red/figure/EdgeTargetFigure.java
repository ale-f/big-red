package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;

public class EdgeTargetFigure extends AbstractFigure {
	
	public EdgeTargetFigure() {
		super();
		
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		try {
		} finally {
			graphics.popState();
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}

}
