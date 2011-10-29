package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;

import dk.itu.big_red.editors.bigraph.figures.assistants.CurvyConnectionRouter;

public class LinkConnectionFigure extends PolylineConnection {
	public LinkConnectionFigure() {
		setAntialias(SWT.ON);
        setLineStyle(org.eclipse.swt.SWT.LINE_SOLID);
        setConnectionRouter(new CurvyConnectionRouter());
	}
	
	@Override
	protected void outlineShape(Graphics g) {
		g.pushState();
		try {
			Point start = getStart(),
					end = getEnd();
			
			Path p = new Path(null);
			p.moveTo(start.x(), start.y());
			p.quadTo(end.x, start.y, end.x(), end.y());
			g.drawPath(p);
		} finally {
			g.popState();
		}
	}
	
	public void setToolTip(String content) {
		Label label = new Label(content);
		label.setBorder(new MarginBorder(4));
		super.setToolTip(label);
	}
}
