package it.uniud.bigredit.policy;

import it.uniud.bigredit.command.CreateCommand;
import it.uniud.bigredit.editparts.BRSPart;
import it.uniud.bigredit.editparts.NestedBigraphPart;
import it.uniud.bigredit.editparts.ReactionPart;
import it.uniud.bigredit.figure.NestedBigraphFigure;
import it.uniud.bigredit.figure.ReactionFiguren;
import it.uniud.bigredit.model.Reaction;

import java.util.HashMap;
import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Node;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import dk.itu.big_red.editors.bigraph.parts.NodePart;
import dk.itu.big_red.editors.bigraph.parts.RootPart;



public class LayoutPolicy extends XYLayoutEditPolicy {
	
	private HashMap< Shape, Integer > oldLineWidths = new HashMap< Shape, Integer >();
	/*
	@Override
	protected Command createChangeConstraintCommand( EditPart child, Object constraint )
	{
		Rectangle r = ( Rectangle )constraint;
		if ( child instanceof GraphPart || child instanceof RootPart || child instanceof NodePart || child instanceof SitePart || child instanceof ReactionPart ||
			 ( ( child instanceof NamePart || child instanceof EdgePart ) && r.width == ( ( BaseNode )child.getModel() ).getLayout().width && r.setHeight( == ( ( BaseNode )child.getModel() ).getLayout().height ) ) {
			LayoutCommand command = new LayoutCommand();
			command.setModel( child.getModel() );
			command.setConstraint( r );
			return command;
		}
		return null;
	}*/
	/*
	@Override
	protected Command createAddCommand( EditPart child, Object constraint )
	{
		if ( child instanceof GraphPart ) {
			BaseNode target = ( BaseNode )getHost().getModel();
			if ( target.getGraph() != null ) {
				return new CompositionCommand( ( Graph )child.getModel(), target.getGraph(),
						                       ( ( BigreditRootEditPart )child.getRoot() ).getWorkbenchPart() );
			}
		}
		if ( child instanceof ReactionPart ) {
			BaseNode target = ( BaseNode )getHost().getModel();
			if ( target.getGraph() != null ) {
				return new ReactionCommand( ( Reaction )child.getModel(), target.getGraph(),
						                    ( ( BigreditRootEditPart )child.getRoot() ).getWorkbenchPart() );
			}
		}
		ReparentCommand command = new ReparentCommand();
		command.setData( child.getModel(), getHost().getModel(), ( Rectangle )constraint );
		return command;
	}*/
	
