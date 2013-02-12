package it.uniud.bigredit.model;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipantHost;

final class BRSHandler
		implements IDescriptorStepExecutor, IDescriptorStepValidator {
	@Override
	public void setHost(IParticipantHost host) {
		/* do nothing */
	}
	
	@Override
	public boolean executeChange(Resolver resolver, IChangeDescriptor b) {
		if (b instanceof BRS.ChangeAddChild) {
			BRS.ChangeAddChild c = (BRS.ChangeAddChild)b;
			c.getCreator().addChild(c.child);
			//c.child.setName(c.name);
			//getNamespace(getNSI(c.child)).put(c.name, c.child);
		} else if(b instanceof BRS.ChangeLayoutChild){
			BRS.ChangeLayoutChild c = (BRS.ChangeLayoutChild)b;
			
			/* beforeApply() implementation follows */
			//oldName = child.getName();
			
			c.getCreator()._changeLayoutChild(c.child, c.layout);
		} else if(b instanceof BRS.ChangeInsideModel){
			BRS.ChangeInsideModel c = (BRS.ChangeInsideModel) b;
			c.getCreator()._changeInsideModel(c.target, c.change);
		} else if(b instanceof BRS.ChangeRemoveChild){
			BRS.ChangeRemoveChild c = (BRS.ChangeRemoveChild) b;
			
			/* beforeApply() implementation follows */
			//oldName = child.getName();
			
			c.getCreator()._changeRemoveChild(c.child);
		}else return false;
		return true;
				
				
		 /*else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			c.link.addPoint(c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			c.link.removePoint(c.getCreator());
		 else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			c.getCreator().removeChild(c.child);
			//getNamespace(getNSI(c.child)).remove(c.child.getName());
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			c.getCreator().setLayout(c.newLayout);
			if (c.getCreator().getParent() instanceof Bigraph)
				((Bigraph)c.getCreator().getParent()).updateBoundaries();
		} else if (b instanceof Edge.ChangeReposition) {
			Edge.ChangeReposition c = (Edge.ChangeReposition)b;
			c.getCreator().averagePosition();
		} else if (b instanceof Colourable.ChangeOutlineColour) {
			Colourable.ChangeOutlineColour c = (Colourable.ChangeOutlineColour)b;
			//c.getCreator().setOutlineColour(c.newColour);
		} else if (b instanceof Colourable.ChangeFillColour) {
			Colourable.ChangeFillColour c = (Colourable.ChangeFillColour)b;
			//c.getCreator().setFillColour(c.newColour);
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			//getNamespace(getNSI(c.getCreator())).remove(c.getCreator().getName());
			c.getCreator().setName(c.newName);
			//getNamespace(getNSI(c.getCreator())).put(c.newName, c.getCreator());
		} else if (b instanceof ModelObject.ChangeComment) {
			ModelObject.ChangeComment c = (ModelObject.ChangeComment)b;
			c.getCreator().setComment(c.comment);
		} else if (b instanceof Site.ChangeAlias) {
			Site.ChangeAlias c = (Site.ChangeAlias)b;
			//c.getCreator().setAlias(c.alias);
		}*/
		
		
	}
	
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor b)
			throws ChangeCreationException {
		if (b instanceof BRS.ChangeLayoutChild) {
			if (((BRS.ChangeLayoutChild)b).child == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
		} else if (b instanceof BRS.ChangeRemoveChild) {
			if (((BRS.ChangeRemoveChild)b).child == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
		} else if (b instanceof BRS.ChangeAddChild) {
			if (((BRS.ChangeAddChild)b).child == null)
				throw new ChangeCreationException(b,
						"" + b + " is not ready");
		} else if (b instanceof BRS.ChangeInsideModel) {
			/* do nothing */
		} else return false;
		return true;
	}
}
