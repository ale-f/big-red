package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IPort;

/**
 * A {@link PortSpec} is an abstract {@link Port}: changes to it don't need to
 * undergo validation. {@link Control}s contain these rather than actual {@link
 * Port}s.
 * @author alec
 *
 */
public class PortSpec implements IPort {
	private String name;
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
}
