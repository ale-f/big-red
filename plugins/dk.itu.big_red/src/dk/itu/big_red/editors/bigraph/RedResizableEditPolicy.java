package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;

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
	
	private static final ChangeBoundsRequest cbr(
			ChangeBoundsRequest request, Object nt) {
		ChangeBoundsRequest cbr = new ChangeBoundsRequest(nt);
		cbr.setLocation(request.getLocation());
		cbr.setEditParts(request.getEditParts());
		cbr.setMoveDelta(request.getMoveDelta());
		cbr.setSizeDelta(request.getSizeDelta());
		cbr.setExtendedData(request.getExtendedData());
		return cbr;
	}
	
	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		return getHost().getParent().getCommand(
				cbr(request, REQ_MOVE_CHILDREN));
	}
	
	@Override
	protected Command getResizeCommand(ChangeBoundsRequest request) {
		return getHost().getParent().getCommand(
				cbr(request, REQ_RESIZE_CHILDREN));
	}
}
