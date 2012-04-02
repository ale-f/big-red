package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.commands.ModelDeleteCommand;
import dk.itu.big_red.editors.bigraph.parts.AbstractPart;
import dk.itu.big_red.editors.bigraph.parts.LinkConnectionPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.changes.IChangeExecutor;

/**
 * {@link Bigraph}s are instances of {@link IChangeExecutor}: they can validate
 * changes being applied to them to make sure that the model remains
 * consistent. This doesn't work well with the GEF's normal way of handling
 * commands, though! A {@link GroupRequest} is normally satisfied by
 * generating several commands, in no particular order: each of these might
 * by itself pass validation, but the combination of them is likely to break
 * the model (two {@link Command}s might both try to delete the same object;
 * undoing them would mean that the same child is added twice, which isn't
 * allowed).
 * <p>This class, the <strong>CombinedCommandFactory</strong>, solves this
 * problem: when EditPolicies defer to the static methods of this class, at
 * most one {@link ChangeCommand} will be generated for each {@link
 * GroupRequest}, so the validation works properly and the model is safe.
 * @author alec
 *
 */
public class CombinedCommandFactory {
	/**
	 * Creates the single {@link Command} for this {@link GroupRequest}.
	 * @param r a {@link GroupRequest}
	 * @return the {@link Command}, or <code>null</code> if it's already been
	 * created by another {@link EditPolicy}
	 */
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
