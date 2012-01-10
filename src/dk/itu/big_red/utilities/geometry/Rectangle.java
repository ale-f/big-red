package dk.itu.big_red.utilities.geometry;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

public class Rectangle extends ReadonlyRectangle {
	protected int x, y, width, height;
	
	public Rectangle() {
		setX(0).setY(0).setWidth(0).setHeight(0);
	}
	
	public Rectangle(int x, int y, int width, int height) {
		setX(x).setY(y).setWidth(width).setHeight(height);
	}
	
	public Rectangle(Point position, Dimension size) {
		setLocation(position).setSize(size);
	}
	
	public Rectangle(ReadonlyRectangle r) {
		setBounds(r);
	}
	
	public Rectangle(org.eclipse.draw2d.geometry.Rectangle r) {
		setX(r.x).setY(r.y).setWidth(r.width).setHeight(r.height);
	}
	
	public Rectangle setBounds(ReadonlyRectangle r) {
		return setX(r.getX()).setY(r.getY()).
				setWidth(r.getWidth()).setHeight(r.getHeight());
	}
	
	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public Rectangle setX(int x) {
		this.x = x;
		return this;
	}
	
	public Rectangle setY(int y) {
		this.y = y;
		return this;
	}
	
	public Rectangle setLocation(int x, int y) {
		return setX(x).setY(y);
	}
	
	public Rectangle setLocation(Point p) {
		return setLocation(p.x, p.y);
	}
	
	public Rectangle setTop(int top) {
		return setY(top);
	}
	
	public Rectangle setLeft(int left) {
		return setX(left);
	}
	
	public Rectangle setBottom(int bottom) {
		return setY(bottom - getHeight());
	}
	
	public Rectangle setRight(int right) {
		return setX(right - getWidth());
	}
	
	public Rectangle setWidth(int width) {
		this.width = width;
		return this;
	}
	
	public Rectangle setHeight(int height) {
		this.height = height;
		return this;
	}
	
	public Rectangle setSize(int width, int height) {
		return setWidth(width).setHeight(height);
	}
	
	public Rectangle setSize(Dimension d) {
		return setSize(d.width, d.height);
	}
	
	public Rectangle translate(int x, int y) {
		return setX(getX() + x).setY(getY() + y);
	}
	
	public Rectangle translate(Point p) {
		return translate(p.x, p.y);
	}
	
	public Rectangle union(ReadonlyRectangle r) {
		return setX(min(getLeft(), r.getLeft())).
				setY(min(getTop(), r.getTop())).
				setRight(max(getRight(), r.getRight())).
				setBottom(max(getBottom(), r.getBottom()));
	}
}
