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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.assistants.Line;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.utilities.ui.ColorWrapper;
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
	private static final Ellipse ELLIPSE = new Ellipse();
	
	private Ellipse getEllipse() {
		return ELLIPSE.setBounds(
			new Rectangle(30, 30,
					((controlWidth - 60) / 10) * 10,
					((controlHeight - 60) / 10) * 10));
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
	private ColorWrapper
		fill = new ColorWrapper(), outline = new ColorWrapper();
	
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
			} else if (Control.PROPERTY_POINTS.equals(name)) {
				pointsBounds = null;
				redraw();
			} else if (ExtendedDataUtilities.FILL.equals(name) ||
					ExtendedDataUtilities.OUTLINE.equals(name)) {
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
				for (PortSpec i : getModel().getPorts())
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
	
	private SignatureEditor editor;
	
	public SignatureEditorPolygonCanvas(SignatureEditor editor, Composite parent, int style) {
		super(parent, style);
		addMouseListener(this);
		addPaintListener(this);
		addControlListener(this);
		addMouseMoveListener(this);
		
		Menu menu = new Menu(this);
		menu.addMenuListener(this);
		setMenu(menu);
		
		this.editor = editor;
	}
	
	private int translationX() {
		return roundToGrid((controlWidth - requireBounds().width) / 2);
	}
	
	private int translationY() {
		return roundToGrid((controlHeight - requireBounds().height) / 2);
	}
	
	private void doChange(Change c) {
		editor.doChange(c);
	}
	
	private void opMovePoint(int moveIndex, int mx, int my) {
		int x = mx - translationX(), y = my - translationY();
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
		for (PortSpec port : getModel().getPorts()) {
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
	
	private void opInsertPoint(int insertIndex, int mx, int my) {
		int x = mx - translationX(), y = my - translationY();
		ChangeGroup cg = new ChangeGroup();
		Point p = roundToGrid(x, y);
		Line l1 = new Line(getPoint(insertIndex - 1), p),
				l2 = new Line(p, getPoint(insertIndex));
		double pivot = l1.getLength() / (l1.getLength() + l2.getLength());
		for (PortSpec port : getModel().getPorts()) {
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
		if (distance >= 1.0) {
			distance = 0.0;
			segment = (segment + 1) % getPointCount();
		}
		cg.add(getModel().changeAddPort(port, name));
		cg.add(port.changeSegment(segment));
		cg.add(port.changeDistance(distance));
		doChange(cg);
	}
	
	private void opMovePort(PortSpec port, int segment, double distance) {
		ChangeGroup cg = new ChangeGroup();
		if (distance >= 1.0) {
			distance = 0.0;
			segment = (segment + 1) % getPointCount();
		}
		cg.add(port.changeSegment(segment));
		cg.add(port.changeDistance(distance));
		doChange(cg);
	}
	
	private int getPointCount() {
		return getModel().getPoints().size();
	}
	
	protected int roundToGrid(int x) {
		return (int)(Math.round(x / 10.0) * 10);
	}
	
	protected Point roundToGrid(int x, int y) {
		return new Point(roundToGrid(x), roundToGrid(y));
	}
	
	protected Point getPoint(int i) {
		PointList points = getModel().getPoints();
		return points.getPoint((i + points.size()) % points.size()).
				translate(translationX(), translationY());
	}
	
	protected int findPortAt(int x, int y) {
		if (getModel().getShape() == Shape.POLYGON) {
			Line l = new Line();
			for (int i = 0; i < getModel().getPorts().size(); i++) {
				PortSpec p = getModel().getPorts().get(i);
				int segment = p.getSegment();
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				tmp.setLocation(l.getPointFromOffset(p.getDistance()));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		} else if (getModel().getShape() == Shape.OVAL) {
			Ellipse e = getEllipse();
			for (int i = 0; i < getModel().getPorts().size(); i++) {
				PortSpec p = getModel().getPorts().get(i);
				tmp.setLocation(e.getPointFromOffset(p.getDistance()));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		}
		return -1;
	}
	
	protected int findPointAt(int x, int y) {
		if (getModel().getShape() != Shape.POLYGON)
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
		if (getModel().getShape() == Shape.OVAL)
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
		if (getModel().getShape() == Shape.OVAL)
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
		if (e.button != 1 || (e.stateMask & SWT.MODIFIER_MASK) != 0)
			return;
		Point p = roundToGrid(e.x, e.y),
		      up = new Point(e.x, e.y);
		dragPortIndex = findPortAt(up.x, up.y);
		if (dragPortIndex == -1) {
			dragPointIndex = findPointAt(p.x, p.y);
			if (dragPointIndex == -1 && getModel().getShape() == Shape.POLYGON) {
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
			PortSpec p = getModel().getPorts().get(dragPortIndex);
			Line l = new Line();
			int segment;
			double offset;
			
			if (getModel().getShape() == Shape.POLYGON) {
				segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection != null)
					offset = l.getOffsetFromPoint(intersection);
				else offset = 0;
			} else {
				segment = 0;
				offset = getEllipse().getClosestOffset(mousePosition);
			}
			
			opMovePort(p, segment, offset);
			
			dragPortIndex = -1;
			redraw();
		} else if (dragPointIndex != -1) { /* a point is being manipulated */
			Point p = roundToGrid(e.x, e.y);
			int pointAtCursor = findPointAt(p.x, p.y);
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
	
	private void drawPortName(GC gc, String name, Point portCenter) {
		org.eclipse.swt.graphics.Point extent = gc.textExtent(name);
		
		int
			nameX = portCenter.x - (extent.x / 2),
			nameY = portCenter.y - (extent.y / 2) - 18;
		
		int oldAlpha = gc.getAlpha();
		gc.setAlpha(127);
		try {
			gc.drawText(name, nameX, nameY, SWT.DRAW_TRANSPARENT);
		} finally {
			gc.setAlpha(oldAlpha);
		}
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		
		gc.setAntialias(SWT.ON);
		gc.setLineWidth(2);
		setCursor(Cursors.ARROW);
		
		if (getEnabled() == false) {
			gc.setBackground(ColorConstants.lightGray);
			gc.setAlpha(128);
			gc.fillRectangle(0, 0, controlWidth, controlHeight);
			return;
		}
		
		Line l = new Line();
		
		gc.setForeground(
			outline.update(ExtendedDataUtilities.getOutline(model)));
		gc.setBackground(
			fill.update(ExtendedDataUtilities.getFill(model)));
		
		if (getModel().getShape() == Shape.POLYGON) {
			/* toIntArray returns a reference to the internal array, so make a
			 * copy to avoid translating the original points */
			PointList points = getModel().getPoints().getCopy();
			int[] pointArray = points.toIntArray();
			
			int dx = translationX(), dy = translationY();
			for (int i = 0; i < pointArray.length; i += 2) {
				pointArray[i] += dx;
				pointArray[i + 1] += dy;
			}
			
			gc.fillPolygon(pointArray);
			gc.drawPolyline(pointArray);
			
			Point first = getPoint(0), last = getPoint(points.size() - 1);
			gc.drawLine(first.x, first.y, last.x, last.y);
			
			gc.setBackground(ColorConstants.black);
			for (int i = 0; i < points.size(); i++)
				fillCircleCentredAt(gc, getPoint(i), 3);
			
			gc.setForeground(ColorConstants.black);
			
			for (PortSpec p : getModel().getPorts()) {
				gc.setBackground(ColorConstants.red);
				
				int segment = p.getSegment();
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				
				Point portCenter = l.getPointFromOffset(p.getDistance());
				fillCircleCentredAt(gc, portCenter, 4);
				drawPortName(gc, p.getName(), portCenter);
			}
		} else {
			Ellipse el = getEllipse();
			int w = ((controlWidth - 60) / 10) * 10,
			    h = ((controlHeight - 60) / 10) * 10;
			gc.fillOval(30, 30, w, h);
			gc.drawOval(30, 30, w, h);
			
			gc.setBackground(ColorConstants.red);
			for (PortSpec p : getModel().getPorts()) {
				Point portCenter = el.getPointFromOffset(p.getDistance());
				fillCircleCentredAt(gc, portCenter, 4);
				drawPortName(gc, p.getName(), portCenter);
			}
		}
		
		if (dragPortIndex != -1) {
			gc.setAlpha(127);
			
			if (getModel().getShape() == Shape.POLYGON) {
				int segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection == null)
					intersection = getPoint(segment);
				tmp.setLocation(intersection);
			} else if (getModel().getShape() == Shape.OVAL) {
				tmp.setLocation(getEllipse().getClosestPoint(mousePosition));
			}
			
			fillCircleCentredAt(gc, tmp, 4);
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
			
			gc.setForeground(ColorConstants.red);
			gc.setBackground(ColorConstants.red);
			
			gc.drawLine(previous.x, previous.y, roundedMousePosition.x, roundedMousePosition.y);
			gc.drawLine(next.x, next.y, roundedMousePosition.x, roundedMousePosition.y);
			fillCircleCentredAt(gc, roundedMousePosition, 3);
			
			int pointAtCursor = findPointAt(roundedMousePosition.x, roundedMousePosition.y);
			if (pointAtCursor != -1 && pointAtCursor != dragPointIndex)
				setCursor(Cursors.NO);
			
			gc.setAlpha(255);
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
			toolTipText = "Port " + getModel().getPorts().get(port).getName() + " and point " + point;
		} else if (port != -1) {
			toolTipText = "Port " + getModel().getPorts().get(port).getName();
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
						
						if (getModel().getShape() == Shape.POLYGON) {
							Line l = new Line();
							l.setFirstPoint(getPoint(segment));
							l.setSecondPoint(getPoint(segment + 1));
							
							Point portPoint =
									l.getIntersection(roundedMousePosition);
							if (portPoint != null) {
								newSegment = segment;
								newDistance = l.getOffsetFromPoint(portPoint);
							} else {
								newSegment = segment;
								newDistance = 0.0;
							}
						} else {
							newSegment = 0;
							newDistance =
								getEllipse().getClosestOffset(mousePosition);
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
					PortSpec p = getModel().getPorts().get(foundPort);
					String newName = UI.promptFor("Port name",
							"Choose a name for this port:",
							(p.getName() == null ? "" : p.getName()),
							getPortNameValidator(p));
					if (newName != null)
						doChange(p.changeName(newName));
				}
			});
			
			UI.createMenuItem(m, 0, "&Remove port", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doChange(getModel().changeRemovePort(getModel().getPorts().get(foundPort)));
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
					ChangeGroup cg = new ChangeGroup();
					for (PortSpec i : getModel().getPorts())
						cg.add(getModel().changeRemovePort(i));
					cg.add(getModel().changePoints(
							new PointList(new int[] { 0, 0 })));
					doChange(cg);
				}
			});
		}
		if (getModel().getShape() == Shape.POLYGON) {
			if (m.getItemCount() > 0)
				new MenuItem(m, SWT.SEPARATOR);
			UI.createMenuItem(m, 0, "&Replace with a regular polygon", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String polySides =
						UI.promptFor("Specify the number of sides",
							"How many sides should your regular polygon have?\n(All ports will be deleted.)",
							"3", getIntegerValidator(3, Integer.MAX_VALUE));
					if (polySides != null) {
						ChangeGroup cg = new ChangeGroup();
						for (PortSpec i : getModel().getPorts())
							cg.add(getModel().changeRemovePort(i));
						cg.add(getModel().changePoints(
								new Ellipse().
								setBounds(new Rectangle(0, 0, 60, 60)).
								getPolygon(Integer.parseInt(polySides))));
						doChange(cg);
					}
				}
			});
		}
	}
	
	@Override
	public void dispose() {
		if (isDisposed())
			return;
		fill.update(null);
		outline.update(null);
		fill = outline = null;
		model = null;
		super.dispose();
	}
}
