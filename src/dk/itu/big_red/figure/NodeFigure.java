package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.model.Control;

public class NodeFigure extends AbstractFigure {
	private Control.Shape shape = Control.Shape.SHAPE_POLYGON;
	private PointList points = Control.POINTS_QUAD;
	private Label labelControl = new Label();    
    
	public NodeFigure() {
		super();
		
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
		Rectangle a = start(graphics);
		try {
			graphics.setBackgroundColor(getBackgroundColor());
			
			switch (shape) {
			case SHAPE_OVAL:
				graphics.fillOval(1, 1, a.width - 1, a.height - 1);
				break;
			case SHAPE_POLYGON:
				graphics.fillPolygon(points);
				break;
			}
		} finally {
			stop(graphics);
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setLineWidth(2);
			graphics.setLineStyle(SWT.LINE_SOLID);
			graphics.setForegroundColor(getForegroundColor());
			
			switch (shape) {
			case SHAPE_OVAL:
				graphics.drawOval(1, 1, a.width - 2, a.height - 2);
				break;
			case SHAPE_POLYGON:
				graphics.drawPolygon(points);
				break;
			}
		} finally {
			stop(graphics);
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
