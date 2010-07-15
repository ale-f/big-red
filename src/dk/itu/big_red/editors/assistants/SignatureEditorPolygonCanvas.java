package dk.itu.big_red.editors.assistants;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import dk.itu.big_red.util.Line;

/**
 * SignatureEditorPolygonCanvases are widgets based on {@link Canvas} that let
 * the user design a polygon. They keep track of a {@link PointList}, and the
 * user can modify that PointList by clicking on the widget.
 * @author alec
 *
 */
public class SignatureEditorPolygonCanvas extends Canvas
implements ControlListener, MouseListener, MouseMoveListener, PaintListener {
	private PointList points = new PointList();
	private Point tmp = Point.SINGLETON;
	private Point mousePosition = new Point(-10, -10);
	private Dimension controlSize = new Dimension();
	private int dragIndex = -1;
	
	public SignatureEditorPolygonCanvas(Composite parent, int style) {
		super(parent, style);
		addMouseListener(this);
		addPaintListener(this);
		addControlListener(this);
		addMouseMoveListener(this);
		
		points.addPoint(50, 50);
	}

	/**
	 * Deletes the point under the crosshairs, if there is one (and if it isn't
	 * the last point remaining).
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		Point p = roundToGrid(e.x, e.y);
		int deleteIndex = findPointAt(p);
		if (deleteIndex != -1 && (deleteIndex != 0 || points.size() > 1)) {
			if (dragIndex == deleteIndex)
				dragIndex = -1;
			points.removePoint(deleteIndex);
		}
	}

	protected Point roundToGrid(int x, int y) {
		return new Point(
				(int)(Math.round(x / 10.0) * 10),
				(int)(Math.round(y / 10.0) * 10));
	}
	
	protected Point getPoint(Point t, int i) {
		return points.getPoint(t, (i + points.size()) % points.size());
	}
	
	protected Point getPoint(int i) {
		return points.getPoint((i + points.size()) % points.size());
	}
	
	protected int findPointAt(Point p) {
		return findPointAt(p.x, p.y);
	}
	
	protected int findPointAt(int x, int y) {
		for (int i = 0; i < points.size(); i++) {
			points.getPoint(tmp, i);
			if (tmp.x == x && tmp.y == y)
				return i;
		}
		return -1;
	}
	
	/**
	 * Creates a new point if the crosshairs aren't currently over one;
	 * otherwise, starts a drag operation.
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button != 1)
			return;
		Point p = roundToGrid(e.x, e.y);
		dragIndex = findPointAt(p);
		if (dragIndex == -1) {
			double distance = Double.MAX_VALUE;
			int index = -1;
			for (int i = 0; i < points.size(); i++) {
				double tDistance = p.getDistance(getPoint(tmp, i));
				if (tDistance < distance) {
					distance = tDistance;
					index = i;
				}
			}
			
			Line l = new Line();
			l.setFirstPoint(getPoint(tmp, index));
			l.setSecondPoint(getPoint(tmp, index - 1));
			
			double id1 = Double.MAX_VALUE, id2 = Double.MAX_VALUE;
			if (l.getIntersection(tmp, mousePosition) != null || points.size() == 1)
				id1 = mousePosition.getDistance(tmp);
			
			l.setSecondPoint(getPoint(tmp, index + 1));
			if (l.getIntersection(tmp, mousePosition) != null)
				id2 = mousePosition.getDistance(tmp);
			
			if (id1 < id2)
				points.insertPoint(p, index);
			else
				points.insertPoint(p, index + 1);
		}
		redraw();
	}

	/**
	 * Moves the point the user's dragging and schedules a redraw, if dragging
	 * is in progress.
	 */
	@Override
	public void mouseUp(MouseEvent e) {
		if (dragIndex != -1) {
			Point p = roundToGrid(e.x, e.y);
			int pointAtCursor = findPointAt(mousePosition);
			if (pointAtCursor == -1) {
				tmp = points.getPoint(dragIndex);
				tmp.x = p.x; tmp.y = p.y;
				points.setPoint(tmp, dragIndex);
			}
			dragIndex = -1;
			redraw();
		}
	}

	@Override
	public void paintControl(PaintEvent e) {
		e.gc.setAntialias(SWT.ON);
		setCursor(Cursors.ARROW);
		
		e.gc.setForeground(ColorConstants.lightGray);
		for (int x = 0; x <= controlSize.width; x += 10) {
			if (x != mousePosition.x)
				e.gc.setAlpha(63);
			else e.gc.setAlpha(255);
			e.gc.drawLine(x, 0, x, controlSize.height);
		}
		for (int y = 0; y <= controlSize.height; y += 10) {
			if (y != mousePosition.y)
				e.gc.setAlpha(63);
			else e.gc.setAlpha(255);
			e.gc.drawLine(0, y, controlSize.width, y);
		}
		
		e.gc.setAlpha(255);
		
		e.gc.setForeground(ColorConstants.black);
		e.gc.drawPolyline(points.toIntArray());
		Point first = getPoint(0), last = getPoint(points.size() - 1);
		e.gc.drawLine(first.x, first.y, last.x, last.y);
		
		e.gc.setBackground(ColorConstants.black);
		for (int i = 0; i < points.size(); i++) {
			points.getPoint(tmp, i);
			e.gc.fillOval(tmp.x - 3, tmp.y - 3, 6, 6);
		}
		
		if (dragIndex != -1) {
			e.gc.setAlpha(127);
			Point previous = getPoint(dragIndex - 1),
				  next = getPoint(dragIndex + 1);
			
			e.gc.setForeground(ColorConstants.red);
			e.gc.setBackground(ColorConstants.red);
			
			e.gc.drawLine(previous.x, previous.y, mousePosition.x, mousePosition.y);
			e.gc.drawLine(next.x, next.y, mousePosition.x, mousePosition.y);
			e.gc.fillOval(mousePosition.x - 3, mousePosition.y - 3, 6, 6);
			
			int pointAtCursor = findPointAt(mousePosition);
			if (pointAtCursor != -1 && pointAtCursor != dragIndex)
				setCursor(Cursors.NO);
			
			e.gc.setAlpha(255);
		}
	}

	/**
	 * Updates the centre point for the crosshairs and schedules a redraw, if
	 * the mouse has moved to a new grid position since the last call to this
	 * method.
	 */
	@Override
	public void mouseMove(MouseEvent e) {
		Point currentMousePosition = roundToGrid(e.x, e.y);
		if (!mousePosition.equals(currentMousePosition)) {
			mousePosition.x = currentMousePosition.x;
			mousePosition.y = currentMousePosition.y;
			redraw();
		}
	}

	/**
	 * See {@link #controlResized}.
	 * @see #controlResized
	 */
	@Override
	public void controlMoved(ControlEvent e) {
		controlResized(e);
	}

	/**
	 * Updates the size information for this Canvas and schedules a redraw.
	 */
	@Override
	public void controlResized(ControlEvent e) {
		org.eclipse.swt.graphics.Point p = getSize();
		controlSize.width = p.x; controlSize.height = p.y;
		redraw();
	}

	/**
	 * Returns the {@link PointList} specifying the polygon drawn in this
	 * Canvas.
	 * @return a PointList specifying a polygon
	 */
	public PointList getPoints() {
		return points;
	}
}
