package dk.itu.big_red.figure;

import java.util.ArrayList;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.model.Control;

public class NodeFigure extends AbstractFigure {
	public static final int NODE_FIGURE_DEFWIDTH = 100;
	public static final int NODE_FIGURE_DEFHEIGHT = 100;

	private ArrayList<Point> portAnchors = new ArrayList<Point>();
	private Control.Shape shape = Control.Shape.SHAPE_RECTANGLE;
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
	
	public void setShape(Control.Shape shape) {
		this.shape = shape;
	}
	
	public Control.Shape getShape() {
		return this.shape;
	}
	
	public void setToolTip(String control, String ports, String content) {
		String labelText = control;
		if (ports != null)
			labelText += "\nHas ports: " + ports;
		if (content != null)
			labelText += "\n\n" + content;
		super.setToolTip(labelText);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		try {
			graphics.setBackgroundColor(getBackgroundColor());
			Rectangle nc = getConstraintCopy(getTotalOffset());
			nc.x++; nc.y++; nc.width -= 2; nc.height -= 2;
			switch (shape) {
			case SHAPE_OVAL:
				graphics.fillOval(nc);
				break;
			case SHAPE_TRIANGLE:
				PointList triangle = new PointList();
				triangle.addPoint(nc.x + (nc.width / 2), nc.y);
				triangle.addPoint(nc.x, nc.y + nc.height);
				triangle.addPoint(nc.x + nc.width, nc.y + nc.height);
				graphics.fillPolygon(triangle);
				break;
			case SHAPE_RECTANGLE:
				graphics.fillRectangle(nc);
				break;
			}
			graphics.setBackgroundColor(ColorConstants.red);
			for (Point p : portAnchors) {
				Rectangle r = getConstraintCopy(getTotalOffset());
				r.x += p.x; r.y += p.y;
				graphics.fillOval(r.x - 4, r.y - 4, 8, 8);
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
			Rectangle nc = getConstraintCopy(getTotalOffset());
			/*
			 * This special adjustment stops the objects from being clipped
			 * (because they went slightly over their borders), because that
			 * looks terrible.
			 */
			nc.x++; nc.y++; nc.width -= 3; nc.height -= 3;
			switch (shape) {
			case SHAPE_OVAL:
				graphics.drawOval(nc);
				break;
			case SHAPE_TRIANGLE:
				PointList triangle = new PointList();
				triangle.addPoint(nc.x + (nc.width / 2), nc.y);
				triangle.addPoint(nc.x, nc.y + nc.height);
				triangle.addPoint(nc.x + nc.width, nc.y + nc.height);
				graphics.drawPolygon(triangle);
				break;
			case SHAPE_RECTANGLE:
				graphics.drawRectangle(nc);
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

	public void addPortAnchor(Point portAnchorPosition) {
		portAnchors.add(portAnchorPosition);
	}

	public void clearPortAnchors() {
		portAnchors.clear();
	}
}
