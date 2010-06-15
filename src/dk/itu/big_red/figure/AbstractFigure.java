package dk.itu.big_red.figure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public abstract class AbstractFigure extends Shape {
	public AbstractFigure() {
		/*
		 * All AbstractFigures should look good!
		 */
		setAntialias(SWT.ON);
		setOpaque(false);
	}
	
	protected Rectangle getAncestorConstraint(int generations) {
		IFigure ancestor = this;
		while (generations > 0) {
			ancestor = ancestor.getParent();
			if (ancestor == null) {
				return null;
			} else {
				generations--;
			}
		}
		return (Rectangle) ancestor.getParent().getLayoutManager().getConstraint(ancestor);
	}
	
	protected Rectangle getConstraint() {
		return getAncestorConstraint(0);
	}
	
	protected Rectangle getConstraintCopy() {
		return new Rectangle(getConstraint());
	}

	protected Rectangle getConstraintCopy(Point p) {
		Rectangle result = getConstraintCopy();
		result.x = p.x; result.y = p.y;
		return result;
	}
	
	public Point getTotalOffset() {
		Rectangle constraint;
		Point offset = new Point();
		int generation = 0;
		
		while (true) {
			constraint = getAncestorConstraint(generation);
			if (constraint == null) break;
			offset.x += constraint.x; offset.y += constraint.y;
			generation++;
		}
		
		return offset;
	}
	
	public void setConstraint(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}
	
	private Rectangle rootConstraint;
	
	public Rectangle getRootConstraint() {
		return rootConstraint;
	}
	
	public void setRootConstraint(Rectangle rootConstraint) {
		this.rootConstraint = rootConstraint;
	}
	
	public void setToolTip(String content) {
		Label label = new Label(content);
		label.setBorder(new MarginBorder(4));
		setToolTip(label);
	}
}
