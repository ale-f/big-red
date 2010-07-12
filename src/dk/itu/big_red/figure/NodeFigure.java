package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Geometry;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.model.Control;

public class NodeFigure extends AbstractFigure {
	public static final int NODE_FIGURE_DEFWIDTH = 100;
	public static final int NODE_FIGURE_DEFHEIGHT = 100;

	private Control.Shape shape = Control.Shape.SHAPE_POLYGON;
	private PointList points = Control.POINTS_QUAD;
	private Label labelControl = new Label();    
    
	public NodeFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
	    
		labelControl.setForegroundColor(ColorConstants.black);
		add(labelControl, 0);
		setConstraint(labelControl, new Rectangle(1, 1, -1, -1));
		
		setFillColour(new RGB(255, 255, 255));
		setOutlineColour(new RGB(0, 0, 0));
	}
	
	public void setLabel(String text) {
		labelControl.setText(text);
	}
	
	public void setPoints(PointList points) {
		this.points = points;
	}
	
	public PointList getPoints() {
		return this.points;
	}
	
	public void setShape(Control.Shape shape) {
		this.shape = shape;
	}
	
	public Control.Shape getShape() {
		return this.shape;
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		try {
			graphics.setBackgroundColor(getBackgroundColor());
			
			graphics.translate(getLocation());
			Rectangle c = getConstraint();
			switch (shape) {
			case SHAPE_OVAL:
				graphics.fillOval(1, 1, c.width - 1, c.height - 1);
				break;
			case SHAPE_POLYGON:
				graphics.fillPolygon(points);
				break;
			}
		} finally {
			graphics.popState();
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.pushState();
		try {
			graphics.setLineWidth(2);
			graphics.setLineStyle(SWT.LINE_SOLID);
			graphics.setForegroundColor(getForegroundColor());
			
			graphics.translate(getLocation());
			Rectangle c = getConstraint();
			switch (shape) {
			case SHAPE_OVAL:
				graphics.drawOval(1, 1, c.width - 2, c.height - 2);
				break;
			case SHAPE_POLYGON:
				graphics.drawPolygon(points);
				break;
			}
		} finally {
			graphics.popState();
		}
	}

	public void setFillColour(RGB fillColour) {
		if (fillColour != null) {
			if (getLocalBackgroundColor() != null)
				getLocalBackgroundColor().dispose();
			setBackgroundColor(new Color(null, fillColour));
		}
	}
	
	public void setOutlineColour(RGB outlineColour) {
		if (outlineColour != null) {
			if (getLocalForegroundColor() != null)
				getLocalForegroundColor().dispose();
			setForegroundColor(new Color(null, outlineColour));
		}
	}
}
