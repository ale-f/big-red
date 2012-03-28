package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IPort;

public class PortSpec extends ModelObject implements IPort {
	public static final String PROPERTY_SEGMENT = "PortSpecSegment";
	public static final String PROPERTY_DISTANCE = "PortSpecDistance";
	
	private abstract class PortSpecChange extends ModelObjectChange {
		@Override
		public PortSpec getCreator() {
			return PortSpec.this;
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
	
	private String name = "";
	private int segment;
	private double distance;
	
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
	
	public PortSpec setName(String name) {
		this.name = name;
		return this;
	}
	
	public int getSegment() {
		return segment;
	}
	
	public void setSegment(int segment) {
		int oldSegment = this.segment;
		this.segment = segment;
		firePropertyChange(PROPERTY_SEGMENT, oldSegment, segment);
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		double oldDistance = this.distance;
		this.distance = distance;
		firePropertyChange(PROPERTY_DISTANCE, oldDistance, distance);
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
	
	public ChangeSegment changeSegment(int segment) {
		return new ChangeSegment(segment);
	}
	
	public ChangeDistance changeDistance(double distance) {
		return new ChangeDistance(distance);
	}
	
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_DISTANCE.equals(name)) {
			return getDistance();
		} else if (PROPERTY_SEGMENT.equals(name)) {
			return getSegment();
		} else return super.getProperty(name);
	}
}
