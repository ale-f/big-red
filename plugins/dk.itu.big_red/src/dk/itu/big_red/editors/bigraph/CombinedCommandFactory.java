package dk.itu.big_red.editors.bigraph;

import org.bigraph.model.Bigraph;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableMoveCommand;
import dk.itu.big_red.editors.bigraph.commands.ModelDeleteCommand;
import dk.itu.big_red.editors.bigraph.parts.AbstractPart;
import dk.itu.big_red.editors.bigraph.parts.LinkConnectionPart;

/**
 * {@link Bigraph}s can validate
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
 */
public class CombinedCommandFactory {
	private static final Object TAG = new Object();
	
	@SuppressWarnings("unchecked")
	private static final boolean tryTag(Request r) {
		if (!isTagged(r)) {
			r.getExtendedData().put(TAG, TAG);
			return true;
		} else return false;
	}
	
	public static boolean isTagged(Request r) {
		return (r.getExtendedData().get(TAG) == TAG);
	}
	
	/**
	 * Creates the single {@link Command} for this {@link GroupRequest}.
	 * @param r a {@link GroupRequest}
	 * @return the {@link Command}, or <code>null</code> if it's already been
	 * created by another {@link EditPolicy}
	 */
	public static Command createDeleteCommand(GroupRequest r) {
		if (!tryTag(r))
			return null;
		ModelDeleteCommand mdc = new ModelDeleteCommand();
		for (Object i : r.getEditParts())
			if (i instanceof AbstractPart || i instanceof LinkConnectionPart)
				mdc.addObject(((EditPart)i).getModel());
		mdc.prepare();
		return mdc;
	}
	
	public static Command createMoveCommand(ChangeBoundsRequest r) {
		if (!tryTag(r))
			return null;
		LayoutableMoveCommand lmc = new LayoutableMoveCommand();
		lmc.setMoveDelta(r.getMoveDelta());
		lmc.setSizeDelta(r.getSizeDelta());
		for (Object i : r.getEditParts())
			if (i instanceof AbstractPart || i instanceof LinkConnectionPart)
				lmc.addObject(((EditPart)i).getModel());
		lmc.prepare();
		return lmc;
	}
}
