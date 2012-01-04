package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

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
		setLongName("Unknown");
		setLabel("?");
		setShape(Control.Shape.POLYGON, POINTS_QUAD);
		setDefaultSize(new Dimension(50, 50));
		setKind(Kind.ACTIVE);
		setResizable(true);
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
	
	/**
	 * Sets this Control's {@link Shape}. <code>points</code> must <i>not</i>
	 * be <code>null</code> if <code>shape</code> is {@link
	 * Shape#POLYGON}, but it <i>must</i> be <code>null</code> otherwise.
	 * @param shape the new Shape
	 * @param points a {@link PointList} specifying a polygon
	 */
	public void setShape(Control.Shape shape, PointList points) {
		if ((points == null && shape == Shape.POLYGON) ||
			(points != null && shape == Shape.OVAL))
			return;
		Control.Shape oldShape = this.shape;
		this.shape = shape;
		firePropertyChange(PROPERTY_SHAPE, oldShape, shape);
		
		PointList oldPoints = this.points;
		this.points = points;
		firePropertyChange(PROPERTY_POINTS, oldPoints, points);
	}

	public void setLongName(String longName) {
		if (longName != null) {
			name = longName;
			if (longName.length() > 1)
				setLabel(longName.substring(0, 1).toUpperCase());
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

	public void clearPorts() {
		ports.clear();
		firePropertyChange(PROPERTY_PORT, null, null);
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
	
	public List<PortSpec> getPorts() {
		return ports;
	}
	
	/**
	 * Produces a <i>new</i> array of {@link Port}s to give to a {@link Node}.
	 * @return an array of Ports
	 */
	public ArrayList<Port> getPortsArray() {
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
	
	protected abstract static class ParameterSpec {
		protected abstract class Parameter {
			protected abstract ParameterSpec getSpec();
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
	
	protected static class LongParameterSpec extends ParameterSpec {
		public class LongParameter extends Parameter {
			protected LongParameter() {
			}
			
			@Override
			protected LongParameterSpec getSpec() {
				return LongParameterSpec.this;
			}
			
			protected long value;
			
			public long getValue() {
				return value;
			}
		
			public LongParameter setValue(long value) {
				if (getSpec().validate(value))
					this.value = value;
				return this;
			}
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
	
	public ArrayList<Parameter> getParametersArray() {
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		for (ParameterSpec spec : getParameters())
			params.add(spec.instantiate());
		return params;
	}
	
	@Override
	public Iterable<PortSpec> getIPorts() {
		return ports;
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