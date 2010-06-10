package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

public class EdgeConnectionFigure extends PolylineConnection {
	public EdgeConnectionFigure() {
		setAntialias(SWT.ON);
        setLineStyle(org.eclipse.swt.SWT.LINE_SOLID);
        setForegroundColor(ColorConstants.darkGreen);
        setConnectionRouter(new ManhattanConnectionRouter());
	}
	
	public void outlineShape(Graphics g) {
		g.pushState();
		try {
			super.outlineShape(g);
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
