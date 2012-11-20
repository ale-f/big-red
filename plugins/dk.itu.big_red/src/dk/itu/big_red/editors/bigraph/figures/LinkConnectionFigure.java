package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;

import dk.itu.big_red.editors.bigraph.figures.assistants.CurvyConnectionRouter;
import dk.itu.big_red.model.LinkStyleUtilities.Style;
import dk.itu.big_red.utilities.ui.UI;

public class LinkConnectionFigure extends PolylineConnection {
	private static final ConnectionRouter
			ROUTER_CURVY = new CurvyConnectionRouter(),
			ROUTER_MANHATTAN = new ManhattanConnectionRouter();
	
	private Style style = Style.CURVY;
	
	public LinkConnectionFigure() {
		setAntialias(SWT.ON);
        setLineStyle(org.eclipse.swt.SWT.LINE_SOLID);
        setStyle(Style.CURVY);
	}
	
	public void setStyle(Style style) {
		this.style = style;
		setConnectionRouter(
				style == Style.CURVY ? ROUTER_CURVY :
				style == Style.MANHATTAN ? ROUTER_MANHATTAN :
				null);
	}
	
	@Override
	protected void outlineShape(Graphics g) {
		g.pushState();
		try {
			Point
				start = getStart(),
				end = getEnd();
			
			switch (style) {
			case CURVY:
				Path p = new Path(UI.getDisplay());
				p.moveTo(start.x(), start.y());
				p.quadTo(end.x, start.y, end.x(), end.y());
				g.drawPath(p);
				break;
			case MANHATTAN:
			default:
				g.drawPolyline(getPoints());
				break;
			}
		} finally {
			g.popState();
		}
	}
	
	private Label toolTip = null;
	
	public void setToolTip(String content) {
		if (toolTip == null)
			toolTip = AbstractFigure.createToolTipFor(this);
		toolTip.setText(content);
	}
}
