package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.model.Control.ParameterSpec.Parameter;
import dk.itu.big_red.model.interfaces.IControl;

/**
 * A Control is the bigraphical analogue of a <i>class</i> - a template from
 * which instances ({@link Node}s) should be constructed. Controls are
 * registered with a {@link Bigraph} as part of its {@link Signature}.
 * 
 * <p>In the formal bigraph model, controls define labels and numbered ports;
 * this model differs slightly by defining <i>named</i> ports and certain
 * graphical properties (chiefly shapes and default port offsets).
 * @author alec
 * @see IControl
 */
public class Control extends Colourable implements IControl {
	public static enum Shape {
		/**
		 * An oval.
		 */
		OVAL,
		/**
		 * A polygon.
		 */
		POLYGON
	}

	public static enum Kind {
		ATOMIC {
			@Override
			public String toString() {
				return "atomic";
			}
		},
		ACTIVE {
			@Override
			public String toString() {
				return "active";
			}
		},
		PASSIVE {
			@Override
			public String toString() {
				return "passive";
			}
		};
	}
	
	/**
	 * The property name fired when the label (the one- or two-character
	 * caption that appears next to {@link Node}s on the bigraph) changes.
	 * The property values are {@link String}s.
	 */
	public static final String PROPERTY_LABEL = "ControlLabel";
	
	/**
	 * The property name fired when the name changes. The property values are
	 * {@link String}s.
	 */
	public static final String PROPERTY_NAME = "ControlName";
	
	/**
	 * The property name fired when the shape changes. The property values are
	 * {@link Control.Shape}s.
	 */
	public static final String PROPERTY_SHAPE = "ControlShape";
	
	/**
	 * The property name fired when the set of points defining this control's
	 * polygon changes. The property values are {@link PointList}s.
	 */
	public static final String PROPERTY_POINTS = "ControlPoints";
	
	/**
	 * The property name fired when the default size changes. (This only
	 * really matters for existing {@link Node}s if they aren't resizable.)
	 * The property values are {@link Dimension}s.
	 */
	public static final String PROPERTY_DEFAULT_SIZE = "ControlDefaultSize";
	
	/**
	 * The property name fired when the resizability changes. If this changes
	 * from <code>true</code> to <code>false</code>, listeners should make sure
	 * that any {@link Node}s with this Control are resized to the default
	 * size. The property values are {@link Boolean}s.
	 * @see Control#getDefaultSize
	 */
	public static final String PROPERTY_RESIZABLE = "ControlResizable";
	
	/**
	 * The property name fired when the set of ports changes. If this changes
	 * from <code>null</code> to a non-null value, then a port has been added;
	 * if it changes from a non-null value to <code>null</code>, one has been
	 * removed. The property values are {@link PortSpec}s.
	 */
	public static final String PROPERTY_PORT = "ControlPort";
	
	/**
	 * The property name fired when the kind changes. The property values are
	 * {@link Kind}s.
	 */
	public static final String PROPERTY_KIND = "ControlKind";
	
	public static final PointList POINTS_QUAD = new PointList(new int[] {
			0, 0,
			0, 40,
			-40, 40,
			-40, 0
	});
	
	private ArrayList<PortSpec> ports = new ArrayList<PortSpec>();
	private PointList points = new PointList();
	
	private Control.Shape shape;
	private String name;
	private String label;
	private Dimension defaultSize;
	private boolean resizable;
	private Control.Kind kind;
	
	public Control() {
		setName("Unknown");
		setLabel("?");
		setShape(Control.Shape.POLYGON);
		setPoints(POINTS_QUAD);
		setDefaultSize(new Dimension(50, 50));
		setKind(Kind.ACTIVE);
		setResizable(true);
	}
	
	@Override
	public Control clone(Map<ModelObject,ModelObject> m) {
		Control c = (Control)super.clone(m);
		
		c.setName(getName());
		c.setLabel(getLabel());
		c.setShape(getShape());
		if (getShape() == Shape.POLYGON)
			c.setPoints(getPoints().getCopy());
		c.setDefaultSize(getDefaultSize().getCopy());
		c.setKind(getKind());
		c.setResizable(isResizable());
		
		for (PortSpec p : getPorts())
			c.addPort(new PortSpec(p));
		
		return c;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		String oldLabel = this.label;
		this.label = label;
		firePropertyChange(PROPERTY_LABEL, oldLabel, label);
	}
	
	public Control.Shape getShape() {
		return shape;
	}
	
	public void setShape(Control.Shape shape) {
		Control.Shape oldShape = this.shape;
		this.shape = shape;
		firePropertyChange(PROPERTY_SHAPE, oldShape, shape);
	}
	
	/**
	 * If this object's shape is {@link Shape#POLYGON}, then gets a copy
	 * of the list of points defining its polygon.
	 * @return a list of points defining a polygon, or <code>null</code> if
	 *         this object's shape is not {@link Shape#POLYGON}
	 * @see Control#getShape
	 * @see Control#setShape
	 */
	public PointList getPoints() {
		if (shape == Shape.POLYGON)
			return points.getCopy();
		else return null;
	}
	
	public void setPoints(PointList points) {
		PointList oldPoints = this.points;
		this.points = points;
		if (shape == Shape.POLYGON)
			firePropertyChange(PROPERTY_POINTS, oldPoints, points);
	}

