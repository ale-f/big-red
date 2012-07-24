package dk.itu.big_red.editors.assistants;

import java.util.List;

import org.bigraph.model.Control;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Site;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ExtendedDataValidator;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.policies.BoundedIntegerNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * The <strong>ExtendedDataUtilities</strong> class is a collection of static
 * methods and fields for manipulating some of the extended data used by Big
 * Red.
 * @author alec
 * @see ColourUtilities
 * @see LayoutUtilities
 */
public abstract class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	static <T> T require(PropertyScratchpad context, ModelObject o,
			String name, Class<T> klass) {
		if (o != null) {
			Object r = (context != null && context.hasProperty(o, name) ?
					context.getProperty(o, name) : o.getExtendedData(name));
			try {
				return klass.cast(r);
			} catch (ClassCastException ex) {
				return null;
			}
		} else return null;
	}
	
	static void set(PropertyScratchpad context, ModelObject o, String name,
			Object value) {
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
			PropertyScratchpad context, ModelObject m) {
		return require(context, m, FILE, IFile.class);
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
			PropertyScratchpad context, ModelObject m) {
		return require(context, m, COMMENT, String.class);
	}
	
	public static void setComment(ModelObject m, String s) {
		m.setExtendedData(COMMENT, s);
	}
	
	public static IChange changeComment(ModelObject m, String s) {
		return m.changeExtendedData(COMMENT, s);
	}
	
	public static IChangeDescriptor changeCommentDescriptor(
			ModelObject.Identifier l, String s) {
		return new ModelObject.ChangeExtendedDataDescriptor(
				l, COMMENT, s, null, null);
	}
	
	@RedProperty(fired = INamePolicy.class, retrieved = INamePolicy.class)
	public static final String PARAMETER_POLICY =
			"eD!+dk.itu.big_red.model.Control.parameter-policy";
	
	public static INamePolicy getParameterPolicy(Control c) {
		return getParameterPolicy(null, c);
	}
	
	public static INamePolicy getParameterPolicy(
			PropertyScratchpad context, Control c) {
		return require(context, c, PARAMETER_POLICY, INamePolicy.class);
	}
	
	public static void setParameterPolicy(Control c, INamePolicy n) {
		c.setExtendedData(PARAMETER_POLICY, n);
	}
	
	public static IChange changeParameterPolicy(Control c, INamePolicy n) {
		return c.changeExtendedData(PARAMETER_POLICY, n);
	}
	
	private static final ExtendedDataValidator parameterValidator =
			new ExtendedDataValidator() {
		@Override
		public void validate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException {
			if (!(c.getCreator() instanceof Node))
				throw new ChangeRejectedException(c,
						c.getCreator() + " is not a Node");
			Node n = (Node)c.getCreator();
				
			Control control = n.getControl();
			INamePolicy policy = getParameterPolicy(control);
			if (policy == null)
				throw new ChangeRejectedException(c,
						"The control " + control.getName() +
						" does not define a parameter");
			
			if (!(c.newValue instanceof String))
				throw new ChangeRejectedException(c,
						"Parameter values must be strings");
			
			String value = (String)c.newValue;
			if ((c.newValue = policy.normalise(value)) == null)
				throw new ChangeRejectedException(c,
						"\"" + value + "\" is not a valid value for the " +
						"parameter of " + control.getName());
		}
	};
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PARAMETER =
			"eD!+dk.itu.big_red.model.Node.parameter";
	
	public static String getParameter(Node n) {
		return getParameter(null, n);
	}
	
	public static String getParameter(
			PropertyScratchpad context, Node n) {
		INamePolicy p = getParameterPolicy(context, n.getControl());
		String s = require(context, n, PARAMETER, String.class),
				t = null;
		if (p != null) {
			t = p.normalise(s);
			if (t == null)
				t = p.get(0);
		}
		if (s != null ? !s.equals(t) : s != t)
			setParameter(context, n, t);
		return t;
	}
	
	public static void setParameter(Node n, String s) {
		setParameter(null, n, s);
	}
	
	public static void setParameter(
			PropertyScratchpad context, Node n, String s) {
		set(context, n, PARAMETER, s);
	}
	
	public static IChange changeParameter(Node n, String s) {
		return n.changeExtendedData(PARAMETER, s, parameterValidator);
	}
	
	public static IChangeDescriptor changeParameterDescriptor(
			Node.Identifier n, String s) {
		return new ModelObject.ChangeExtendedDataDescriptor(
				n, PARAMETER, s, parameterValidator, null);
	}
	
	@RedProperty(fired = Integer.class, retrieved = Integer.class)
	public static final String SEGMENT =
			"eD!+dk.itu.big_red.model.PortSpec.segment";
	
	public static int getSegment(PortSpec p) {
		return getSegment(null, p);
	}
	
	public static int getSegment(PropertyScratchpad context, PortSpec p) {
		Integer i = require(context, p, SEGMENT, Integer.class);
		if (i == null) {
			recalculatePosition(context, p);
			i = require(context, p, SEGMENT, Integer.class);
		}
		return i;
	}
	
	public static void setSegment(PortSpec p, int i) {
		setSegment(null, p, i);
	}
	
	public static void setSegment(
			PropertyScratchpad context, PortSpec p, int i) {
		set(context, p, SEGMENT, i);
	}
	
	public static IChange changeSegment(PortSpec p, int i) {
		return p.changeExtendedData(SEGMENT, i);
	}
	
	@RedProperty(fired = Double.class, retrieved = Double.class)
	public static final String DISTANCE =
			"eD!+dk.itu.big_red.model.PortSpec.distance";
	
	private static void recalculatePosition(
			PropertyScratchpad context, PortSpec p) {
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
			PropertyScratchpad context, PortSpec p) {
		Double d = require(context, p, DISTANCE, Double.class);
		if (d == null) {
			recalculatePosition(context, p);
			d = require(context, p, DISTANCE, Double.class);
		}
		return d;
	}
	
	public static void setDistance(PortSpec p, double d) {
		setDistance(null, p, d);
	}
	
	public static void setDistance(
			PropertyScratchpad context, PortSpec p, double d) {
		set(context, p, DISTANCE, d);
	}
	
	public static IChange changeDistance(PortSpec p, double d) {
		return p.changeExtendedData(DISTANCE, d);
	}
	
	@RedProperty(fired = Object.class, retrieved = Object.class)
	public static final String SHAPE =
			"eD!+dk.itu.big_red.model.Control.shape";
	
	public static Object getShape(Control c) {
		return getShape(null, c);
	}
	
	public static Object getShape(PropertyScratchpad context, Control c) {
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
			PropertyScratchpad context, Control c, Object s) {
		if (s instanceof PointList || s instanceof Ellipse)
			set(context, c, SHAPE, s);
	}
	
	public static IChange changeShape(Control c, Object s) {
		return c.changeExtendedData(SHAPE, s);
	}
	
	private static final ExtendedDataValidator labelValidator =
			new ExtendedDataValidator() {
		@Override
		public void validate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException {
			if (!(c.newValue instanceof String)) {
				throw new ChangeRejectedException(c, "Labels must be strings");
			} else if (((String)c.newValue).length() == 0) {
				throw new ChangeRejectedException(c,
						"Labels must not be empty");
			}
		}
	};
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String LABEL =
			"eD!+dk.itu.big_red.model.Control.label";
	
	public static String getLabel(Control c) {
		return getLabel(null, c);
	}
	
	public static String labelFor(String s) {
		return (s != null ? (s.length() > 0 ? s.substring(0, 1) : s) : "?");
	}
	
	public static String getLabel(PropertyScratchpad context, Control c) {
		String s = require(context, c, LABEL, String.class);
		if (s == null)
			setLabel(context, c, s = labelFor(c.getName(context)));
		return s;
	}
	
	public static void setLabel(Control c, String s) {
		setLabel(null, c, s);
	}
	
	public static void setLabel(
			PropertyScratchpad context, Control c, String s) {
		set(context, c, LABEL, s);
	}
	
	public static IChange changeLabel(Control c, String s) {
		return c.changeExtendedData(LABEL, s, labelValidator);
	}
	
	private static final ExtendedDataValidator aliasValidator =
			new ExtendedDataValidator() {
		@Override
		public void validate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException {
			if (c.newValue != null) {
				if (!(c.newValue instanceof String))
					throw new ChangeRejectedException(c,
							"Aliases must be strings");
				INamePolicy np = new BoundedIntegerNamePolicy(0);
				if (np.normalise((String)c.newValue) == null)
					throw new ChangeRejectedException(c,
							"\"" + c.newValue + "\" is not a valid alias" +
							" for " + c.getCreator());
			}
		}
	};
	
	public static final String ALIAS =
			"eD!+dk.itu.big_red.model.Site.alias";
	
	public static String getAlias(Site s) {
		return getAlias(null, s);
	}
	
	public static String getAlias(PropertyScratchpad context, Site s) {
		return require(context, s, ALIAS, String.class);
	}
	
	public static void setAlias(Site s, String a) {
		setAlias(null, s, a);
	}
	
	public static void setAlias(PropertyScratchpad context, Site s, String a) {
		set(context, s, ALIAS, a);
	}
	
	public static IChange changeAlias(Site s, String a) {
		return s.changeExtendedData(ALIAS, a, aliasValidator);
	}
	
	public static IChangeDescriptor changeAliasDescriptor(
			Site.Identifier s, String a) {
		return new Layoutable.ChangeExtendedDataDescriptor(
				s, ALIAS, a, aliasValidator, null);
	}
}
