package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.ModelDeleteCommand;
import dk.itu.big_red.editors.bigraph.parts.AbstractPart;
import dk.itu.big_red.editors.bigraph.parts.LinkConnectionPart;

public class CombinedCommandFactory {
	@SuppressWarnings("unchecked")
	public static Command createDeleteCommand(GroupRequest r) {
		if (r.getExtendedData().containsKey(ModelDeleteCommand.GROUP_MAP_ID))
			return null;
		ModelDeleteCommand mdc = new ModelDeleteCommand();
		for (Object i_ : r.getEditParts()) {
			if (i_ instanceof AbstractPart) {
				AbstractPart i = (AbstractPart)i_;
				mdc.addObject(i.getModel());
			} else if (i_ instanceof LinkConnectionPart) {
				LinkConnectionPart i = (LinkConnectionPart)i_;
				mdc.addObject(i.getModel());
			}
		}
		mdc.prepare();
		r.getExtendedData().put(ModelDeleteCommand.GROUP_MAP_ID, "");
		return mdc;
	}
}
