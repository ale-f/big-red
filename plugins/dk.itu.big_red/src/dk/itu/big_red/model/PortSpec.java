package dk.itu.big_red.model;

import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IPort;

public class PortSpec extends ModelObject implements IPort {
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "PortSpecName";
	
	@RedProperty(fired = Control.class, retrieved = Control.class)
	public static final String PROPERTY_CONTROL = "PortSpecControl";
	
	@RedProperty(fired = Integer.class, retrieved = Integer.class)
	public static final String PROPERTY_SEGMENT = "PortSpecSegment";
	
	@RedProperty(fired = Double.class, retrieved = Double.class)
	public static final String PROPERTY_DISTANCE = "PortSpecDistance";
	
	private abstract class PortSpecChange extends ModelObjectChange {
		@Override
		public PortSpec getCreator() {
			return PortSpec.this;
		}
	}
	
	public class ChangeName extends PortSpecChange {
		public String name;
		public ChangeName(String name) {
			this.name = name;
		}
		
		private String oldName;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
		}
		
		@Override
		public boolean isReady() {
			return (name != null);
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public Change inverse() {
			return new ChangeName(oldName);
		}
	}
	
	public class ChangeSegment extends PortSpecChange {
		public int segment;
		
		public ChangeSegment(int segment) {
			this.segment = segment;
		}
		
		private int oldSegment;
		@Override
		public void beforeApply() {
			oldSegment = getCreator().getSegment();
		}
		
		@Override
		public ChangeSegment inverse() {
			return new ChangeSegment(oldSegment);
		}
	}
	
	public class ChangeDistance extends PortSpecChange {
		public double distance;
		
		public ChangeDistance(double distance) {
			this.distance = distance;
		}
		
		private double oldDistance;
		@Override
		public void beforeApply() {
			oldDistance = getCreator().getDistance();
		}
		
		@Override
		public ChangeDistance inverse() {
			return new ChangeDistance(oldDistance);
		}
	}
	
	private String name;
	private int segment;
	private double distance;
	private Control control;
	
	public PortSpec() {
		
	}
	
	public PortSpec(String name, int segment, double distance) {
		setName(name);
		setSegment(segment);
		setDistance(distance);
	}

	public PortSpec(PortSpec p) {
		setName(p.getName());
		setSegment(p.getSegment());
		setDistance(p.getDistance());
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getName(IPropertyProviderProxy context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	public int getSegment() {
		return segment;
	}
	
	public int getSegment(IPropertyProviderProxy context) {
		return (Integer)getProperty(context, PROPERTY_SEGMENT);
	}
	
	protected void setSegment(int segment) {
		int oldSegment = this.segment;
		this.segment = segment;
		firePropertyChange(PROPERTY_SEGMENT, oldSegment, segment);
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getDistance(IPropertyProviderProxy context) {
		return (Double)getProperty(context, PROPERTY_DISTANCE);
	}
	
	protected void setDistance(double distance) {
		double oldDistance = this.distance;
		this.distance = distance;
		firePropertyChange(PROPERTY_DISTANCE, oldDistance, distance);
	}

	public Control getControl() {
		return control;
	}
	
	public Control getControl(IPropertyProviderProxy context) {
		return (Control)getProperty(context, PROPERTY_CONTROL);
	}
	
	protected void setControl(Control control) {
		Control oldControl = this.control;
		this.control = control;
		firePropertyChange(PROPERTY_CONTROL, oldControl, control);
	}
	
	@Override
	public ILink getLink() {
		return null;
	}

	@Override
	public INode getNode() {
		return null;
	}
	
	@Override
	public String toString() {
		return "PortSpec(" + getName() + ", " + getSegment() + ", " +
				getDistance() + ")";
	}
	
	public ChangeName changeName(String name) {
		return new ChangeName(name);
	}
	
	public ChangeSegment changeSegment(int segment) {
		return new ChangeSegment(segment);
	}
	
	public ChangeDistance changeDistance(double distance) {
		return new ChangeDistance(distance);
	}
	
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else if (PROPERTY_DISTANCE.equals(name)) {
			return getDistance();
		} else if (PROPERTY_SEGMENT.equals(name)) {
			return getSegment();
		} else if (PROPERTY_CONTROL.equals(name)) {
			return getControl();
		} else return super.getProperty(name);
	}
}
