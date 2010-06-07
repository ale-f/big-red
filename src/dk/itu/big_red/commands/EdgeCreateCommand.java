package dk.itu.big_red.commands;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.ui.PlatformUI;

import dk.itu.big_red.model.*;
import dk.itu.big_red.part.AbstractPart;

public class EdgeCreateCommand extends Command {
	private Thing target, source;
	private String targetKey, sourceKey;
	private Edge edge;
	private Point icl;
	
	public EdgeCreateCommand() {
		super();
		target = source = null;
		targetKey = sourceKey = null;
		edge = null;
	}
	
	public void setObject(Object s) {
		if (s instanceof Edge)
			this.edge = (Edge)s;
	}

	public void setTarget(Object e) {
		if (e instanceof Name || e instanceof Node)
			this.target = (Thing)e;
	}
	
	public void setSource(Object e) {
		if (e instanceof Name || e instanceof Node)
			this.source = (Thing)e;
	}
	
	public static Point getTotalOffset(Thing n) {
		Rectangle constraint;
		Point offset = new Point();
		Thing generation = n;
		
		while (!(generation instanceof Bigraph)) {
			constraint = generation.getLayout();
			offset.x += constraint.x; offset.y += constraint.y;
			generation = generation.getParent();
		}
		
		return offset;
	}
	
	public void setInitialClickLocation(Point icl) {
		/* FIXME - zooming breaks this */
		this.icl = getTotalOffset(source);
		this.icl.x = icl.x - this.icl.x; this.icl.y = icl.y - this.icl.y;
	}
	
	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}
	
	public boolean compatiblePorts() {
		if (source instanceof Name || target instanceof Name) return true;
			else if (targetKey != null && sourceKey != null) return true;
		Node sn = (Node)source;
		Node tn = (Node)target;
		targetKey = sourceKey = null;
		for (String s : sn.getControl().getPorts()) {
			for (String t : tn.getControl().getPorts()) {
				if (source.getSignature().canConnect(s, t)) {
					sourceKey = s;
					targetKey = t;
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canExecute() {
		return (target != null && source != null && edge != null && compatiblePorts());
	}
	
	public void execute() {
		if (canExecute()) {
			if (source instanceof Node && target instanceof Node)
				((Node)source).connect(sourceKey, (Node)target, targetKey, edge);
			else {
				if (source instanceof Node && sourceKey == null && icl != null) {
					Node ns = (Node)source;
					String bestPort = null;
					double bestDistance = Double.MAX_VALUE;
					for (String s : ns.getControl().getPorts()) {
						Point p = ns.getPortAnchorPosition(s);
						if (p.getDistance(this.icl) < bestDistance) {
							bestPort = s;
							bestDistance = p.getDistance(this.icl);
						}
					}
					sourceKey = bestPort;
				}
				if (target instanceof Node && targetKey == null) {
					/* FIXME - no way to detect final location? */
					Node nt = (Node)target;
					String bestPort = null;
					for (String s : nt.getControl().getPorts()) {
						bestPort = s;
						break;
					}
					targetKey = bestPort;
				}
				edge.setSource(source, sourceKey);
				edge.setTarget(target, targetKey);
				source.addEdge(edge); target.addEdge(edge);
			}
		}
	}
	
	public boolean canUndo() {
		return (target != null && source != null && edge != null &&
				target.edgeIncident(edge) && source.edgeIncident(edge));
	}
	
	public void undo() {
		source.removeEdge(edge);
		target.removeEdge(edge);
		edge.setSource(null, null); edge.setTarget(null, null);
	}
}
