package dk.itu.big_red.editors.assistants;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ModelObject.ChangeExtendedData;
import dk.itu.big_red.model.ModelObject.ExtendedDataValidator;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.assistants.IPropertyProvider;
import dk.itu.big_red.model.assistants.Line;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.names.policies.INamePolicy;

public final class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	private static Object require(
			IPropertyProvider context, ModelObject o, String name,
			Class<?> klass) {
		Object r = (context != null && context.hasProperty(o, name) ?
				context.getProperty(o, name) : o.getExtendedData(name));
		return (klass.isInstance(r) ? r : null);
	}
	
	private static void set(IPropertyProvider context,
			ModelObject o, String name, Object value) {
		if (o == null || name == null)
			return;
		if (context != null) {
			context.setProperty(o, name, value);
		} else o.setExtendedData(name, value);
	}
	
	@RedProperty(fired = IFile.class, retrieved = IFile.class)
	public static final String FILE =
			"eD!+dk.itu.big_red.model.ModelObject.file";
	
	public static IFile getFile(ModelObject m) {
		return getFile(null, m);
	}
	
	public static IFile getFile(
			IPropertyProvider context, ModelObject m) {
		return (IFile)require(context, m, FILE, IFile.class);
	}
	
	public static void setFile(ModelObject m, IFile f) {
		m.setExtendedData(FILE, f);
	}
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String COMMENT =
			"eD!+dk.itu.big_red.model.ModelObject.comment";
	
	public static String getComment(ModelObject m) {
		return getComment(null, m);
	}
	
	public static String getComment(
			IPropertyProvider context, ModelObject m) {
		String s = (String)require(context, m, COMMENT, String.class);
		return (s != null ? s : "");
	}
	
	public static void setComment(ModelObject m, String s) {
		m.setExtendedData(COMMENT, (s.length() != 0 ? s : null));
	}
	
	public static Change changeComment(ModelObject m, String s) {
		return m.changeExtendedData(COMMENT, (s.length() != 0 ? s : null));
	}
	
	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String FILL =
			"eD!+dk.itu.big_red.model.Colourable.fill";
	
	public static Colour getFill(ModelObject m) {
		return getFill(null, m);
	}
	
	public static Colour getFill(
			IPropertyProvider context, ModelObject m) {
		Colour c = (Colour)require(context, m, FILL, Colour.class);
		if (c == null) {
			if (m instanceof Node) {
				c = getFill(context, ((Node)m).getControl());
			} else if (m instanceof Control) {
				c = new Colour("white");
			}
			if (c != null)
				setFill(context, m, c);
		}
		return c;
	}
	
	public static void setFill(ModelObject m, Colour c) {
		setFill(null, m, c);
	}
	
	public static void setFill(
			IPropertyProvider context, ModelObject m, Colour c) {
		set(context, m, FILL, c);
	}
	
	public static Change changeFill(ModelObject m, Colour c) {
		return m.changeExtendedData(FILL, c);
	}
	
	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String OUTLINE =
			"eD!+dk.itu.big_red.model.Colourable.outline";
	
	public static Colour getOutline(ModelObject m) {
		return getOutline(null, m);
	}
	
	public static Colour getOutline(
			IPropertyProvider context, ModelObject m) {
		Colour c = (Colour)require(context, m, OUTLINE, Colour.class);
		if (c == null) {
			if (m instanceof Node) {
				c = getOutline(context, ((Node)m).getControl());
			} else if (m instanceof Control) {
				c = new Colour("black");
			} else if (m instanceof Link) {
				c = new Colour("green");
			}
			if (c != null)
				setOutline(context, m, c);
		}
		return c;
	}
	
	public static void setOutline(ModelObject m, Colour c) {
		setOutline(null, m, c);
	}
	
	public static void setOutline(
			IPropertyProvider context, ModelObject m, Colour c) {
		set(context, m, OUTLINE, c);
	}
	
	public static Change changeOutline(ModelObject m, Colour c) {
		return m.changeExtendedData(OUTLINE, c);
	}
	
	@RedProperty(fired = INamePolicy.class, retrieved = INamePolicy.class)
	public static final String PARAMETER_POLICY =
			"eD!+dk.itu.big_red.model.Control.parameter-policy";
	
	public static INamePolicy getParameterPolicy(Control c) {
		return getParameterPolicy(null, c);
	}
	
	public static INamePolicy getParameterPolicy(
			IPropertyProvider context, Control c) {
		return (INamePolicy)require(
				context, c, PARAMETER_POLICY, INamePolicy.class);
	}
	
	public static void setParameterPolicy(Control c, INamePolicy n) {
		c.setExtendedData(PARAMETER_POLICY, n);
	}
	
	public static Change changeParameterPolicy(Control c, INamePolicy n) {
		return c.changeExtendedData(PARAMETER_POLICY, n);
	}
	
	private static final ExtendedDataValidator parameterValidator =
			new ExtendedDataValidator() {
		@Override
		public String validate(ChangeExtendedData c,
				IPropertyProvider context) {
			if (!(c.getCreator() instanceof Node))
				return c.getCreator() + " is not a Node";
			Node n = (Node)c.getCreator();
				
			Control control = n.getControl();
			INamePolicy policy = getParameterPolicy(control);
			if (policy == null)
				return "The control " + control.getName() +
						" does not define a parameter";
			
			if (!(c.newValue instanceof String))
				return "Parameter values must be strings";
			
			String value = (String)c.newValue;
			if ((c.newValue = policy.normalise(value)) == null)
				return "\"" + value + "\" is not a valid value for the " +
						"parameter of " + control.getName();
			return null;
		}
	};
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PARAMETER =
			"eD!+dk.itu.big_red.model.Node.parameter";
	
	public static String getParameter(Node n) {
		return getParameter(null, n);
	}
	
	public static String getParameter(
			IPropertyProvider context, Node n) {
		INamePolicy p = getParameterPolicy(context, n.getControl());
		String s = (String)require(context, n, PARAMETER, String.class),
				t = null;
		if (p != null)
			t = p.normalise(s);
		if (s != null ? !s.equals(t) : s != t)
			set(context, n, PARAMETER, t);
		return t;
	}
	
	public static void setParameter(Node n, String s) {
		setParameter(null, n, s);
	}
	
	public static void setParameter(
			IPropertyProvider context, Node n, String s) {
		set(context, n, PARAMETER, s);
	}
	
	public static Change changeParameter(Node n, String s) {
		return n.changeExtendedData(PARAMETER, s, parameterValidator);
	}
	
	@RedProperty(fired = Integer.class, retrieved = Integer.class)
	public static final String SEGMENT =
			"eD!+dk.itu.big_red.model.PortSpec.segment";
	
	public static int getSegment(PortSpec p) {
		return getSegment(null, p);
	}
	
	public static int getSegment(IPropertyProvider context, PortSpec p) {
		Integer i = (Integer)require(context, p, SEGMENT, Integer.class);
		if (i == null) {
			recalculatePosition(context, p);
			i = (Integer)require(context, p, SEGMENT, Integer.class);
		}
		return i;
	}
	
	public static void setSegment(PortSpec p, int i) {
		setSegment(null, p, i);
	}
	
	public static void setSegment(
			IPropertyProvider context, PortSpec p, int i) {
		set(context, p, SEGMENT, i);
	}
	
	public static Change changeSegment(PortSpec p, int i) {
		return p.changeExtendedData(SEGMENT, i);
	}
	
	@RedProperty(fired = Double.class, retrieved = Double.class)
	public static final String DISTANCE =
			"eD!+dk.itu.big_red.model.PortSpec.distance";
	
	private static void recalculatePosition(
			IPropertyProvider context, PortSpec p) {
		int segment = 0;
		double distance = 0;
		
		Object shape = getShape(context, p.getControl(context));
		List<PortSpec> l = p.getControl(context).getPorts(context);
		int index = l.indexOf(p) + 1;
		
		if (shape instanceof PointList) {
			PointList pl = (PointList)shape;
			int pls = pl.size();
			distance = (((double)pls) / l.size()) * index;
			
			segment = (int)Math.floor(distance);
			distance -= segment;
			segment %= pls;
		} else if (shape instanceof Ellipse) {
			segment = 0;
			distance = (1.0 / l.size()) * index;
		}
		
		setSegment(context, p, segment);
		setDistance(context, p, distance);
	}
	
	public static double getDistance(PortSpec p) {
		return getDistance(null, p);
	}
	
	public static double getDistance(
			IPropertyProvider context, PortSpec p) {
		Double d = (Double)require(context, p, DISTANCE, Double.class);
		if (d == null) {
			recalculatePosition(context, p);
			d = (Double)require(context, p, DISTANCE, Double.class);
		}
		return d;
	}
	
	public static void setDistance(PortSpec p, double d) {
		setDistance(null, p, d);
	}
	
	public static void setDistance(
			IPropertyProvider context, PortSpec p, double d) {
		set(context, p, DISTANCE, d);
	}
	
	public static Change changeDistance(PortSpec p, double d) {
		return p.changeExtendedData(DISTANCE, d);
	}
	
	public static final String SHAPE =
			"eD!+dk.itu.big_red.model.Control.shape";
	
	public static Object getShape(Control c) {
		return getShape(null, c);
	}
	
	public static Object getShape(IPropertyProvider context, Control c) {
		Object o = require(context, c, SHAPE, Object.class);
		if (!(o instanceof PointList || o instanceof Ellipse)) {
			o = new Ellipse(new Rectangle(0, 0, 300, 300)).
				getPolygon(Math.max(3, c.getPorts(context).size()));
			setShape(context, c, o);
			
			for (PortSpec p : c.getPorts(context))
				recalculatePosition(context, p);
		}
		return o;
	}
	
	public static void setShape(Control c, Object s) {
		setShape(null, c, s);
	}
	
	public static void setShape(
			IPropertyProvider context, Control c, Object s) {
		if (s instanceof PointList || s instanceof Ellipse)
			set(context, c, SHAPE, s);
	}
	
	public static Change changeShape(Control c, Object s) {
		return c.changeExtendedData(SHAPE, s);
	}
	
	private static final ExtendedDataValidator labelValidator =
			new ExtendedDataValidator() {
		@Override
		public String validate(
				ChangeExtendedData c, IPropertyProvider context) {
			if (!(c.newValue instanceof String)) {
				return "Labels must be strings";
			} else if (((String)c.newValue).length() == 0) {
				return "Labels must not be empty";
			} else return null;
		}
	};
	
	public static final String LABEL =
			"eD!+dk.itu.big_red.model.Control.label";
	
	public static String getLabel(Control c) {
		return getLabel(null, c);
	}
	
	private static String labelFor(String s) {
		return (s != null ? (s.length() > 0 ? s.substring(0, 1) : s) : "?");
	}
	
	public static String getLabel(IPropertyProvider context, Control c) {
		String s = (String)require(context, c, LABEL, String.class);
		if (s == null)
			set(context, c, LABEL, s = labelFor(c.getName(context)));
		return s;
	}
	
	public static void setLabel(Control c, String s) {
		setLabel(null, c, s);
	}
	
	public static void setLabel(
			IPropertyProvider context, Control c, String s) {
		set(context, c, LABEL, s);
	}
	
	public static Change changeLabel(Control c, String s) {
		return c.changeExtendedData(LABEL, s, labelValidator);
	}
	
	public static Change changeControlName(Control c, String s) {
		ChangeGroup cg = new ChangeGroup();
		cg.add(c.changeName(s));
		cg.add(changeLabel(c, labelFor(s)));
		return cg;
	}
	
	public static final String LAYOUT =
			"eD!+dk.itu.big_red.Layoutable.layout";
	
	public static Rectangle getLayout(Layoutable l) {
		return getLayout(null, l);
	}
	
	public static Rectangle getLayout(
			IPropertyProvider context, Layoutable l) {
		Rectangle r = (Rectangle)require(context, l, LAYOUT, Rectangle.class);
		if (r == null) {
			if (l instanceof Port) {
				Port p = (Port)l;
				r = new Rectangle(0, 0, 10, 10);
				PointList polypt = p.getParent().getFittedPolygon();
				double distance = getDistance(context, p.getSpec());
				if (polypt != null) {
					int segment = getSegment(context, p.getSpec());
					org.eclipse.draw2d.geometry.Point
						p1 = polypt.getPoint(segment),
						p2 = polypt.getPoint((segment + 1) % polypt.size());
					r.setLocation(new Line(p1, p2).
							getPointFromOffset(distance).translate(-5, -5));
				} else {
					r.setLocation(
						new Ellipse(
							getLayout(p.getParent()).getCopy().setLocation(0, 0)).
							getPointFromOffset(distance).translate(-5, -5));
				}
			} else if (l instanceof Edge) {
				List<Point> points = ((Edge)l).getPoints(context);
				int s = points.size();
				r = new Rectangle(50, 50, 10, 10);
				if (s != 0) {
					int tx = 0, ty = 0;
					for (Point p : points) {
						Rectangle rect = getRootLayout(context, p);
						tx += rect.x; ty += rect.y;
					}
					r.setLocation(tx / s, ty / s);
				}
			} else if (!(l instanceof Bigraph))
				set(context, l, LAYOUT, r = new Rectangle());
		}
		return r;
	}
	
	public static void setLayout(Layoutable l, Rectangle r) {
		setLayout(null, l, r);
	}
	
	public static void setLayout(
			IPropertyProvider context, Layoutable l, Rectangle r) {
		set(context, l, LAYOUT, r);
	}
	
	public static Change changeLayout(Layoutable l, Rectangle r) {
		return l.changeExtendedData(LAYOUT, r);
	}
	
	public static Rectangle getRootLayout(Layoutable l) {
		return getRootLayout(null, l);
	}
	
	public static Rectangle getRootLayout(
			IPropertyProvider context, Layoutable l) {
		Rectangle r = getLayout(context, l), r2;
		if (r != null) {
			r = r.getCopy();
			while ((l = l.getParent(context)) != null &&
					(r2 = getLayout(context, l)) != null)
				r.translate(r2.x, r2.y);
		}
		return r;
	}
}
