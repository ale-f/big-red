package it.uniud.bigredit.policy;

import it.uniud.bigredit.command.DeleteCommand;
import it.uniud.bigredit.model.BRS;

import java.util.List;

import org.bigraph.model.ModelObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.CombinedCommandFactory;

public class LayoutableDeletePolicy extends ComponentEditPolicy {
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		List<EditPart> parts= deleteRequest.getEditParts();
		DeleteCommand command= new DeleteCommand();
		for(EditPart part: parts){
			
			command.setTarget((BRS)part.getParent().getModel());
			command.setSon((ModelObject)part.getModel());
		}
		return command.prepare();
	}
}
