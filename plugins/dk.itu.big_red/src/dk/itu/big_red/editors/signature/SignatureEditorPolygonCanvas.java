package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IInputValidator;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.assistants.Line;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.utilities.ui.UI;

/**
 * SignatureEditorPolygonCanvases are widgets based on {@link Canvas} that let
 * the user design a polygon. They keep track of a {@link PointList}, and the
 * user can modify that PointList by clicking on the widget.
 * @author alec
 *
 */
public class SignatureEditorPolygonCanvas extends Canvas
implements ControlListener, MouseListener, MouseMoveListener, PaintListener,
MenuListener, PropertyChangeListener {
	private static final Rectangle ELLIPSE = new Rectangle();
	
	private Rectangle getEllipse() {
		return ELLIPSE.setLocation(30, 30).
			setSize(((controlWidth - 60) / 10) * 10,
					((controlHeight - 60) / 10) * 10);
	}
	
	private int controlWidth, controlHeight;
	private Rectangle pointsBounds = null;
	
	public Rectangle requireBounds() {
		if (pointsBounds == null)
			if (getModel() != null)
				pointsBounds = getModel().getPoints().getBounds();
		return pointsBounds;
	}
	
	private List<ModelObject> listeningTo = new ArrayList<ModelObject>();
	
	private void listenTo(ModelObject m) {
		if (m != null && listeningTo.add(m))
			m.addPropertyChangeListener(this);
	}
	
	private void stopListeningTo(ModelObject m) {
		if (m != null && listeningTo.remove(m))
			m.removePropertyChangeListener(this);
	}
	
	private Control model;
	
	public void setModel(Control model) {
		for (ModelObject i : listeningTo)
			i.removePropertyChangeListener(this);
		listeningTo.clear();
		
		this.model = model;
		listenTo(model);
		for (PortSpec i : model.getPorts())
			listenTo(i);
		pointsBounds = null;
		redraw();
	}
	
	public Control getModel() {
		return model;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		Object source = evt.getSource(),
				oldValue = evt.getOldValue(),
				newValue = evt.getNewValue();
		if (source instanceof Control) {
			if (Control.PROPERTY_PORT.equals(name)) {
				if (oldValue == null) {
					listenTo((PortSpec)newValue);
				} else if (newValue == null) {
					stopListeningTo((PortSpec)oldValue);
				}
				redraw();
			} else if (Control.PROPERTY_POINTS.equals(name) ||
					Control.PROPERTY_FILL.equals(name) ||
					Control.PROPERTY_OUTLINE.equals(name)) {
				redraw();
			}
		} else if (source instanceof PortSpec) {
			if (PortSpec.PROPERTY_SEGMENT.equals(name) ||
				PortSpec.PROPERTY_DISTANCE.equals(name))
				redraw();
		}
	}
	
	private IInputValidator getPortNameValidator(final PortSpec current) {
		return new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if (newText.length() == 0)
					return "Port names must not be empty.";
				for (PortSpec i : getPorts())
					if (i != current && i.getName().equals(newText))
						return "This port name is already in use.";
				return null;

			}
		};
	}
	
	private static IInputValidator getIntegerValidator(final int min, final int max) {
		return new IInputValidator() {
			@Override
			public String isValid(String newText) {
				try {
					int x = Integer.parseInt(newText);
					if (x < min || x > max)
						return "The value must not be less than " + min + " or greater than " + max;
					return null;
				} catch (NumberFormatException e) {
					return "The value is not an integer";
				}
			}
		};
	}
	
	private Point tmp = new Point();
	
	private Point roundedMousePosition = new Point(-10, -10),
	              mousePosition = new Point(-10, -10);
	
	/**
	 * The index of the point which is currently the subject of a drag
	 * operation - or, if {@link #newPoint} is <code>true</code>, the index
	 * that the newly created point <i>will</i> have.
	 */
	private int dragPointIndex = -1;
	/**
	 * Is a new point being created?
	 */
	private boolean newPoint = false;
	
	private int dragPortIndex = -1;
	
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
	
	private void doChange(Change c) {}
	
	private void opMovePoint(int moveIndex, int x, int y) {
		PointList pl = getModel().getPoints().getCopy();
		pl.setPoint(new Point(x, y), moveIndex);
		doChange(getModel().changePoints(pl));
	}
	
	private void opDeletePoint(int deleteIndex) {
		ChangeGroup cg = new ChangeGroup();
		Point p = getPoint(deleteIndex);
		Line l1 = new Line(getPoint(deleteIndex - 1), p),
				l2 = new Line(p, getPoint(deleteIndex + 1));
		double len1 = l1.getLength(),
				len2 = l2.getLength(),
				len = len1 + len2,
				l1l = len1 / len;
		if (dragPointIndex == deleteIndex)
			dragPointIndex = -1;
		for (PortSpec port : getPorts()) {
			int segment = port.getSegment();
			double distance = port.getDistance();
			if (segment == deleteIndex - 1) {
				cg.add(port.changeDistance(distance * l1l));
			} else if (segment == deleteIndex) {
				cg.add(port.changeDistance(l1l + (distance * (len2 / len))));
			}
			if (segment >= deleteIndex)
				cg.add(port.changeSegment(segment - 1));
		}
		PointList pl = getModel().getPoints().getCopy();
		pl.removePoint(deleteIndex);
		cg.add(getModel().changePoints(pl));
		doChange(cg);
	}
	
	private void opInsertPoint(int insertIndex, int x, int y) {
		ChangeGroup cg = new ChangeGroup();
		Point p = roundToGrid(x, y);
		Line l1 = new Line(getPoint(insertIndex - 1), p),
				l2 = new Line(p, getPoint(insertIndex));
		double pivot = l1.getLength() / (l1.getLength() + l2.getLength());
		for (PortSpec port : getPorts()) {
			int segment = port.getSegment();
			double distance = port.getDistance();
			if (segment == (insertIndex - 1)) {
				if (distance < pivot) {
					cg.add(port.changeDistance((pivot - distance) / pivot));
				} else {
					cg.add(port.changeSegment(segment + 1));
					cg.add(
						port.changeDistance((distance - pivot) / (1 - pivot)));
				}
			} else if (segment >= insertIndex) {
				cg.add(port.changeSegment(segment + 1));
			}
		}
		PointList pl = getModel().getPoints().getCopy();
		pl.insertPoint(p, insertIndex);
		cg.add(getModel().changePoints(pl));
		doChange(cg);
	}
	
	private void opAddPort(PortSpec port, String name, int segment, double distance) {
		ChangeGroup cg = new ChangeGroup();
		cg.add(getModel().changeAddPort(port));
		cg.add(port.changeName(name));
		cg.add(port.changeSegment(segment));
		cg.add(port.changeDistance(distance));
		doChange(cg);
	}
	
	private void opRenamePort(PortSpec port, String name) {
		doChange(port.changeName(name));
	}
	
	private void opMovePort(PortSpec port, int segment, double distance) {
		ChangeGroup cg = new ChangeGroup();
		cg.add(port.changeSegment(segment));
		cg.add(port.changeDistance(distance));
		doChange(cg);
	}
	
	private void opDeletePort(PortSpec port) {
		doChange(getModel().changeRemovePort(port));
	}
	
	private int getPointCount() {
		return getModel().getPoints().size();
	}
	
	private Shape getShape() {
		return getModel().getShape();
	}
	
	private List<PortSpec> getPorts() {
		return getModel().getPorts();
	}
	
	protected Point roundToGrid(int x, int y) {
		return new Point(
			(int)(Math.round(x / 10.0) * 10),
			(int)(Math.round(y / 10.0) * 10));
	}
	
	protected Point getPoint(int i) {
		PointList points = getModel().getPoints();
		Rectangle bounds = requireBounds();
		return points.getPoint((i + points.size()) % points.size()).
				translate((controlWidth - bounds.width) / 2,
						(controlHeight - bounds.height) / 2);
	}
	
	protected int findPortAt(int x, int y) {
		if (getShape() == Shape.POLYGON) {
			Line l = new Line();
			for (int i = 0; i < getPorts().size(); i++) {
				PortSpec p = getPorts().get(i);
				int segment = p.getSegment();
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				tmp.setLocation(l.getPointFromOffset(p.getDistance()));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		} else if (getShape() == Shape.OVAL) {
			Ellipse e =
				new Ellipse().setBounds(getEllipse());
			for (int i = 0; i < getPorts().size(); i++) {
				PortSpec p = getPorts().get(i);
				tmp.setLocation(e.getPointFromOffset(p.getDistance()));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		}
		return -1;
	}
	
	protected int findPointAt(int x, int y) {
		if (getShape() != Shape.POLYGON)
			return -1;
		for (int i = 0; i < getPointCount(); i++) {
			tmp = getPoint(i);
			if (x >= tmp.x - 3 && x <= tmp.x + 3 &&
				y >= tmp.y - 3 && y <= tmp.y + 3)
				return i;
		}
		return -1;
	}
	
	private int getNearestSegment(Point up, double threshold) {
		if (getShape() == Shape.OVAL)
			return 0;
		int index = -1;
		Line l = new Line();
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < getPointCount(); i++) {
			l.setFirstPoint(getPoint(i));
			l.setSecondPoint(getPoint(i + 1));
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
	 * Deletes the point under the crosshairs, if there is one (and if it isn't
	 * the last point remaining).
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (getShape() == Shape.OVAL)
			return;
		Point p = roundToGrid(e.x, e.y);
		int deleteIndex = findPointAt(p.x, p.y);
		if (deleteIndex != -1 && (deleteIndex != 0 || getPointCount() > 1))
			opDeletePoint(deleteIndex);
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
		dragPortIndex = findPortAt(up.x, up.y);
		if (dragPortIndex == -1) {
			dragPointIndex = findPointAt(p.x, p.y);
			if (dragPointIndex == -1 && getShape() == Shape.POLYGON) {
				if (getPointCount() == 1) {
					dragPointIndex = 0;
					newPoint = true;
				} else {
					int index = getNearestSegment(up, 15);
					
					if (index != -1) {
						dragPointIndex = index + 1;
						newPoint = true;
					}
				}
			}
		}
		redraw();
	}

	/**
	 * Creates or moves a point, or moves a port, depending on what the user
	 * initially clicked.
	 */
	@Override
	public void mouseUp(MouseEvent e) {
		if (dragPortIndex != -1) { /* a port is being moved */
			PortSpec p = getPorts().get(dragPortIndex);
			Line l = new Line();
			int segment;
			double offset;
			
			if (getShape() == Shape.POLYGON) {
				segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection != null)
					offset = l.getOffsetFromPoint(intersection);
				else offset = 0;
			} else {
				segment = 0;
				offset =
					new Ellipse(getEllipse()).getClosestOffset(mousePosition);
			}
			
			opMovePort(p, segment, offset);
			
			dragPortIndex = -1;
			redraw();
		} else if (dragPointIndex != -1) { /* a point is being manipulated */
			Point p = roundToGrid(e.x, e.y);
			int pointAtCursor = findPointAt(roundedMousePosition.x, roundedMousePosition.y);
			if (pointAtCursor == -1) {
				if (!newPoint) {
					opMovePoint(dragPointIndex, p.x, p.y);
				} else opInsertPoint(dragPointIndex, p.x, p.y);
			}
			dragPointIndex = -1;
			newPoint = false;
			redraw();
		}
	}
	
	private void fillCircleCentredAt(GC gc, Point p, int radius) {
		gc.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		e.gc.setAntialias(SWT.ON);
		e.gc.setLineWidth(2);
		setCursor(Cursors.ARROW);
		
		if (getEnabled() == false) {
			e.gc.setBackground(ColorConstants.lightGray);
			e.gc.setAlpha(128);
			e.gc.fillRectangle(0, 0, controlWidth, controlHeight);
			return;
		}
		
		e.gc.setForeground(ColorConstants.black);
		
		Line l = new Line();
		
		Color
			outline = getModel().getOutlineColour().getSWTColor(),
			fill = getModel().getFillColour().getSWTColor();
		
		e.gc.setForeground(outline);
		e.gc.setBackground(fill);
		
		List<PortSpec> ports = getPorts();
		if (getShape() == Shape.POLYGON) {
			/* toIntArray returns a reference to the internal array, so make a
			 * copy to avoid translating the original points */
			PointList points = getModel().getPoints().getCopy();
			Rectangle bounds = requireBounds();
			int[] pointArray = points.toIntArray();
			
			int dx = (controlWidth - bounds.width) / 2,
				dy = (controlHeight - bounds.height) / 2;
			for (int i = 0; i < pointArray.length; i += 2) {
				pointArray[i] += dx;
				pointArray[i + 1] += dy;
			}
			
			e.gc.fillPolygon(pointArray);
			e.gc.drawPolyline(pointArray);
			
			Point first = getPoint(0), last = getPoint(points.size() - 1);
			e.gc.drawLine(first.x, first.y, last.x, last.y);
			
			e.gc.setBackground(ColorConstants.black);
			for (int i = 0; i < points.size(); i++)
				fillCircleCentredAt(e.gc, getPoint(i), 3);
			
			e.gc.setBackground(ColorConstants.red);
			for (PortSpec p : ports) {
				int segment = p.getSegment();
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				
				fillCircleCentredAt(e.gc,
						l.getPointFromOffset(p.getDistance()), 4);
			}
		} else {
			Ellipse el = new Ellipse(getEllipse());
			int w = ((controlWidth - 60) / 10) * 10,
			    h = ((controlHeight - 60) / 10) * 10;
			e.gc.fillOval(30, 30, w, h);
			e.gc.drawOval(30, 30, w, h);
			
			e.gc.setBackground(ColorConstants.red);
			for (PortSpec p : ports) {
				fillCircleCentredAt(e.gc,
					el.getPointFromOffset(p.getDistance()), 4);
			}
		}
		
		if (dragPortIndex != -1) {
			e.gc.setAlpha(127);
			
			if (getShape() == Shape.POLYGON) {
				int segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection == null)
					intersection = getPoint(segment);
				tmp.setLocation(intersection);
			} else if (getShape() == Shape.OVAL) {
				tmp.setLocation(
					new Ellipse(getEllipse()).getClosestPoint(mousePosition));
			}
			
			fillCircleCentredAt(e.gc, tmp, 4);
		} else if (dragPointIndex != -1) {
			e.gc.setAlpha(127);
			Point previous, next;
			if (!newPoint) {
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
			fillCircleCentredAt(e.gc, roundedMousePosition, 3);
			
			int pointAtCursor = findPointAt(roundedMousePosition.x, roundedMousePosition.y);
			if (pointAtCursor != -1 && pointAtCursor != dragPointIndex)
				setCursor(Cursors.NO);
			
			e.gc.setAlpha(255);
		}
	}

	private int tippedPoint, tippedPort;
	
	private void updateToolTip() {
		int point = findPointAt(mousePosition.x, mousePosition.y),
		    port = findPortAt(mousePosition.x, mousePosition.y);
		if (point == -1)
			point = findPointAt(roundedMousePosition.x, roundedMousePosition.y);
		if (port == -1)
			port = findPortAt(roundedMousePosition.x, roundedMousePosition.y);
		
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
			roundedMousePosition.setLocation(currentMousePosition);
		} else if (dragPointIndex != -1 || dragPortIndex != -1)
			redraw();
		updateToolTip();
	}

	@Override
	public void controlMoved(ControlEvent e) {
		controlResized(e);
	}

	/**
	 * Updates the size information for this Canvas and schedules a redraw.
	 */
	@Override
	public void controlResized(ControlEvent e) {
		org.eclipse.swt.graphics.Point size = getSize();
		controlWidth = size.x; controlHeight = size.y;
		redraw();
	}

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
		
		final int foundPoint = findPointAt(roundedMousePosition.x, roundedMousePosition.y),
		          foundPort = findPortAt(mousePosition.x, mousePosition.y),
		          segment = getNearestSegment(roundedMousePosition, 15);
		
		UI.createMenuItem(m, SWT.NONE, "Polygon canvas", null).setEnabled(false);
		new MenuItem(m, SWT.SEPARATOR);
				
		if (foundPort == -1) {
			if (segment != -1) {
				UI.createMenuItem(m, 0, "Add &port", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int newSegment;
						double newDistance;
						
						if (getShape() == Shape.POLYGON) {
							Line l = new Line();
							l.setFirstPoint(getPoint(segment));
							l.setSecondPoint(getPoint(segment + 1));
							
							Point portPoint =
									l.getIntersection(roundedMousePosition);
							newSegment = segment;
							newDistance = l.getOffsetFromPoint(portPoint);
						} else {
							newSegment = 0;
							newDistance = new Ellipse(getEllipse()).
									getClosestOffset(mousePosition);
						}
						
						String newName = UI.promptFor("New port name",
								"Choose a name for the new port:", "",
								getPortNameValidator(null));
						if (newName != null)
							opAddPort(new PortSpec(),
									newName, newSegment, newDistance);
					}
				});
			}
		} else {
			UI.createMenuItem(m, 0, "Re&name port", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PortSpec p = getPorts().get(foundPort);
					String newName = UI.promptFor("Port name",
							"Choose a name for this port:",
							(p.getName() == null ? "" : p.getName()),
							getPortNameValidator(p));
					if (newName != null)
						opRenamePort(p, newName);
				}
			});
			
			UI.createMenuItem(m, 0, "&Remove port", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					opDeletePort(getPorts().get(foundPort));
				}
			});
		}
		
		if (foundPoint != -1) {
			if (foundPoint != 0 || getPointCount() > 1) {
				UI.createMenuItem(m, 0, "&Remove point", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						opDeletePoint(foundPoint);
					}
				});
			} else {
				UI.createMenuItem(m, 0, "Cannot remove last point", null).setEnabled(false);
			}
		}
		if (getPointCount() > 1) {
			UI.createMenuItem(m, 0, "Remove &all points and ports", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					/* XXX
					setMode(mode); */
				}
			});
		}
		if (m.getItemCount() > 0)
			new MenuItem(m, SWT.SEPARATOR);
		if (getShape() == Shape.POLYGON) {
			UI.createMenuItem(m, 0, "&Replace with a regular polygon", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String polySides =
						UI.promptFor("Specify the number of sides",
							"How many sides should your regular polygon have?\n(All ports will be deleted.)",
							"3", getIntegerValidator(3, Integer.MAX_VALUE));
					if (polySides != null) {
						/* XXX
						setMode(mode);
						
						setPoints(new Ellipse().
								setBounds(new Rectangle(0, 0, 60, 60)).
								getPolygon(Integer.parseInt(polySides))); */
					}
				}
			});
		}
	}
}
