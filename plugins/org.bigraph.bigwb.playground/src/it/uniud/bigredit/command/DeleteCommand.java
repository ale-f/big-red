package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;

public class DeleteCommand extends ChangeCommand {

	
	private ModelObject target;
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	private ModelObject del;
	
	
	public DeleteCommand() {
		setChange(cg);
	}
	
	@Override
	public void prepare() {
		cg.clear();
		
		cg.add(((BRS)target).changeRemoveChild(del));
	}
	
	public void setTarget(BRS target){
		this.target=target;
	}
	
	public void setSon(ModelObject deleteNode){
		del=deleteNode;
	}

}
