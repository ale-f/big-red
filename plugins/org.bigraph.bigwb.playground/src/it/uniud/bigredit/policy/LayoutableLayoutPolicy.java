package it.uniud.bigredit.policy;

import it.uniud.bigredit.editparts.BRSPart;
import it.uniud.bigredit.editparts.NestedBigraphPart;
import it.uniud.bigredit.editparts.ReactionPart;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import it.uniud.bigredit.command.CompositionCommand;
import it.uniud.bigredit.command.LayoutableAddCommand;
import it.uniud.bigredit.command.LayoutableCreateCommand;
import it.uniud.bigredit.command.LayoutableRelayoutCommand;
import it.uniud.bigredit.command.ReactionCommand;
import it.uniud.bigredit.model.Reaction;

import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.model.LayoutUtilities;

/**
 * This class is an elaboration of the original LayoutableLayoutPolicy in BigRed
 * this class manages also BRSPart and NestedBigraphPart
 *
 * @author Carlo
 *
 */
public class LayoutableLayoutPolicy extends XYLayoutEditPolicy {
	
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		
		LayoutableRelayoutCommand command = null;
		if (!(child instanceof BRSPart)) {
			if (child instanceof NestedBigraphPart) {
				command = new LayoutableRelayoutCommand();
				command.setModel(child.getModel());
				command.setParent((ModelObject)child.getParent().getModel());
				command.setConstraint(constraint);
				command.prepare();
			} else if (child instanceof ReactionPart) {
				command = new LayoutableRelayoutCommand();
				command.setModel(child.getModel());
				command.setParent((ModelObject)child.getParent().getModel());
				command.setConstraint(constraint);
				command.prepare();
			}else{
				command = new LayoutableRelayoutCommand();
				command.setModel(child.getModel());
				command.setConstraint(constraint);
				command.prepare();
			}
		}
		return command;
	}
	
	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		LayoutableAddCommand command= null;
		if ( child instanceof NestedBigraphPart ) {
			Bigraph target = ( Bigraph )getHost().getModel();
			if ( target.getBigraph() != null ) {
				
				return new CompositionCommand( ( Bigraph )child.getModel(), target.getBigraph());
						                 // ( ( BRSPart )child.getRoot() ).getWorkbenchPart() );
			}
		}else if(child instanceof ReactionPart){
			/** TODO FILL with matching / composition */
			ModelObject target = ( ModelObject )getHost().getModel();
			if ( target != null ) {
				return new ReactionCommand( ( Reaction )child.getModel(), (Bigraph)target
						            );
			}
			
			
		}else{
			command = new LayoutableAddCommand();
			command.setParent(getHost().getModel());
			command.setChild(child.getModel());
			command.setConstraint(constraint);
			command.prepare();
			
		}
		return command;
	}
	
	/*@Override
	protected Command getOrphanChildrenCommand(Request request) {
		System.out.println("get OrphanChildren Command");
		LayoutableOrphanCommand command = null;
		if (request.getType() == REQ_ORPHAN_CHILDREN && request instanceof GroupRequest) {
		
			GroupRequest groupRequest = (GroupRequest)request;
			command = new LayoutableOrphanCommand();
			
			Object model =
				((EditPart)groupRequest.getEditParts().get(0)).getModel();
			if (model instanceof Layoutable)
				command.setParent(((Layoutable)model).getParent());
			
			command.setChildren(groupRequest.getEditParts());
			command.prepare();
		}
		return command;
	}*/
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		
		Object requestObject = request.getNewObject();
		
		requestObject.getClass();
		Dimension size = new Dimension(100, 100);
		ModelObject parent;
		
		if(getHost() instanceof NestedBigraphPart){
			parent= (ModelObject)((Bigraph)getHost().getModel());
		}else if(getHost() instanceof NestedBigraphPart){
			parent= (ModelObject)((Reaction)getHost().getModel());
		}else if (getHost() instanceof BigraphPart){
			parent = (ModelObject)getHost().getModel();
		}else if (getHost() instanceof BRSPart){
			parent = (ModelObject)getHost().getModel();
		}else{
			parent = (ModelObject)getHost().getModel();
			
		}
		
		if (parent instanceof Container && parent!= null && requestObject!=null) {
				if(LayoutUtilities.getLayout(((Layoutable) requestObject))!=null){
					size.setSize(LayoutUtilities.getLayout(((Layoutable) requestObject)).getSize());
				}
		}
		
		
		LayoutableCreateCommand cmd = new LayoutableCreateCommand();
		cmd.setContainer(parent);
		cmd.setObject(request.getNewObject());
		
		Rectangle constraint = (Rectangle)getConstraintFor(request);
		constraint.x = (constraint.x < 0 ? 0 : constraint.x);
		constraint.y = (constraint.y < 0 ? 0 : constraint.y);
		constraint.width = (constraint.width <= 0 ? size.width : constraint.width);
		constraint.height = (constraint.height <= 0 ? size.height : constraint.height);
		cmd.setLayout(constraint);
		cmd.prepare();
		
		return cmd;
	}

}
