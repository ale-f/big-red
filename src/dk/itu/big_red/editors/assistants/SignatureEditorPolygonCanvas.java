package dk.itu.big_red.editors.assistants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
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
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.util.Ellipse;
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
	
	private Shape mode = Shape.SHAPE_POLYGON;
	private PointList points = new PointList();
	private Point tmp = new Point();
	private Dimension controlSize = new Dimension();
	
	private double ellipseWidth = 40, ellipseHeight = 40;
	
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
	 * Changes the mode of the editor, allowing either ovals or polygons to be
	 * edited. (Both modes allow for ports to be added, moved, and removed, but
	 * only the polygon editor allows for the shape to be changed.)
	 * 
	 * <p>Even if the mode wasn't actually changed, a call to this method
	 * resets the editor, removing all points and ports.
	 * @param mode a {@link Shape}
	 */
	public void setMode(Shape mode) {
		points.removeAllPoints();
		firePointChange(PointEvent.REMOVED, null);
		points.addPoint(0, 0);
		
		ports.clear();
		firePortChange(PortEvent.REMOVED, null);
				
		if (mode == Shape.SHAPE_POLYGON) {
			firePointChange(PointEvent.ADDED, new Point(0, 0));

			centrePolygon();
		} else if (mode == Shape.SHAPE_OVAL) {
			/* no special handling */
		}

		this.mode = mode;
		redraw();
	}
	
	public Shape getMode() {
		return mode;
	}
	
	/**
	 * Deletes the point under the crosshairs, if there is one (and if it isn't
	 * the last point remaining).
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (mode == Shape.SHAPE_OVAL)
			return;
		Point p = roundToGrid(e.x, e.y);
		int deleteIndex = findPointAt(p);
		if (deleteIndex != -1 && (deleteIndex != 0 || points.size() > 1)) {
			if (dragPointIndex == deleteIndex)
				dragPointIndex = -1;
			firePointChange(PointEvent.REMOVED, points.removePoint(deleteIndex));
			for (Port port : ports) {
				int segment = port.getSegment();
				if (segment >= deleteIndex)
					port.setSegment(segment - 1);
			}
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
		if (mode == Shape.SHAPE_POLYGON) {
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
		} else if (mode == Shape.SHAPE_OVAL) {
			Ellipse e = new Ellipse();
			e.setBounds(new Rectangle(30, 30, ((controlSize.width - 60) / 10) * 10, ((controlSize.height - 60) / 10) * 10));
			for (int i = 0; i < ports.size(); i++) {
				Port p = ports.get(i);
				tmp.setLocation(e.getPointFromOffset(p.getDistance()));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		}
		return -1;
	}
	
	protected int findPointAt(Point p) {
		return findPointAt(p.x, p.y);
	}
	
	protected int findPointAt(int x, int y) {
		if (mode != Shape.SHAPE_POLYGON)
			return -1;
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
		if (mode == Shape.SHAPE_OVAL)
			return 0;
		int index = -1;
		Line l = new Line();
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < points.size(); i++) {
			l.setFirstPoint(getPoint(tmp, i));
			l.setSecondPoint(getPoint(tmp, i + 1));
			double tDistance;
			if (l.getIntersection(tmp, up) != null) {
				tDistance = up.getDistance(tmp);
				if (tDistance < distance) {
					distance = tDistance;
					index = i;
				}
			} else {
				tDistance = l.getFirstPoint().getDistance(up);
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
			dragPointIndex = findPointAt(p);
			if (dragPointIndex == -1 && mode == Shape.SHAPE_POLYGON) {
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
			Port p = ports.get(dragPortIndex);
			Line l = new Line();
			int segment;
			double offset;
			
			if (mode == Shape.SHAPE_POLYGON) {
				segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection != null)
					offset = l.getOffsetFromPoint(intersection);
				else offset = 0;
			} else {
				segment = 0;
				offset = new Ellipse(new Rectangle(30, 30, ((controlSize.width - 60) / 10) * 10, ((controlSize.height - 60) / 10) * 10))
					.getClosestOffset(mousePosition);
			}
			
			p.setSegment(segment);
			p.setDistance(offset);
			firePortChange(PortEvent.MOVED, p);
			
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
					for (Port port : ports) {
						int segment = port.getSegment();
						if (segment >= dragPointIndex)
							port.setSegment(segment + 1);
					}
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
		
		Line l = new Line();
		
		if (mode == Shape.SHAPE_POLYGON) {
			e.gc.drawPolyline(points.toIntArray());
			Point first = getPoint(0), last = getPoint(points.size() - 1);
			e.gc.drawLine(first.x, first.y, last.x, last.y);
			
			e.gc.setBackground(ColorConstants.black);
			for (int i = 0; i < points.size(); i++) {
				points.getPoint(tmp, i);
				e.gc.fillOval(tmp.x - 3, tmp.y - 3, 6, 6);
			}
			
			e.gc.setBackground(ColorConstants.red);
			for (Port p : ports) {
				int segment = p.getSegment();
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				
				Point pt = l.getPointFromOffset(p.getDistance());
				e.gc.fillOval(pt.x - 4, pt.y - 4, 8, 8);
			}
		} else {
			int w = ((controlSize.width - 60) / 10) * 10,
			    h = ((controlSize.height - 60) / 10) * 10;
			e.gc.drawOval(30, 30, w, h);
			
			e.gc.setBackground(ColorConstants.red);
			for (Port p : ports) {
				Point pt = new Ellipse(new Rectangle(30, 30, w, h)).getPointFromOffset(p.getDistance());
				e.gc.fillOval(pt.x - 4, pt.y - 4, 8, 8);
			}
		}
		
		if (dragPortIndex != -1) {
			e.gc.setAlpha(127);
			
			if (mode == Shape.SHAPE_POLYGON) {
				int segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection == null)
					intersection = getPoint(segment);
				tmp.setLocation(intersection);
			} else if (mode == Shape.SHAPE_OVAL) {
				tmp.setLocation(new Ellipse(new Rectangle(30, 30, ((controlSize.width - 60) / 10) * 10, ((controlSize.height - 60) / 10) * 10))
					.getClosestPoint(mousePosition));
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

	private int tippedPoint, tippedPort;
	
	private void updateToolTip() {
		int point = findPointAt(mousePosition),
		    port = findPortAt(mousePosition);
		if (point == -1)
			point = findPointAt(roundedMousePosition);
		if (port == -1)
			port = findPortAt(roundedMousePosition);
		
		if (tippedPoint == point && tippedPort == port)
			return;
		
		tippedPoint = point;
		tippedPort = port;
		
		String toolTipText = null;
		if (port != -1 && point != -1) {
			toolTipText = "Port " + getPorts().get(port).getName() + " and point " + point;
		} else if (port != -1) {
			toolTipText = "Port " + getPorts().get(port).getName();
		} else if (point != -1) {
			toolTipText = "Point " + point;
		}
		setToolTipText(toolTipText);
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
		updateToolTip();
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
				if (mode == Shape.SHAPE_POLYGON)
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
				else UI.createMenuItem(m, 0, "Add &port", new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						Port p = new Port();
						p.setSegment(0);
						p.setDistance(new Ellipse(new Rectangle(30, 30, ((controlSize.width - 60) / 10) * 10, ((controlSize.height - 60) / 10) * 10)).getClosestOffset(mousePosition));
						
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
			UI.createMenuItem(m, 0, "Re&name port", new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					final Port p = ports.get(foundPort);
					InputDialog d = new InputDialog(getShell(),
							"Port name", "Choose a name for this port:",
							p.getName(), new IInputValidator() {
								
								@Override
								public String isValid(String newText) {
									for (Port i : ports)
										if (i.getName().equals(newText) && i != p)
											return "This port name is already in use.";
									return null;
								}
							});
					if (d.open() == InputDialog.OK) {
						p.setName(d.getValue());
						firePortChange(PortEvent.RENAMED, p);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			UI.createMenuItem(m, 0, "&Remove port", new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					firePortChange(PortEvent.REMOVED, ports.remove(foundPort));
					redraw();
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
					setMode(mode);
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
	
	/**
	 * Returns a list of {@link Port}s specifying the polygon drawn in this
	 * Canvas.
	 * @return a PointList specifying a polygon
	 */
	public List<Port> getPorts() {
		return ports;
	}

	/**
	 * Overwrites the current list of Ports with the contents of the given
	 * {@link List}.
	 * @param ports a list of Ports
	 */
	public void setPorts(List<Port> ports) {
		if (ports != null) {
			this.ports.clear();
			this.ports.addAll(ports);
		}
	}
}
