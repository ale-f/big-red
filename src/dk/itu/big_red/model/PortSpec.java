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

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getSegment() {
		return segment;
	}
	
	public void setSegment(int segment) {
		this.segment = segment;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public ILink getILink() {
		return null;
	}

	@Override
	public INode getINode() {
		return null;
	}
}
