package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import dk.itu.big_red.utilities.ui.UI;

public class BigraphFigure extends AbstractFigure {
	private boolean displayGuides = true;
	private int upperRootBoundary = Integer.MIN_VALUE,
	            lowerOuterNameBoundary = Integer.MAX_VALUE,
	            upperInnerNameBoundary = Integer.MIN_VALUE,
	            lowerRootBoundary = Integer.MAX_VALUE;

	public void setDisplayGuides(boolean displayGuides) {
		this.displayGuides = displayGuides;
	}
	
	@Override
	protected void outlineShape(Graphics g) {
		if (!displayGuides)
			return;
		
		Rectangle r = start(g);
		int width = r.width;
		try {
			g.setAlpha(63);
			
			g.setLineStyle(SWT.LINE_DASH);
			
			g.setFont(UI.tweakFont(g.getFont(), 8, SWT.ITALIC));
			
			if (lowerOuterNameBoundary < Integer.MAX_VALUE) {
				g.drawLine(0, lowerOuterNameBoundary, width, lowerOuterNameBoundary);
				g.drawText("outer name boundary", 10, lowerOuterNameBoundary + 2);
			}
			
			if (upperRootBoundary >= 0) {
				g.drawLine(0, upperRootBoundary, width, upperRootBoundary);
				g.drawText("upper root boundary", 10, upperRootBoundary + 2);
			}
			
			if (lowerRootBoundary < Integer.MAX_VALUE) {
				g.drawLine(0, lowerRootBoundary, width, lowerRootBoundary);
				g.drawText("lower root boundary", 10, lowerRootBoundary + 2);
			}
			
			if (upperInnerNameBoundary >= 0) {
				g.drawLine(0, upperInnerNameBoundary, width, upperInnerNameBoundary);
				g.drawText("inner name boundary", 10, upperInnerNameBoundary + 2);
			}
			
			g.getFont().dispose();
		} finally {
			stop(g);
		}
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
