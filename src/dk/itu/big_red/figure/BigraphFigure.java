package dk.itu.big_red.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;

public class BigraphFigure extends AbstractFigure {

	public BigraphFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void outlineShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}

}
