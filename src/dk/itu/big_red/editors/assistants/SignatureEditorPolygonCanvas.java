package dk.itu.big_red.editors.assistants;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import dk.itu.big_red.editors.assistants.PointListener.PointEvent;
import dk.itu.big_red.editors.assistants.PortListener.PortEvent;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.util.Line;
import dk.itu.big_red.util.UI;

/**
 * SignatureEditorPolygonCanvases are widgets based on {@link Canvas} that let
 * the user design a polygon. They keep track of a {@link PointList}, and the
 * user can modify that PointList by clicking on the widget.
 * @author alec
 *
 */
public class SignatureEditorPolygonCanvas extends Canvas
implements ControlListener, MouseListener, MouseMoveListener, PaintListener,
MenuListener {
	/**
	 * The property name fired when the set of points changes.
	 */
	public static final String PROPERTY_POINT = "SignatureEditorPolygonCanvasPoint";
	
	/**
	 * The property name fired when the set of ports changes.
	 */
	public static final String PROPERTY_PORT = "SignatureEditorPolygonCanvasPort";
	
	private PointList points = new PointList();
	private Point tmp = new Point();
	private Dimension controlSize = new Dimension();
	
	private Point roundedMousePosition = new Point(-10, -10),
	              mousePosition = new Point(-10, -10);
	
	private int dragPointIndex = -1;
	private Point dragPoint = null;
	
	private int dragPortIndex = -1;
	
	private ArrayList<Port> ports = new ArrayList<Port>();
	
	protected ArrayList<PointListener> pointListeners =
		new ArrayList<PointListener>();
	protected ArrayList<PortListener> portListeners =
		new ArrayList<PortListener>();
	
	public SignatureEditorPolygonCanvas(Composite parent, int style) {
		super(parent, style);
		addMouseListener(this);
		addPaintListener(this);
		addControlListener(this);
		addMouseMoveListener(this);
		
		Menu menu = new Menu(this);
		menu.addMenuListener(this);
		setMenu(menu);
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
			if (dragPointIndex == deleteIndex)
				dragPointIndex = -1;
			firePointChange(PointEvent.REMOVED, points.removePoint(deleteIndex));
		}
	}

	protected Point roundToGrid(Point p) {
		p.x = (int)(Math.round(p.x / 10.0) * 10);
		p.y = (int)(Math.round(p.y / 10.0) * 10);
		return p;
	}
	
	protected Point roundToGrid(int x, int y) {
		return roundToGrid(new Point(x, y));
	}
	
	protected Point getPoint(Point t, int i) {
		return points.getPoint(t, (i + points.size()) % points.size());
	}
	
	protected Point getPoint(int i) {
		return points.getPoint((i + points.size()) % points.size());
	}
	
	protected int findPortAt(Point p) {
		return findPortAt(p.x, p.y);
	}
	
	protected int findPortAt(int x, int y) {
		Line l = new Line();
		for (int i = 0; i < ports.size(); i++) {
			Port p = ports.get(i);
			int segment = p.getSegment();
			l.setFirstPoint(getPoint(segment));
			l.setSecondPoint(getPoint(segment + 1));
			tmp.setLocation(l.getPointFromOffset(p.getDistance()));
			if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
				y >= tmp.y - 4 && y <= tmp.y + 4)
				return i;
		}
		return -1;
	}
	
	protected int findPointAt(Point p) {
		return findPointAt(p.x, p.y);
	}
	
	protected int findPointAt(int x, int y) {
		for (int i = 0; i < points.size(); i++) {
			points.getPoint(tmp, i);
			if (x >= tmp.x - 3 && x <= tmp.x + 3 &&
				y >= tmp.y - 3 && y <= tmp.y + 3)
				return i;
		}
		return -1;
	}
	
	private void centrePolygon() {
		org.eclipse.swt.graphics.Point s = getSize();
		
		Rectangle polyBounds = points.getBounds();
		points.translate(
			roundToGrid(polyBounds.getTopLeft().getNegated().translate(s.x / 2, s.y / 2).translate(-polyBounds.width / 2, -polyBounds.height / 2)));
		firePointChange(PointEvent.MOVED, null);
		redraw();
	}
	
	private int getNearestSegment(Point up, double threshold) {
		int index = -1;
		Line l = new Line();
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < points.size(); i++) {
			l.setFirstPoint(getPoint(tmp, i));
			l.setSecondPoint(getPoint(tmp, i + 1));
			if (l.getIntersection(tmp, up) != null) {
				double tDistance = up.getDistance(tmp);
				if (tDistance < distance) {
					distance = tDistance;
					index = i;
				}
			}
		}
		
		if (distance < threshold)
			return index;
		else return -1;
	}
	
	/**
	 * Creates a new point if the crosshairs aren't currently over one;
	 * otherwise, starts a drag operation.
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button != 1)
			return;
		Point p = roundToGrid(e.x, e.y),
		      up = new Point(e.x, e.y);
		dragPortIndex = findPortAt(up);
		if (dragPortIndex == -1) {
			dragPointIndex = findPointAt(up);
			if (dragPointIndex == -1) {
				if (points.size() == 1) {
					dragPointIndex = 0;
					dragPoint = p;
				} else {
					int index = getNearestSegment(up, 15);
					
					if (index != -1) {
						dragPointIndex = index + 1;
						dragPoint = p;
					}
				}
			}
		}
		redraw();
	}

	/**
	 * Moves the point the user's dragging and schedules a redraw, if dragging
	 * is in progress.
	 */
	@Override
	public void mouseUp(MouseEvent e) {
		if (dragPortIndex != -1) {
			dragPortIndex = -1;
			redraw();
		} else if (dragPointIndex != -1) {
			Point p = roundToGrid(e.x, e.y);
			int pointAtCursor = findPointAt(roundedMousePosition);
			if (pointAtCursor == -1) {
				if (dragPoint == null) {
					tmp = points.getPoint(dragPointIndex);
					tmp.x = p.x; tmp.y = p.y;
					points.setPoint(tmp, dragPointIndex);
					firePointChange(PointEvent.MOVED, tmp);
				} else {
					dragPoint.x = p.x;
					dragPoint.y = p.y;
					points.insertPoint(dragPoint, dragPointIndex);
					firePointChange(PointEvent.ADDED, dragPoint);
				}
			}
			dragPointIndex = -1;
			dragPoint = null;
			redraw();
		}
	}

	@Override
	public void paintControl(PaintEvent e) {
		e.gc.setAntialias(SWT.ON);
		setCursor(Cursors.ARROW);
		
		e.gc.setForeground(ColorConstants.lightGray);
		for (int x = 0; x <= controlSize.width; x += 10) {
			if (x != roundedMousePosition.x)
				e.gc.setAlpha(63);
			else e.gc.setAlpha(255);
			e.gc.drawLine(x, 0, x, controlSize.height);
		}
		for (int y = 0; y <= controlSize.height; y += 10) {
			if (y != roundedMousePosition.y)
				e.gc.setAlpha(63);
			else e.gc.setAlpha(255);
			e.gc.drawLine(0, y, controlSize.width, y);
		}
		
		if (getEnabled() == false) {
			e.gc.setBackground(ColorConstants.lightGray);
			e.gc.setAlpha(128);
			e.gc.fillRectangle(0, 0, controlSize.width, controlSize.height);
			return;
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
		
		e.gc.setBackground(ColorConstants.red);
		Line l = new Line();
		for (Port p : ports) {
			int segment = p.getSegment();
			l.setFirstPoint(getPoint(segment));
			l.setSecondPoint(getPoint(segment + 1));
			
			Point pt = l.getPointFromOffset(p.getDistance());
			e.gc.fillOval(pt.x - 4, pt.y - 4, 8, 8);
		}
		
		if (dragPortIndex != -1) {
			e.gc.setAlpha(127);
			
			int segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
			if (segment != -1) {
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				tmp.setLocation(l.getIntersection(mousePosition));
			} else {
				int index = -1;
				double distance = Double.MAX_VALUE;
				for (int i = 0; i < points.size(); i++) {
					points.getPoint(tmp, i);
					double td = tmp.getDistance(mousePosition);
					if (td < distance) {
						index = i;
						distance = td;
					}
				}
				if (index != -1)
					points.getPoint(tmp, index);
			}
			
			e.gc.fillOval(tmp.x - 4, tmp.y - 4, 8, 8);
		} else if (dragPointIndex != -1) {
			e.gc.setAlpha(127);
			Point previous, next;
			if (dragPoint == null) {
				previous = getPoint(dragPointIndex - 1);
				next = getPoint(dragPointIndex + 1);
			} else {
				previous = getPoint(dragPointIndex - 1);
				next = getPoint(dragPointIndex);
			}
			
			e.gc.setForeground(ColorConstants.red);
			e.gc.setBackground(ColorConstants.red);
			
			e.gc.drawLine(previous.x, previous.y, roundedMousePosition.x, roundedMousePosition.y);
			e.gc.drawLine(next.x, next.y, roundedMousePosition.x, roundedMousePosition.y);
			e.gc.fillOval(roundedMousePosition.x - 3, roundedMousePosition.y - 3, 6, 6);
			
			int pointAtCursor = findPointAt(roundedMousePosition);
			if (pointAtCursor != -1 && pointAtCursor != dragPointIndex)
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
		mousePosition.setLocation(e.x, e.y);
		Point currentMousePosition = roundToGrid(e.x, e.y);
		if (!roundedMousePosition.equals(currentMousePosition)) {
			roundedMousePosition.x = currentMousePosition.x;
			roundedMousePosition.y = currentMousePosition.y;
			redraw();
		} else if (dragPointIndex != -1 || dragPortIndex != -1)
			redraw();
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
		
		/*
		 * The first point can only safely be added at this point (the control
		 * doesn't have a size when the constructor is running).
		 */
		if (points.size() == 0) {
			points.addPoint(0, 0);
			centrePolygon();
		}
		
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

	/**
	 * Overwrites the current polygon with the contents of the given {@link
	 * PointList}.
	 * <p>The points on the given PointList will not be snapped to the grid,
	 * nor will they be checked for scale.
	 * @param points a PointList
	 */
	public void setPoints(PointList points) {
		if (points != null && points.size() >= 1) {
			this.points.removeAllPoints();
			this.points.addAll(points);
			centrePolygon();
		}
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void menuHidden(MenuEvent e) {
	}

	/**
	 * Populates and displays the pop-up menu.
	 */
	@Override
	public void menuShown(MenuEvent e) {
		Menu m = getMenu();
		for (MenuItem i : m.getItems())
			i.dispose();
		
		final int foundPoint = findPointAt(roundedMousePosition),
		          foundPort = findPortAt(mousePosition),
		          segment = getNearestSegment(roundedMousePosition, 15);
		
		if (foundPort == -1) {
			if (segment != -1) {
				UI.createMenuItem(m, 0, "Add &port", new SelectionListener() {
		
					@Override
					public void widgetSelected(SelectionEvent e) {
						Line l = new Line();
						l.setFirstPoint(getPoint(segment));
						l.setSecondPoint(getPoint(segment + 1));
						
						Point portPoint = l.getIntersection(roundedMousePosition);
						double distance = l.getOffsetFromPoint(portPoint);
						
						int lsegment = segment;
						if (distance >= 1) {
							lsegment++;
							distance = 0;
						}
						
						Port p = new Port();
						p.setSegment(lsegment);
						p.setDistance(distance);
						
						ports.add(p);
						firePortChange(PortEvent.ADDED, p);
						redraw();
					}
		
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		} else {
			UI.createMenuItem(m, 0, "&Remove port", new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		if (foundPoint != -1) {
			if (foundPoint != 0 || points.size() > 1) {
				UI.createMenuItem(m, 0, "&Remove point", new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						for (Iterator<Port> it = ports.iterator(); it.hasNext(); ) {
							Port p = it.next();
							if (p.getSegment() == foundPoint) {
								it.remove();
								firePortChange(PortEvent.REMOVED, p);
							}
						}
						firePointChange(PointEvent.REMOVED, points.removePoint(foundPoint));
						redraw();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			} else {
				UI.createMenuItem(m, 0, "Cannot remove last point", null).setEnabled(false);
			}
		}
		if (points.size() > 1) {
			UI.createMenuItem(m, 0, "Remove &all points and ports", new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					points.removeAllPoints();
					firePointChange(PointEvent.REMOVED, null);
					
					ports.clear();
					firePortChange(PortEvent.REMOVED, null);
					
					points.addPoint(0, 0);
					firePointChange(PointEvent.ADDED, new Point(0, 0));

					centrePolygon();
					redraw();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		if (m.getItemCount() > 0)
			new MenuItem(m, SWT.SEPARATOR);
		UI.createMenuItem(m, 0, "Centre &polygon on canvas", new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				centrePolygon();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	/**
	 * Registers the given listener to be notified when the user adds or
	 * removes a point from the canvas. 
	 * @param listener a {@link PointListener}
	 */
	public void addPointListener(PointListener listener) {
		pointListeners.add(listener);
	}

	/**
	 * Unregisters the given listener from being notified when the user adds or
	 * removes a point from the canvas. 
	 * @param listener a {@link PointListener}
	 */
	public void removePointListener(PointListener listener) {
		pointListeners.remove(listener);
	}
	
	private void firePointChange(int type, Point object) {
		PointEvent e = new PointEvent();
		e.source = this;
		e.type = type;
		e.object = object;
		for (PointListener i : pointListeners)
			i.pointChange(e);
	}
	
	/**
	 * Registers the given listener to be notified when the user adds or
	 * removes a port from the canvas. 
	 * @param listener a {@link PointListener}
	 */
	public void addPortListener(PortListener listener) {
		portListeners.add(listener);
	}

	/**
	 * Unregisters the given listener from being notified when the user adds or
	 * removes a port from the canvas. 
	 * @param listener a {@link PointListener}
	 */
	public void removePortListener(PortListener listener) {
		portListeners.remove(listener);
	}
	
	private void firePortChange(int type, Port object) {
		PortEvent e = new PortEvent();
		e.source = this;
		e.type = type;
		e.object = object;
		for (PortListener i : portListeners)
			i.portChange(e);
	}
}
