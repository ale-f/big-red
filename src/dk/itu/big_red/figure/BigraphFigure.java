package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class BigraphFigure extends AbstractFigure {
	protected int upperRootBoundary = Integer.MIN_VALUE,
	              lowerOuterNameBoundary = Integer.MAX_VALUE,
	              upperInnerNameBoundary = Integer.MIN_VALUE,
	              lowerRootBoundary = Integer.MAX_VALUE;
	
	public BigraphFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
	}
	
	@Override
	protected void fillShape(Graphics g) {
		Rectangle r = start(g);
		int width = r.width, height = r.height;
		try {
			g.setAlpha(63);
			
			g.setBackgroundColor(ColorConstants.red);
			g.fillRectangle(0, 0, width, lowerOuterNameBoundary);
			
			g.setBackgroundColor(ColorConstants.green);
			g.fillRectangle(0, upperRootBoundary, width, lowerRootBoundary - upperRootBoundary);
			
			g.setBackgroundColor(ColorConstants.blue);
			g.fillRectangle(0, upperInnerNameBoundary, width, height - upperInnerNameBoundary);
			
		} finally {
			stop(g);
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		// TODO Auto-generated method stub

	}

	public void setUpperRootBoundary(int upperRootBoundary) {
		this.upperRootBoundary = upperRootBoundary;
	}

	public void setLowerOuterNameBoundary(int lowerOuterNameBoundary) {
		this.lowerOuterNameBoundary = lowerOuterNameBoundary;
	}

	public void setUpperInnerNameBoundary(int upperInnerNameBoundary) {
		this.upperInnerNameBoundary = upperInnerNameBoundary;
	}

	public void setLowerRootBoundary(int lowerRootBoundary) {
		this.lowerRootBoundary = lowerRootBoundary;
	}

}
