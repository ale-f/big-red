package dk.itu.big_red.editors.assistants;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ModelObject.ChangeExtendedData;
import dk.itu.big_red.model.ModelObject.ExtendedDataValidator;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.names.policies.INamePolicy;

public final class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	private static Object require(
			IPropertyProviderProxy context, ModelObject o, String name,
			Class<?> klass) {
		Object r = (context != null && context.hasProperty(o, name) ?
				context.getProperty(o, name) : o.getExtendedData(name));
		return (klass.isInstance(r) ? r : null);
	}
	
	@RedProperty(fired = IFile.class, retrieved = IFile.class)
	public static final String FILE =
			"eD!+dk.itu.big_red.model.ModelObject.file";
	
	public static IFile getFile(ModelObject m) {
		return getFile(null, m);
	}
	
	public static IFile getFile(
			IPropertyProviderProxy context, ModelObject m) {
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
			IPropertyProviderProxy context, ModelObject m) {
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
			IPropertyProviderProxy context, ModelObject m) {
		Colour c = (Colour)require(context, m, FILL, Colour.class);
		if (c == null) {
			if (m instanceof Node) {
				c = getFill(context, ((Node)m).getControl());
			} else if (m instanceof Control) {
				c = new Colour("white");
			}
		}
		return c;
	}
	
	public static void setFill(ModelObject m, Colour c) {
		m.setExtendedData(FILL, c);
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
			IPropertyProviderProxy context, ModelObject m) {
		Colour c = (Colour)require(context, m, OUTLINE, Colour.class);
		if (c == null) {
			if (m instanceof Node) {
				c = getOutline(context, ((Node)m).getControl());
			} else if (m instanceof Control) {
				c = new Colour("black");
			} else if (m instanceof Link) {
				c = new Colour("green");
			}
		}
		return c;
	}
	
	public static void setOutline(ModelObject m, Colour c) {
		m.setExtendedData(OUTLINE, c);
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
			IPropertyProviderProxy context, Control c) {
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
				IPropertyProviderProxy context) {
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
			IPropertyProviderProxy context, Node n) {
		String s = (String)require(context, n, PARAMETER, String.class);
		if (s == null) {
			INamePolicy p = getParameterPolicy(context, n.getControl());
			if (p != null)
				s = p.get(0);
		}
		return s;
	}
	
	public static void setParameter(Node n, String s) {
		n.setExtendedData(PARAMETER, s);
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
	
	public static int getSegment(IPropertyProviderProxy context, PortSpec p) {
		Integer i = (Integer)require(context, p, SEGMENT, Integer.class);
		return (i != null ? i : -1);
	}
	
	public static void setSegment(PortSpec p, int i) {
		p.setExtendedData(SEGMENT, i);
	}
	
	public static Change changeSegment(PortSpec p, int i) {
		return p.changeExtendedData(SEGMENT, i);
	}
	
	@RedProperty(fired = Double.class, retrieved = Double.class)
	public static final String DISTANCE =
			"eD!+dk.itu.big_red.model.PortSpec.distance";
	
	public static double getDistance(PortSpec p) {
		return getDistance(null, p);
	}
	
	public static double getDistance(
			IPropertyProviderProxy context, PortSpec p) {
		Double d = (Double)require(context, p, DISTANCE, Double.class);
		return (d != null ? d : Double.NaN);
	}
	
	public static void setDistance(PortSpec p, double d) {
		p.setExtendedData(DISTANCE, d);
	}
	
	public static Change changeDistance(PortSpec p, double d) {
		return p.changeExtendedData(DISTANCE, d);
	}
	
	public static final String SHAPE =
			"eD!+dk.itu.big_red.model.Control.shape";
	
	public static Object getShape(Control c) {
		return getShape(null, c);
	}
	
	public static Object getShape(IPropertyProviderProxy context, Control c) {
		Object o = require(context, c, SHAPE, Object.class);
		if (o instanceof PointList || o instanceof Ellipse) {
			return o;
		} else return Control.POINTS_QUAD;
	}
	
	public static void setShape(Control c, Object s) {
		if (s instanceof PointList || s instanceof Ellipse)
			c.setExtendedData(SHAPE, s);
	}
	
	public static Change changeShape(Control c, Object s) {
		return c.changeExtendedData(SHAPE, s);
	}
	
	public static final String LABEL =
			"eD!+dk.itu.big_red.model.Control.label";
	
	public static String getLabel(Control c) {
		return getLabel(null, c);
	}
	
	private static String labelFor(String s) {
		return (s.length() > 0 ? s.substring(0, 1) : s);
	}
	
	public static String getLabel(IPropertyProviderProxy context, Control c) {
		String s = (String)require(context, c, LABEL, String.class);
		if (s == null)
			s = labelFor(c.getName(context));
		return s;
	}
	
	public static void setLabel(Control c, String s) {
		c.setExtendedData(LABEL, s);
	}
	
	public static Change changeLabel(Control c, String s) {
		return c.changeExtendedData(LABEL, s);
	}
	
	public static Change changeControlName(Control c, String s) {
		ChangeGroup cg = new ChangeGroup();
		cg.add(c.changeName(s));
		cg.add(changeLabel(c, labelFor(s)));
		return cg;
	}
}
