package it.uniud.bigredit.policy;

import it.uniud.bigredit.editparts.BRSPart;
import it.uniud.bigredit.editparts.NestedBigraphPart;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import it.uniud.bigredit.command.CompositionCommand;
import it.uniud.bigredit.command.LayoutableAddCommand;
import it.uniud.bigredit.command.LayoutableCreateCommand;
import it.uniud.bigredit.editparts.BigreditRootEditPart;

import dk.itu.big_red.editors.bigraph.commands.LayoutableRelayoutCommand;
import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;

public class LayoutableLayoutPolicy extends XYLayoutEditPolicy {
	
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		
		LayoutableRelayoutCommand command = null;
		if (!(child instanceof BigraphPart)) {
			command = new LayoutableRelayoutCommand();
			
			command.setModel(child.getModel());
			command.setConstraint(constraint);
			command.prepare();
		}
		return command;
	}
	
	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		
		LayoutableAddCommand command= null;
		if ( child instanceof NestedBigraphPart ) {
			Bigraph target = ( Bigraph )getHost().getModel();
			if ( target.getBigraph() != null ) {
				
				return new CompositionCommand( ( Bigraph )child.getModel(), target.getBigraph(),
						                  ( ( BigreditRootEditPart )child.getRoot() ).getWorkbenchPart() );
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
			System.out.println("instanceofNestedBigraphPart");
			parent= (ModelObject)((Bigraph)getHost().getParent().getModel());
		}else if (getHost() instanceof BigraphPart){
			System.out.println("instanceof BigraphPart");
			parent = (ModelObject)getHost().getModel();
		}else if (getHost() instanceof BRSPart){
			System.out.println("instanceof BRSPart");
			parent = (ModelObject)getHost().getModel();
		}else{
			System.out.println("instanceof something");
			parent = (ModelObject)getHost().getModel();
			
		}
		
//		if (!(parent instanceof Container)) {
//			//return null;
//		} else {
//			//if(((Layoutable) requestObject).getLayout() != null){
//				size.setSize(((Layoutable) requestObject).getLayout().getSize());
//			//}	
//		}
		
		
		/*Layoutable self = (Layoutable)getHost().getModel();
		if (!(self instanceof Container)) {
			return null;
		} else {
			size.setSize(((Layoutable)requestObject).getLayout().getSize());
		}*/
		
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
