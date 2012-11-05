package it.uniud.bigredit.model;

import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;

final class ReactionHandler implements IStepExecutor, IStepValidator {
	@Override
	public boolean executeChange(IChange b) {
		if (b instanceof Reaction.ChangeAddReactum) {

			Reaction.ChangeAddReactum c = (Reaction.ChangeAddReactum) b;
			c.getCreator().changeReactum(c.child);
			
		} else if (b instanceof Reaction.ChangeAddRedex) {
			Reaction.ChangeAddRedex c = (Reaction.ChangeAddRedex) b;
			c.getCreator().changeRedex(c.child);
		}else if(b instanceof Reaction.ChangeLayoutChild){
			Reaction.ChangeLayoutChild c = (Reaction.ChangeLayoutChild)b;
			c.getCreator()._changeLayoutChild(c.child, c.layout);
		} else if(b instanceof Reaction.ChangeInsideModel){
			Reaction.ChangeInsideModel c = (Reaction.ChangeInsideModel) b;
			c.getCreator()._changeInsideModel(c.target, c.change);
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process p, IChange b)
			throws ChangeRejectedException {
		//System.out.println("called _tryValidateChange BRSChangeValidator");
		if (b instanceof Reaction.ChangeAddReactum ||
				b instanceof Reaction.ChangeAddRedex ||
				b instanceof Reaction.ChangeLayoutChild ||
				b instanceof Reaction.ChangeInsideModel) {
			/* do nothing */
		} else return false;
		return true;
	}
}
