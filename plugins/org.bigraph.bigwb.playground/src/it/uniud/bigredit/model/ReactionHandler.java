package it.uniud.bigredit.model;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipantHost;

final class ReactionHandler
		implements IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public void setHost(IParticipantHost host) {
		/* do nothing */
	}
	
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor b) {
		if (b instanceof Reaction.ChangeAddReactum) {

			Reaction.ChangeAddReactum c = (Reaction.ChangeAddReactum) b;
			c.getCreator().changeReactum(c.child);
			
		} else if (b instanceof Reaction.ChangeAddRedex) {
			Reaction.ChangeAddRedex c = (Reaction.ChangeAddRedex) b;
			c.getCreator().changeRedex(c.child);
		}else if(b instanceof Reaction.ChangeLayoutChild){
			Reaction.ChangeLayoutChild c = (Reaction.ChangeLayoutChild)b;
			
			/* beforeApply() implementation follows */
			//oldName = child.getName();
			
			c.getCreator()._changeLayoutChild(c.child, c.layout);
		} else if(b instanceof Reaction.ChangeInsideModel){
			Reaction.ChangeInsideModel c = (Reaction.ChangeInsideModel) b;
			c.getCreator()._changeInsideModel(c.target, c.change);
		} else return false;
		return true;
	}
	
	@Override
	public boolean tryValidateChange(Process p, IChangeDescriptor b)
			throws ChangeCreationException {
		//System.out.println("called _tryValidateChange BRSChangeValidator");
		if (b instanceof Reaction.ChangeAddReactum) {
			if (((Reaction.ChangeAddReactum)b).child == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
		} else if (b instanceof Reaction.ChangeAddRedex) {
			if (((Reaction.ChangeAddRedex)b).child == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
		} else if (b instanceof Reaction.ChangeLayoutChild) {
			if (((Reaction.ChangeLayoutChild)b).child == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
		} else if (b instanceof Reaction.ChangeInsideModel) {
			/* do nothing */
		} else return false;
		return true;
	}
}
