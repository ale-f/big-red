package dk.itu.big_red.figure.adornments;

import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;

public class AntialiasedPolygonDecoration extends PolygonDecoration {
	
	public static final PointList TERMINAL_HAT = new PointList();
	public static final PointList PORT = new PointList();
	
	static {
		/* setup TERMINAL_HAT */
		PORT.addPoint(1, 2);
		PORT.addPoint(2, 1);
		PORT.addPoint(2, -1);
		PORT.addPoint(1, -2);
		PORT.addPoint(-1, -2);
		PORT.addPoint(-2, -1);
		PORT.addPoint(-2, 1);
		PORT.addPoint(-1, 2);
		PORT.addPoint(1, 2);
	}
	
	public AntialiasedPolygonDecoration() {
		super();
		setAntialias(SWT.ON);
	}
}