	public void setName(String name) {
		if (name != null) {
			String oldName = this.name;
			this.name = name;
			firePropertyChange(PROPERTY_NAME, oldName, name);
			if (name.length() > 1)
				setLabel(name.substring(0, 1).toUpperCase());
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	public Dimension getDefaultSize() {
		return defaultSize;
	}
	
	public void setDefaultSize(Dimension defaultSize) {
		if (defaultSize != null) {
			Dimension oldSize = this.defaultSize;
			this.defaultSize = defaultSize;
			firePropertyChange(PROPERTY_DEFAULT_SIZE, oldSize, defaultSize);
		}
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public void setKind(Kind kind) {
		Kind oldKind = this.kind;
		this.kind = kind;
		firePropertyChange(PROPERTY_KIND, oldKind, kind);
	}
	
	public boolean isResizable() {
		return resizable;
	}
	
	public void setResizable(Boolean resizable) {
		Boolean oldResizable = this.resizable;
		this.resizable = resizable;
		firePropertyChange(PROPERTY_RESIZABLE, oldResizable, resizable);
	}
	
	public void addPort(PortSpec p) {
		if (p != null) {
			PortSpec q = new PortSpec(p.getName(), p.getSegment(), p.getDistance());
			ports.add(q);
			firePropertyChange(PROPERTY_PORT, null, q);
		}
	}
	
	public void removePort(String port) {
		PortSpec p = getPort(port);
		if (p != null) {
			ports.remove(p);
			firePropertyChange(PROPERTY_PORT, p, null);
		}
	}
	
	public boolean hasPort(String port) {
		return (getPort(port) != null);
	}
	
	/**
	 * Gets the {@link List} of this {@link Control}'s {@link PortSpec}s.
	 * @return a reference to the internal list; modify it and <s>vampire
	 * bats</s> undocumented behaviour will steal your blood while you sleep
	 * @see #createPorts()
	 */
	public List<PortSpec> getPorts() {
		return ports;
	}
	
	/**
	 * Produces a <i>new</i> array of {@link Port}s to give to a {@link Node}.
	 * @return an array of Ports
	 * @see #getPorts()
	 */
	public ArrayList<Port> createPorts() {
		ArrayList<Port> r = new ArrayList<Port>();
		for (PortSpec i : ports)
			r.add(new Port(i.getName(), i.getSegment(), i.getDistance()));
		return r;
	}
	
	public PortSpec getPort(String name) {
		for (PortSpec i : ports)
			if (i.getName().equals(name))
				return i;
		return null;
	}
	
	private ArrayList<ParameterSpec> parameters;
	
	public abstract static class ParameterSpec {
		public abstract class Parameter {
			protected abstract ParameterSpec getSpec();
			
			public abstract Object getValue();
		}
		
		protected ParameterSpec(String name) {
			setName(name);
		}
		
		private String name;
		
		public ParameterSpec setName(String name) {
			this.name = name;
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		/**
		 * Indicates whether or not a {@link Parameter} instantiated from this
		 * {@link ParameterSpec} could have the given value.
		 * @param value an {@link Object}
		 * @return <code>true</code> if <code>value</code> represents a
		 * permissible value, or <code>false</code> otherwise
		 */
		public abstract boolean validate(Object value);
		
		/**
		 * Instantiates a {@link Parameter} governed by this {@link
		 * ParameterSpec}.
		 * @return a new {@link Parameter}
		 * @see #validate(Object)
		 */
		public abstract Parameter instantiate();
	}
	
	public static class LongParameterSpec extends ParameterSpec {
		public class LongParameter extends Parameter {
			protected LongParameter() {
			}
			
			@Override
			protected LongParameterSpec getSpec() {
				return LongParameterSpec.this;
			}
			
			protected long value;
			
			@Override
			public Long getValue() {
				return value;
			}
		
			public LongParameter setValue(Long value) {
				if (getSpec().validate(value))
					this.value = value;
				return this;
			}
		}
		
		protected LongParameterSpec(String name) {
			super(name);
		}
		
		private long minimum = Long.MIN_VALUE, maximum = Long.MAX_VALUE;
		
		public ParameterSpec setMinimum(long minimum) {
			this.minimum = minimum;
			return this;
		}
		
		public long getMinimum() {
			return minimum;
		}
		
		public ParameterSpec setMaximum(long maximum) {
			this.maximum = maximum;
			return this;
		}
		
		public long getMaximum() {
			return maximum;
		}
		
		@Override
		public boolean validate(Object value) {
			if (value instanceof Long) {
				Long l = (Long)value;
				return (l >= minimum && l <= maximum);
			} else return false;
		}
		
		@Override
		public Parameter instantiate() {
			return new LongParameter();
		}
	}
	
	public List<ParameterSpec> getParameters() {
		if (parameters == null)
			parameters = new ArrayList<ParameterSpec>();
		return parameters;
	}
	
	public ArrayList<Parameter> createParameters() {
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		for (ParameterSpec spec : getParameters())
			params.add(spec.instantiate());
		return params;
	}
	
	@Override
	public Iterable<PortSpec> getIPorts() {
		return getPorts();
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Control}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_PORT} will return a {@link List}&lt;{@link
	 * PortSpec}&gt;, <strong>not</strong> a {@link PortSpec}.
	 * </ul>
	 */
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_DEFAULT_SIZE)) {
			return getDefaultSize();
		} else if (name.equals(PROPERTY_LABEL)) {
			return getLabel();
		} else if (name.equals(PROPERTY_NAME)) {
			return getName();
		} else if (name.equals(PROPERTY_POINTS)) {
			return getPoints();
		} else if (name.equals(PROPERTY_PORT)) {
			return getPorts();
		} else if (name.equals(PROPERTY_RESIZABLE)) {
			return isResizable();
		} else if (name.equals(PROPERTY_SHAPE)) {
			return getShape();
		} else if (name.equals(PROPERTY_KIND)) {
			return getKind();
		} else return super.getProperty(name);
	}
	
	@Override
	public UserControl getUserControl() {
		return UserControl.OUTLINE_AND_FILL;
	}
}