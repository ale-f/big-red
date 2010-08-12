package dk.itu.big_red.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class EdgeConnectionFigure extends PolylineConnection {
	public EdgeConnectionFigure() {
		setAntialias(SWT.ON);
        setLineStyle(org.eclipse.swt.SWT.LINE_SOLID);
        setOutlineColour(new RGB(0, 127, 0));
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
			
			g.setLineWidth(1);
			sr.width -= 1; sr.height -= 1;
			g.drawOval(sr);
		} finally {
			g.popState();
		}
	}
	
	public void setToolTip(String content) {
		String labelText = "Edge";
		if (content != null)
			labelText += "\n\n" + content;
		
		Label label = new Label(labelText);
		label.setBorder(new MarginBorder(4));
		super.setToolTip(label);
	}
	
	public void setOutlineColour(RGB outlineColour) {
		if (outlineColour != null) {
			if (getLocalForegroundColor() != null)
				getLocalForegroundColor().dispose();
			setForegroundColor(new Color(null, outlineColour));
		}
	}
}
