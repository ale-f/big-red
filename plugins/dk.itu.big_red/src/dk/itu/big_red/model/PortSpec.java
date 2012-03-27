package dk.itu.big_red.model;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IPort;

public class PortSpec extends ModelObject implements IPort {
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
		public Change inverse() {
			return getCreator().changeSegment(oldSegment);
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
		public Change inverse() {
			return getCreator().changeDistance(oldDistance);
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
		setName(p.getName()).setSegment(p.getSegment()).
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
	
	public PortSpec setSegment(int segment) {
		this.segment = segment;
		return this;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public PortSpec setDistance(double distance) {
		this.distance = distance;
		return this;
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
}
