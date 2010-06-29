package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

public class EdgeConnectionFigure extends PolylineConnection {
	public EdgeConnectionFigure() {
		setAntialias(SWT.ON);
        setLineStyle(org.eclipse.swt.SWT.LINE_SOLID);
        setForegroundColor(ColorConstants.darkGreen);
	}
	
	public void outlineShape(Graphics g) {
		g.pushState();
		try {
			Rectangle sr = getClientArea();
			sr.height *= 2; sr.width *= 2;
			Dimension d = getStart().getDifference(getEnd());
			
			if (d.height > 0)
				sr.y -= getClientArea().height;
			
			if (d.width < 0)
				sr.x -= getClientArea().width;
			
			g.setForegroundColor(ColorConstants.darkGreen);
			g.setLineWidth(1);
			sr.width -= 1; sr.height -= 1;
			g.drawOval(sr);
		} finally {
			g.popState();
		}
	}
	
	public void setToolTip(String content) {
		Label label = new Label(content);
		label.setBorder(new MarginBorder(4));
		setToolTip(label);
	}
}
