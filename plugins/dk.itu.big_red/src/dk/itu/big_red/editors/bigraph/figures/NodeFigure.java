package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import dk.itu.big_red.model.Ellipse;

public class NodeFigure extends AbstractFigure {
	private Object shape;
	private Label labelControl = new Label();    
    
	public NodeFigure() {
		super();
		
		labelControl.setForegroundColor(ColorConstants.black);
		add(labelControl, 0);
		setConstraint(labelControl, new Rectangle(1, 1, -1, -1));
	}
	
	public void setLabel(String text) {
		labelControl.setText(text);
	}
	
	public void setShape(Object shape) {
		this.shape = shape;
	}
	
	public Object getShape() {
		return shape;
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			if (shape instanceof Ellipse) {
				graphics.fillOval(1, 1, a.width - 1, a.height - 1);
			} else if (shape instanceof PointList) {
				graphics.fillPolygon((PointList)shape);
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
			
			if (shape instanceof Ellipse) {
				graphics.drawOval(1, 1, a.width - 2, a.height - 2);
			} else if (shape instanceof PointList) {
				graphics.drawPolygon((PointList)shape);
			}
		} finally {
			stop(graphics);
		}
	}
}
