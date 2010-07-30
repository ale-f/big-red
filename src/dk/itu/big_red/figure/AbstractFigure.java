package dk.itu.big_red.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import dk.itu.big_red.figure.adornments.SlightlyOvergenerousClippingStrategy;

public abstract class AbstractFigure extends Shape {
	public AbstractFigure() {
		/*
		 * All AbstractFigures should look good!
		 */
		setAntialias(SWT.ON);
		setOpaque(false);
		setClippingStrategy(new SlightlyOvergenerousClippingStrategy());
		setLayoutManager(new XYLayout());
	}
	
	public void setConstraint(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}
	
	public void setToolTip(String content) {
		Label label = new Label(content);
		label.setBorder(new MarginBorder(4));
		setToolTip(label);
	}
	
	/**
	 * Pushes the state of the specified {@link Graphics}, translates its
	 * co-ordinates by the {@link #getLocation location} of this object, and
	 * returns the {@link Rectangle} in which this figure should be drawn.
	 * <p>Remember to call {@link #stop} to restore the state of the Graphics.
	 * @param g a Graphics
	 * @return the region in which this figure should be drawn
	 */
	public Rectangle start(Graphics g) {
		g.pushState();
		g.translate(getLocation());
		return Rectangle.SINGLETON.setLocation(0, 0).setSize(getSize());
	}
	
	/**
	 * Cleans up after an invocation of {@link #start}, popping the state of
	 * the specified {@link Graphics}.
	 *
	 * <p>Although this is currently equivalent to <code>g.popState()</code>,
	 * it's not guaranteed to remain that way - if <code>start</code> acquires
	 * more functionality, then this method will clean up after that, too.
	 * @param g a Graphics
	 */
	public void stop(Graphics g) {
		g.popState();
	}
}
