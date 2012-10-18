package it.uniud.bigredit.model;

import org.bigraph.model.Node;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

final class ReactionHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof Reaction.ChangeAddReactum) {

			Reaction.ChangeAddReactum c = (Reaction.ChangeAddReactum) b;
			((Reaction) c.getCreator()).changeReactum(c.child);
			
		} else if (b instanceof Reaction.ChangeAddRedex) {
			Reaction.ChangeAddRedex c = (Reaction.ChangeAddRedex) b;
			((Reaction) c.getCreator()).changeRedex(c.child);
		}else if(b instanceof Reaction.ChangeLayoutChild){
			Reaction.ChangeLayoutChild c = (Reaction.ChangeLayoutChild)b;
			((Reaction)c.getCreator())._changeLayoutChild(c.child, c.layout);
		} else if(b instanceof Reaction.ChangeInsideModel){
			Reaction.ChangeInsideModel c = (Reaction.ChangeInsideModel) b;
			((Reaction)c.getCreator())._changeInsideModel(c.target, c.change);
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process p, IChange b)
			throws ChangeRejectedException {
		//System.out.println("called _tryValidateChange BRSChangeValidator");
		if (b instanceof BRS.ChangeAddChild) {
			BRS.ChangeAddChild c = (BRS.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				throw new ChangeRejectedException(b,
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			

		} else return false;
		return true;
	}
}
