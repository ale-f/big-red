package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Control;
import org.bigraph.model.ModelObject;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Store;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ControlUtilities;
import dk.itu.big_red.model.Ellipse;
import dk.itu.big_red.model.Line;
import dk.itu.big_red.utilities.ui.ColorWrapper;
import dk.itu.big_red.utilities.ui.UI;

import static dk.itu.big_red.model.ControlUtilities.getDistance;
import static dk.itu.big_red.model.ControlUtilities.getSegment;

/**
 * SignatureEditorPolygonCanvases are widgets based on {@link Canvas} that let
 * the user design a polygon. They keep track of a {@link PointList}, and the
 * user can modify that PointList by clicking on the widget.
 * @author alec
 */
public class SignatureEditorPolygonCanvas extends Canvas implements
		ControlListener, MouseListener, MouseMoveListener, PaintListener,
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
	
	private Object shape;
	
	private Object getShape() {
		if (shape == null)
			shape = ControlUtilities.getShape(getModel());
		return shape;
	}
	
	private PointList getPoints() {
		Object shape = getShape();
		return (shape instanceof PointList ? (PointList)shape : null);
	}
	
	public Rectangle requireBounds() {
		if (pointsBounds == null)
			if (getModel() != null)
				pointsBounds = getPoints().getBounds();
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
		shape = null;
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
			} else if (ControlUtilities.SHAPE.equals(name)) {
				shape = null;
				pointsBounds = null;
				redraw();
			} else if (ColourUtilities.FILL.equals(name) ||
					ColourUtilities.OUTLINE.equals(name)) {
				redraw();
			}
		} else if (source instanceof PortSpec) {
			if (ControlUtilities.SEGMENT.equals(name) ||
				ControlUtilities.DISTANCE.equals(name) ||
				PortSpec.PROPERTY_NAME.equals(name))
				redraw();
		}
	}
	
	private IInputValidator getPortNameValidator(final PortSpec current) {
		return new IInputValidator() {
			@Override
			public String isValid(String newText) {
				IChangeDescriptor c = (current != null ?
						new NamedModelObject.ChangeNameDescriptor(
								current.getIdentifier(), newText) :
						new Control.ChangeAddPortSpecDescriptor(
								new PortSpec.Identifier(
										newText, getModel().getIdentifier())));
				try {
					DescriptorExecutorManager.getInstance().tryValidateChange(
							getModel().getSignature(), c);
					return null;
				} catch (ChangeCreationException e) {
					return e.getRationale();
				}
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
		Rectangle r = requireBounds();
		return roundToGrid(((controlWidth - r.width) / 2) - r.x);
	}
	
	private int translationY() {
		Rectangle r = requireBounds();
		return roundToGrid(((controlHeight - r.height) / 2) - r.y);
	}
	
	private void doChange(IChangeDescriptor c) {
		editor.doChange(c);
	}
	
	private void opSetShape(Object shape) {
		doChange(new ControlUtilities.ChangeShapeDescriptor(
				(PropertyScratchpad)null, getModel(), shape));
	}
	
	private void opMovePoint(int moveIndex, int mx, int my) {
		int x = mx - translationX(), y = my - translationY();
		PointList pl = getPoints().getCopy();
		pl.setPoint(new Point(x, y), moveIndex);
		opSetShape(pl);
	}
	
	private void opDeletePoint(int deleteIndex) {
		ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
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
			int segment = getSegment(port);
			double distance = getDistance(port);
			if (segment == deleteIndex - 1) {
				cg.add(new ControlUtilities.ChangeDistanceDescriptor(
						null, port, distance * l1l));
			} else if (segment == deleteIndex) {
				cg.add(new ControlUtilities.ChangeDistanceDescriptor(
						null, port, l1l + (distance * (len2 / len))));
			}
			if (segment >= deleteIndex)
				cg.add(new ControlUtilities.ChangeSegmentDescriptor(
						null, port, segment - 1));
		}
		PointList pl = getPoints().getCopy();
		pl.removePoint(deleteIndex);
		opSetShape(pl);
		doChange(cg);
	}
	
	private void opDeletePort(int deleteIndex) {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		PortSpec p = getModel().getPorts().get(deleteIndex);
		PortSpec.Identifier pid = p.getIdentifier();
		cdg.add(new Store.ToStoreDescriptor(
				pid, Store.getInstance().createID()));
		cdg.add(new Control.ChangeRemovePortSpecDescriptor(pid));
		doChange(cdg);
	}
	
	private ChangeDescriptorGroup changeDeleteAllPorts() {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		for (PortSpec p : getModel().getPorts()) {
			PortSpec.Identifier pid = p.getIdentifier();
			cdg.add(new Store.ToStoreDescriptor(
					pid, Store.getInstance().createID()));
			cdg.add(new Control.ChangeRemovePortSpecDescriptor(pid));
		}
		return cdg;
	}
	
	private void opInsertPoint(int insertIndex, int mx, int my) {
		int x = mx - translationX(), y = my - translationY();
		ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
		Point p = roundToGrid(x, y);
		Line l1 = new Line(getPoint(insertIndex - 1), p),
				l2 = new Line(p, getPoint(insertIndex));
		double pivot = l1.getLength() / (l1.getLength() + l2.getLength());
		for (PortSpec port : getModel().getPorts()) {
			int segment = getSegment(port);
			double distance = getDistance(port);
			if (segment == (insertIndex - 1)) {
				if (distance < pivot) {
					cg.add(new ControlUtilities.ChangeDistanceDescriptor(
							null, port, (pivot - distance) / pivot));
				} else {
					cg.add(new ControlUtilities.ChangeSegmentDescriptor(
							null, port, segment + 1));
					cg.add(new ControlUtilities.ChangeDistanceDescriptor(
							null, port, (distance - pivot) / (1 - pivot)));
				}
			} else if (segment >= insertIndex) {
				cg.add(new ControlUtilities.ChangeSegmentDescriptor(
						null, port, segment + 1));
			}
		}
		PointList pl = getPoints().getCopy();
		pl.insertPoint(p, insertIndex);
		opSetShape(pl);
		doChange(cg);
	}
	
	private void opAddPort(
			PortSpec port, String name, int segment, double distance) {
		ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
		if (distance >= 1.0) {
			distance = 0.0;
			segment = (segment + 1) % getPointCount();
		}
		PortSpec.Identifier id =
				new PortSpec.Identifier(name, getModel().getIdentifier());
		cg.add(new Control.ChangeAddPortSpecDescriptor(id));
		cg.add(new ControlUtilities.ChangeSegmentDescriptor(id, 0, segment));
		cg.add(new ControlUtilities.ChangeDistanceDescriptor(id, 0, distance));
		doChange(cg);
	}
	
	private void opMovePort(PortSpec port, int segment, double distance) {
		ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
		if (distance >= 1.0) {
			distance = 0.0;
			segment = (segment + 1) % getPointCount();
		}
		cg.add(new ControlUtilities.ChangeSegmentDescriptor(
				null, port, segment));
		cg.add(new ControlUtilities.ChangeDistanceDescriptor(
				null, port, distance));
		doChange(cg);
	}
	
	private int getPointCount() {
		PointList pl = getPoints();
		return (pl != null ? pl.size() : 0);
	}
	
	protected int roundToGrid(int x) {
		return (int)(Math.round(x / 10.0) * 10);
	}
	
	protected Point roundToGrid(int x, int y) {
		return new Point(roundToGrid(x), roundToGrid(y));
	}
	
	protected Point getPoint(int i) {
		PointList points = getPoints();
		return points.getPoint((i + points.size()) % points.size()).
				translate(translationX(), translationY());
	}
	
	protected int findPortAt(int x, int y) {
		Object shape = getShape();
		if (shape instanceof PointList) {
			Line l = new Line();
			for (int i = 0; i < getModel().getPorts().size(); i++) {
				PortSpec p = getModel().getPorts().get(i);
				int segment = getSegment(p);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				tmp.setLocation(l.getPointFromOffset(getDistance(p)));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		} else if (shape instanceof Ellipse) {
			Ellipse e = getEllipse();
			for (int i = 0; i < getModel().getPorts().size(); i++) {
				PortSpec p = getModel().getPorts().get(i);
				tmp.setLocation(e.getPointFromOffset(getDistance(p)));
				if (x >= tmp.x - 4 && x <= tmp.x + 4 &&
					y >= tmp.y - 4 && y <= tmp.y + 4)
					return i;
			}
		}
		return -1;
	}
	
	protected int findPointAt(int x, int y) {
		for (int i = 0; i < getPointCount(); i++) {
			tmp = getPoint(i);
			if (x >= tmp.x - 3 && x <= tmp.x + 3 &&
				y >= tmp.y - 3 && y <= tmp.y + 3)
				return i;
		}
		return -1;
	}
	
	private int getNearestSegment(Point up, double threshold) {
		int index = -1;
		Line l = new Line();
		double distance = Double.MAX_VALUE;
		if (getShape() instanceof PointList) {
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
		} else if (getShape() instanceof Ellipse) {
			index = 0;
			distance = getEllipse().getClosestPoint(up).getDistance(up);
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
		int deleteIndex = findPointAt(e.x, e.y);
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
		dragPortIndex = findPortAt(e.x, e.y);
		if (dragPortIndex == -1) {
			dragPointIndex = findPointAt(e.x, e.y);
			if (dragPointIndex == -1 && getShape() instanceof PointList) {
				if (getPointCount() == 1) {
					dragPointIndex = 0;
					newPoint = true;
				} else {
					int index = getNearestSegment(new Point(e.x, e.y), 15);
					
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
			
			if (getShape() instanceof PointList) {
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
	
	private static void fillCircleCentredAt(GC gc, Point p, int radius) {
		gc.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
	}
	
	private static void drawPortName(GC gc, String name, Point portCenter) {
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
		Cursor cursor = Cursors.ARROW;
		
		GC gc = e.gc;
		
		gc.setAntialias(SWT.ON);
		gc.setLineWidth(2);
		
		if (getEnabled() == false) {
			gc.setBackground(ColorConstants.lightGray);
			gc.setAlpha(128);
			gc.fillRectangle(0, 0, controlWidth, controlHeight);
			return;
		}
		
		Line l = new Line();
		
		gc.setForeground(
			outline.update(ColourUtilities.getOutline(model)));
		gc.setBackground(
			fill.update(ColourUtilities.getFill(model)));
		
		Object shape = getShape();
		if (shape instanceof PointList) {
			/* toIntArray returns a reference to the internal array, so make a
			 * copy to avoid translating the original points */
			PointList points = ((PointList)shape).getCopy();
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
			gc.setBackground(ColorConstants.red);
			
			for (PortSpec p : getModel().getPorts()) {
				int segment = getSegment(p);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				
				Point portCenter = l.getPointFromOffset(getDistance(p));
				fillCircleCentredAt(gc, portCenter, 4);
				drawPortName(gc, p.getName(), portCenter);
			}
		} else {
			Ellipse el = getEllipse();
			int w = ((controlWidth - 60) / 10) * 10,
			    h = ((controlHeight - 60) / 10) * 10;
			gc.fillOval(30, 30, w, h);
			gc.drawOval(30, 30, w, h);
			
			gc.setForeground(ColorConstants.black);
			gc.setBackground(ColorConstants.red);
			for (PortSpec p : getModel().getPorts()) {
				Point portCenter = el.getPointFromOffset(getDistance(p));
				fillCircleCentredAt(gc, portCenter, 4);
				drawPortName(gc, p.getName(), portCenter);
			}
		}
		
		if (dragPortIndex != -1) {
			gc.setAlpha(127);
			
			if (shape instanceof PointList) {
				int segment = getNearestSegment(mousePosition, Double.MAX_VALUE);
				l.setFirstPoint(getPoint(segment));
				l.setSecondPoint(getPoint(segment + 1));
				Point intersection = l.getIntersection(mousePosition);
				if (intersection == null)
					intersection = getPoint(segment);
				tmp.setLocation(intersection);
			} else if (shape instanceof Ellipse) {
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
				cursor = Cursors.NO;
			
			gc.setAlpha(255);
		}
		
		setCursor(cursor);
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
		
		final int foundPoint = findPointAt(mousePosition.x, mousePosition.y),
		          foundPort = findPortAt(mousePosition.x, mousePosition.y),
		          segment = getNearestSegment(mousePosition, 15);
		
		UI.createMenuItem(m, SWT.NONE, "Polygon canvas", null).setEnabled(false);
		new MenuItem(m, SWT.SEPARATOR);
				
		if (foundPort == -1) {
			if (segment != -1) {
				UI.createMenuItem(m, 0, "Add &port", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int newSegment;
						double newDistance;
						
						if (getShape() instanceof PointList) {
							Line l = new Line();
							l.setFirstPoint(getPoint(segment));
							l.setSecondPoint(getPoint(segment + 1));
							
							Point portPoint =
									l.getIntersection(mousePosition);
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
						doChange(new NamedModelObject.ChangeNameDescriptor(
								p.getIdentifier(), newName));
				}
			});
			
			UI.createMenuItem(m, 0, "&Remove port", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					opDeletePort(foundPort);
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
				UI.createMenuItem(m, 0, "Cannot remove last point", null).
					setEnabled(false);
			}
		}
		if (getPointCount() > 1) {
			UI.createMenuItem(m, 0, "Remove &all points and ports",
					new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ChangeDescriptorGroup cdg = changeDeleteAllPorts();
					cdg.add(new ControlUtilities.ChangeShapeDescriptor(
							(PropertyScratchpad)null, getModel(),
							new PointList(new int[] { 0, 0 })));
					doChange(cdg);
				}
			});
		}
		
		if (getShape() instanceof PointList) {
			if (m.getItemCount() > 0)
				new MenuItem(m, SWT.SEPARATOR);
			UI.createMenuItem(m, 0, "&Replace with a regular polygon",
					new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String polySides =
						UI.promptFor("Specify the number of sides",
							"How many sides should your regular polygon have?" +
							"\n(All ports will be deleted.)",
							"3", getIntegerValidator(3, Integer.MAX_VALUE));
					if (polySides != null) {
						ChangeDescriptorGroup cdg = changeDeleteAllPorts();
						Ellipse el = Ellipse.SINGLETON.setBounds(
								new Rectangle(0, 0, 60, 60));
						cdg.add(new ControlUtilities.ChangeShapeDescriptor(
								(PropertyScratchpad)null, getModel(),
								el.getPolygon(Integer.parseInt(
										polySides))));
						doChange(cdg);
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