	@Override
	protected Command getCreateCommand( CreateRequest request )
	{
		
		if ( request.getType() != REQ_CREATE )
			return null;
		Object object = request.getNewObject();
		
		Rectangle constraint = new  Rectangle ((org.eclipse.draw2d.geometry.Rectangle)getConstraintFor( request ));
		CreateCommand command = new CreateCommand();
		
		/*if ( getHost() instanceof NameBarPart && object instanceof Root ) {
			command.setParent( ( ( BaseNode )getHost().getModel() ).getGraph() );
			constraint.x += ( ( BaseNode )getHost().getModel() ).getLayout().x;
			constraint.y += ( ( BaseNode )getHost().getModel() ).getLayout().y;
		}
		else*/
		command.setParent( getHost().getModel() );
		command.setChild( object );
		boolean create = false;
		
		if ( object instanceof Bigraph && ( getHost() instanceof BRSPart || getHost() instanceof ReactionPart ) ) {
			if ( constraint.width() <= 0 )
				constraint.setWidth(NestedBigraphFigure.DEF_WIDTH);
			if ( constraint.height() <= 0 )
				constraint.setHeight(NestedBigraphFigure.DEF_HEIGHT);
			
			
			create = true;
		}
		if ( object instanceof Reaction && getHost() instanceof BRSPart ) {
			constraint.setWidth(ReactionFiguren.DEF_WIDTH);
			constraint.setHeight(ReactionFiguren.DEF_HEIGHT);
			create = true;
		}
		if ( object instanceof Root && ( getHost() instanceof NestedBigraphPart  ) ) {
			Container t = (Container) command.getParent();
			if ( constraint.width() <= 0 || constraint.height()  <= 0 ) {
				/*constraint.getWidth() = Math.min( t.getLayout().getWidth() / 2, RootFigure.DEF_WIDTH );
				constraint.height = Math.min( t.getLayout().getHeight()  / 2 - NameBarFigure.DEF_HEIGHT - BaseNode.MARGIN, RootFigure.DEF_HEIGHT );
				*/
				constraint.setWidth(Math.max( constraint.width(), constraint.height()  ));
				constraint.setHeight(constraint.width());
			}
			create = true;
		}
		if ( object instanceof Node &&
		     ( getHost() instanceof RootPart || getHost() instanceof NodePart ) ) {
			if ( constraint.width() <= 0 || constraint.height()  <= 0 ) {
				/*constraint.getWidth() = Math.min( ( ( BaseNode )getHost().getModel() ).getLayout().getWidth() / 2, NodeFigure.DEF_WIDTH );
				constraint.height = Math.min( ( ( BaseNode )getHost().getModel() ).getLayout().height / 2, NodeFigure.DEF_HEIGHT );
				constraint.getWidth() = Math.max( constraint.getWidth(), constraint.getHeight()  );
				constraint.height = constraint.getWidth();*/
			}
			create = true;
		}
		if ( object instanceof Site &&
			 ( getHost() instanceof RootPart || getHost() instanceof NodePart ) ) {
			if ( constraint.width() <= 0 || constraint.height()  <= 0 ) {
				/*constraint.getWidth() = Math.min( ( ( BaseNode )getHost().getModel() ).getLayout().getWidth() / 2, SiteFigure.DEF_WIDTH );
				constraint.height = Math.min( ( ( BaseNode )getHost().getModel() ).getLayout().getHeight()  / 2, SiteFigure.DEF_HEIGHT );
				*/
				constraint.setWidth(Math.max( constraint.width(), constraint.height()  ));
				constraint.setHeight(constraint.width());
			}
			create = true;
		}
		/*if ( object instanceof Name && getHost() instanceof NameBarPart ) {
			constraint.width  = NameFigure.DEF_WIDTH;
			constraint.height = NameFigure.DEF_HEIGHT;
			create = true;
		}*/
		
		if ( create ) {
			command.setLayout(constraint);
			
			return command;
		}
		return null;
	}
	/*
	@SuppressWarnings( "unchecked" )
	private void findEdges( BaseNode model, List list )
	{
		for ( Link link : model.getAllConnections() )
			if ( link.getOther( model ) instanceof Edge && !list.contains( link.getOther( model ) ) )
				list.add( link.getOther( model ).getEditPart() );
		for ( BaseNode t : model.getChildren() )
			findEdges( t, list );
	}
	@SuppressWarnings( "unchecked" )
	@Override
	protected Command getAddCommand( Request generic )
	{
		ChangeBoundsRequest request = ( ChangeBoundsRequest )generic;
		List editParts = new ArrayList( request.getEditParts() );
		CompoundCommand command = new CompoundCommand();
		GraphicalEditPart childPart;
		Rectangle r;
		Object constraint;
		
		int n = 0;
		int graphs = 0;
		for ( Object o : request.getEditParts() ) {
			if ( o instanceof GraphPart ) {
				graphs++;
				continue;
			}
			if ( o instanceof ReactionPart )
				continue;
			if ( o instanceof EdgePart && ( ( BaseNode )getHost().getModel() ).getGraph() != ( ( BaseNode )( ( EditPart )o ).getModel() ).getGraph() )
				n++;
			BaseNode model = ( BaseNode )( ( GraphicalEditPart )o ).getModel();
			if ( ( ( BaseNode )getHost().getModel() ).getGraph() != model.getGraph() )
				findEdges( model, editParts );
		}
		if ( n == request.getEditParts().size() )
			return command;
		if ( getHost() instanceof ReactionPart && graphs > 2 - ( ( BaseNode )getHost().getModel() ).getChildren().size() )
			return command;

		for ( int i = 0; i < editParts.size(); i++ ) {
			childPart = ( GraphicalEditPart )editParts.get( i );
			r = childPart.getFigure().getBounds().getCopy();
			childPart.getFigure().translateToAbsolute( r );
			r = request.getTransformedRectangle( r );
			getLayoutContainer().translateToRelative( r );
			getLayoutContainer().translateFromParent( r );
			r.translate( getLayoutOrigin().getNegated() );
			constraint = getConstraintFor( r );
			Command c = createAddCommand( childPart, translateToModelConstraint( constraint ) );
			command.add( c );
			if ( childPart instanceof EdgePart && ( ( BaseNode )getHost().getModel() ).getGraph() != ( ( BaseNode )childPart.getModel() ).getGraph() )
				( ( ReparentCommand )c ).setClone( true );
			if ( editParts.size() > 1 && childPart instanceof GraphPart && c instanceof CompositionCommand )
				( ( CompositionCommand )c ).disable();
			if ( editParts.size() > 1 && childPart instanceof ReactionPart && c instanceof ReactionCommand )
				( ( ReactionCommand )c ).disable();
		}
		if ( command.size() > 0 && command.getCommands().get( command.size() - 1 ) instanceof ReparentCommand ) {
			( ( ReparentCommand )command.getCommands().get( command.size() - 1 ) ).setDisconnectLinks( true );
			( ( ReparentCommand )command.getCommands().get( command.size() - 1 ) ).setOrganiseGraph( true );
		}
		return command.unwrap();
	}
	
	@SuppressWarnings( "unchecked" )
	@Override
	protected Command getCloneCommand( ChangeBoundsRequest request )
	{
		List editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		command.setLabel( "Clone" );
		GraphicalEditPart childPart;
		Rectangle r;
		Object constraint;
		
		CloneCommand clone = new CloneCommand();
		command.add( clone );
		
		int graphs = 0;
		for ( int i = 0; i < editParts.size(); i++ ) {
			childPart = ( GraphicalEditPart )editParts.get( i );
			if ( childPart instanceof GraphPart )
				graphs++;
			clone.addElement( ( BaseNode )childPart.getModel() );
		}
		if ( getHost() instanceof ReactionPart && graphs > 2 - ( ( BaseNode )getHost().getModel() ).getChildren().size() )
			clone.disable();
		clone.init();
		Iterator< Entry< BaseNode, BaseNode > > i = clone.getClones().entrySet().iterator();
		
		while ( i.hasNext() ) {
			Entry< BaseNode, BaseNode > e = i.next();
			BaseNode node = e.getValue();
			
			childPart = e.getKey().getEditPart();
			r = childPart.getFigure().getBounds().getCopy();
			childPart.getFigure().translateToAbsolute( r );
			r = request.getTransformedRectangle( r );
			getLayoutContainer().translateToRelative( r );
			getLayoutContainer().translateFromParent( r );
			r.translate( getLayoutOrigin().getNegated() );
			constraint = getConstraintFor( r );

			ReparentCommand c = new ReparentCommand();
			c.setData( node, getHost().getModel(), ( Rectangle )translateToModelConstraint( constraint ) );
			command.add( c );
			if ( !i.hasNext() )
				c.setOrganiseGraph( true );
		}
		return command.unwrap();
	}
	
	@Override
	public EditPolicy createChildEditPolicy( EditPart child )
	{
		if ( child instanceof NamePart || child instanceof EdgePart || child instanceof ReactionPart )
			return new NonResizableEditPolicy();
		return super.createChildEditPolicy( child );
	}
	
	@Override
	public void showLayoutTargetFeedback( Request request )
	{
		EditPart part = null;
		if ( request.getType() == REQ_CREATE ) {
			CreateCommand c = ( CreateCommand )getCreateCommand( ( CreateRequest )request );
			if ( c != null )
				part = c.getParent().getEditPart();
		}
		if ( request.getType() == REQ_ADD || request.getType() == REQ_CLONE ) {
			if ( ( request.getType() == REQ_ADD   && !getAddCommand( request ).canExecute() ) ||
				 ( request.getType() == REQ_CLONE && !getCloneCommand( ( ChangeBoundsRequest )request ).canExecute() ) )
				return;

			boolean b = true;
			boolean b2 = false;
			boolean b2Checked = false;
			for ( Object o : ( ( GroupRequest )request ).getEditParts() ) {
				EditPart t = ( EditPart )o;
				
				if ( t instanceof GraphPart ) {
					if ( ( ( GroupRequest )request ).getEditParts().size() > 1 && !( getHost() instanceof ReactionPart ) )
						b = false;
					BaseNode target = ( BaseNode )getHost().getModel();
					if ( target.getGraph() != null ) {
						part = target.getGraph().getEditPart();
						
						for ( BaseNode n : target.getGraph().getInnerNameList() )
							showFeedback( n.getEditPart() );
						for ( BaseNode n : target.getGraph().getSiteList() )
							showFeedback( n.getEditPart() );
						for ( BaseNode n : ( ( Graph )t.getModel() ).getRootList() )
							showFeedback( n.getEditPart() );
						for ( BaseNode n : ( ( Graph )t.getModel() ).getOuterNameList() )
							showFeedback( n.getEditPart() );
					}
					else if ( target instanceof Reaction )
						part = getHost();
					else
						b = false;
				}
				if ( t instanceof ReactionPart ) {
					if ( ( ( GroupRequest )request ).getEditParts().size() > 1 )
						b = false;
					BaseNode target = ( BaseNode )getHost().getModel();
					if ( target.getGraph() != null )
						part = target.getGraph().getEditPart();
					else
						b = false;
				}
				else {
					ReparentCommand command = new ReparentCommand();
					command.setData( t.getModel(), getHost().getModel(), new Rectangle( 0, 0, -1, -1 ) );
					if ( !( ( t.getModel() instanceof Edge || t.getModel() instanceof Name ) && part != null ) && command.canExecute() ) {
						b2Checked = true;
						if ( command.getParent() != ( ( BaseNode )t.getModel() ).getParent() || request.getType() == REQ_CLONE )
							b2 = true;
						part = command.getParent().getEditPart();
					}
				}
			}
			if ( !b || ( !b2 && b2Checked ) )
				part = null;
		}

		showFeedback( part );
	}
	
	private void showFeedback( EditPart part )
	{
		if ( part instanceof BaseEditPart ) {
			if ( ( ( BaseEditPart )part ).getFigure() instanceof Shape ) {
				Shape s = ( Shape )( ( BaseEditPart )part ).getFigure();
				if ( !oldLineWidths.containsKey( s ) )
					oldLineWidths.put( s, s.getLineWidth() );
				s.setLineWidth( oldLineWidths.get( s ) + 1 );
			}
		}
	}
	private void eraseFeedback( EditPart part )
	{
		if ( part instanceof BaseEditPart ) {
			if ( ( ( BaseEditPart )part ).getFigure() instanceof Shape ) {
				Shape s = ( Shape )( ( BaseEditPart )part ).getFigure();
				s.setLineWidth( oldLineWidths.get( s ) );
				oldLineWidths.remove( s );
			}
		}
	}
	
	@Override
	public void eraseLayoutTargetFeedback( Request request )
	{
		EditPart part = null;
		if ( request.getType() == REQ_CREATE ) {
			CreateCommand c = ( CreateCommand )getCreateCommand( ( CreateRequest )request );
			if ( c != null )
				part = c.getParent().getEditPart();
		}
		if ( request.getType() == REQ_ADD || request.getType() == REQ_CLONE ) {
			if ( ( request.getType() == REQ_ADD   && !getAddCommand( request ).canExecute() ) ||
			     ( request.getType() == REQ_CLONE && !getCloneCommand( ( ChangeBoundsRequest )request ).canExecute() ) )
					return;

			boolean b = true;
			boolean b2 = false;
			boolean b2Checked = false;
			for ( Object o : ( ( GroupRequest )request ).getEditParts() ) {
				EditPart t = ( EditPart )o;
				
				if ( t instanceof GraphPart ) {
					if ( ( ( GroupRequest )request ).getEditParts().size() > 1 && !( getHost() instanceof ReactionPart ) )
						b = false;
					BaseNode target = ( BaseNode )getHost().getModel();
					if ( target.getGraph() != null ) {
						part = target.getGraph().getEditPart();
						
						for ( BaseNode n : target.getGraph().getInnerNameList() )
							eraseFeedback( n.getEditPart() );
						for ( BaseNode n : target.getGraph().getSiteList() )
							eraseFeedback( n.getEditPart() );
						for ( BaseNode n : ( ( Graph )t.getModel() ).getRootList() )
							eraseFeedback( n.getEditPart() );
						for ( BaseNode n : ( ( Graph )t.getModel() ).getOuterNameList() )
							eraseFeedback( n.getEditPart() );
					}
					else if ( target instanceof Reaction )
						part = getHost();
					else
						b = false;
				}
				if ( t instanceof ReactionPart ) {
					if ( ( ( GroupRequest )request ).getEditParts().size() > 1 )
						b = false;
					BaseNode target = ( BaseNode )getHost().getModel();
					if ( target.getGraph() != null )
						part = target.getGraph().getEditPart();
					else
						b = false;
				}
				else {
					ReparentCommand command = new ReparentCommand();
					command.setData( t.getModel(), getHost().getModel(), new Rectangle( 0, 0, -1, -1 ) );
					if ( !( ( t.getModel() instanceof Edge || t.getModel() instanceof Name ) && part != null ) && command.canExecute() ) {
						b2Checked = true;
						if ( command.getParent() != ( ( BaseNode )t.getModel() ).getParent() || request.getType() == REQ_CLONE )
							b2 = true;
						part = command.getParent().getEditPart();
					}
				}
			}
			if ( !b || ( !b2 && b2Checked ) )
				part = null;
		}
		eraseFeedback( part );
	}*/
}
