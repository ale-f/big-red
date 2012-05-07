package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import dk.itu.big_red.editors.bigraph.parts.EdgePart;
import dk.itu.big_red.editors.bigraph.parts.PortPart;

public class RedResizableEditPolicy extends ResizableEditPolicy {
	@Override
	public void setHost(EditPart host) {
		super.setHost(host);
		if (host instanceof EdgePart || host instanceof PortPart)
			setResizeDirections(0);
		if (host instanceof PortPart)
			setDragAllowed(false);
	}
}
