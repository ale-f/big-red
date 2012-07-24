package dk.itu.big_red.model;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.eclipse.draw2d.geometry.Rectangle;

public final class BigraphBoundaryState {
	private int
		upperRootBoundary = Integer.MIN_VALUE,
		lowerOuterNameBoundary = Integer.MAX_VALUE,
		upperInnerNameBoundary = Integer.MIN_VALUE,
		lowerRootBoundary = Integer.MAX_VALUE;
	
	public BigraphBoundaryState() {
	}
	
	public BigraphBoundaryState(Bigraph b) {
		refresh(b);
	}
	
	public BigraphBoundaryState(PropertyScratchpad context, Bigraph b) {
		refresh(context, b);
	}
	
	public boolean refresh(Bigraph b) {
		return refresh(null, b);
	}
	
	public boolean refresh(PropertyScratchpad context, Bigraph b) {
		int
			upperRootBoundary = Integer.MIN_VALUE,
			lowerOuterNameBoundary = Integer.MAX_VALUE,
			upperInnerNameBoundary = Integer.MIN_VALUE,
			lowerRootBoundary = Integer.MAX_VALUE;
		
		for (Layoutable i : b.getChildren(context)) {
			if (i instanceof Edge)
				continue;
			Rectangle r = LayoutUtilities.getLayout(context, i);
			int top = r.y(), bottom = r.y() + r.height();
			if (i instanceof OuterName) {
				if (bottom > upperRootBoundary)
					upperRootBoundary = bottom;
			} else if (i instanceof Root) {
				if (top < lowerOuterNameBoundary)
					lowerOuterNameBoundary = top;
				if (bottom > upperInnerNameBoundary)
					upperInnerNameBoundary = bottom;
			} else if (i instanceof InnerName) {
				if (top < lowerRootBoundary)
					lowerRootBoundary = top;
			}
		}
		
		boolean changed =
				(upperRootBoundary != this.upperRootBoundary) ||
				(lowerOuterNameBoundary != this.lowerOuterNameBoundary) ||
				(upperInnerNameBoundary != this.upperInnerNameBoundary) ||
				(lowerRootBoundary != this.lowerRootBoundary);
		
		if (upperRootBoundary != this.upperRootBoundary)
			this.upperRootBoundary = upperRootBoundary;
		if (lowerOuterNameBoundary != this.lowerOuterNameBoundary)
			this.lowerOuterNameBoundary = lowerOuterNameBoundary;
		if (upperInnerNameBoundary != this.upperInnerNameBoundary)
			this.upperInnerNameBoundary = upperInnerNameBoundary;
		if (lowerRootBoundary != this.lowerRootBoundary)
			this.lowerRootBoundary = lowerRootBoundary;
		
		return changed;
	}
	
	public int getUpperRootBoundary() {
		return upperRootBoundary;
	}

	public int getLowerOuterNameBoundary() {
		return lowerOuterNameBoundary;
	}

	public int getUpperInnerNameBoundary() {
		return upperInnerNameBoundary;
	}

	public int getLowerRootBoundary() {
		return lowerRootBoundary;
	}
	
	public static final int
		B_NONE = 0,
		B_LON = 1,
		B_UR = 1 << 1,
		B_LR = 1 << 2,
		B_R = B_UR | B_LR,
		B_UIN = 1 << 3;
	
	public int getBoundaryState(Rectangle r) {
		int top = r.y(), bottom = r.bottom();
		return
			(top < upperRootBoundary ? B_UR : 0) |
			(bottom > lowerRootBoundary ? B_LR : 0) |
			(bottom > lowerOuterNameBoundary ? B_LON : 0) |
			(top < upperInnerNameBoundary ? B_UIN : 0);
	}
}