package dk.itu.big_red.util.geometry;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

public abstract class ReadonlyRectangle {
	public abstract int getX();
	public abstract int getY();
	public abstract int getWidth();
	public abstract int getHeight();
	
	public int getTop() {
		return getY();
	}
	
	public int getBottom() {
		return getY() + getHeight();
	}
	
	public int getLeft() {
		return getX();
	}
	
	public int getRight() {
		return getX() + getWidth();
	}
	
	public Point getTopLeft() {
		return new Point(getLeft(), getTop());
	}
	
	public Point getBottomLeft() {
		return new Point(getLeft(), getBottom());
	}
	
	public Point getBottomRight() {
		return new Point(getRight(), getBottom());
	}
	
	public Point getCenter() {
		return new Point(getX() + (getWidth() / 2),
				getY() + (getHeight() / 2));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReadonlyRectangle) {
			ReadonlyRectangle r = (ReadonlyRectangle)obj;
			return (getX() == r.getX() &&
					getY() == r.getY() &&
					getWidth() == r.getWidth() &&
					getHeight() == r.getHeight());
		}
		return false;
	}
	
	public Point getLocation() {
		return new Point(getX(), getY());
	}
	
	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}
	
	public boolean contains(ReadonlyRectangle r) {
		return (getX() <= r.getX() && getY() <= r.getY() &&
				getRight() >= r.getRight() && getBottom() >= r.getBottom());
	}
	
	public boolean intersects(ReadonlyRectangle r) {
		int x1 = max(getLeft(), r.getLeft()),
			x2 = min(getRight(), r.getRight()),
			y1 = max(getTop(), r.getTop()),
			y2 = min(getBottom(), r.getBottom());
		return (x2 - x1 >= 0 && y2 - y1 >= 0);
	}
	
	public boolean isEmpty() {
		return (getWidth() <= 0 || getHeight() <= 0);
	}
	
	public Rectangle getUnion(ReadonlyRectangle r) {
		return new Rectangle(this).union(r);
	}
	
	@Override
	public Rectangle clone() {
		return getCopy();
	}
	
	public Rectangle getCopy() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}
	
	public org.eclipse.draw2d.geometry.Rectangle getDraw2DRectangle() {
		org.eclipse.draw2d.geometry.Rectangle r =
			new org.eclipse.draw2d.geometry.Rectangle(getX(), getY(), getWidth(), getHeight());
		return r;
	}
	
	protected static int min(int a, int b) {
		return Math.min(a, b);
	}	
	
	protected static int max(int a, int b) {
		return Math.max(a, b);
	}
	
	public static final Rectangle SINGLETON = new Rectangle();
	
	@Override
	public String toString() {
		return "Rectangle((" + getX() + ", " + getY() + "), " +
				getWidth() + " x " + getHeight() + ")";
	}
}
