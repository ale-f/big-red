package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;

import java.util.HashMap;


import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Root;




public class CreateCommand extends Command {
	
	private Container parent;
	private Layoutable child;
	private Rectangle layout;
	private HashMap< Layoutable, Rectangle > oldLayouts;
	
	public CreateCommand()
	{
		super();
		parent = null;
		child = null;
		setLabel( "Create" );
	}
	
	public void setParent( Object o )
	{
		if ( o instanceof Container )
			parent = ( Container )o;
	}
	public Container getParent()
	{
		return parent;
	}
	public void setChild( Object o )
	{
		if ( o instanceof Layoutable )
			child = ( Layoutable )o;
	}
	
	public void setLayout( Rectangle r )
	{
		layout = r;
	}
	
	@Override
	public boolean canExecute()
	{
		return !( parent == null || child == null );
	}

	@Override
	public void execute()
	{

		child.changeLayout(layout);
		
		//child.setLayout( layout, false, true );
		if(parent instanceof BRS){
			((BRS)parent).addChild(child);
		}
		//parent.addChild( child );
		/*oldLayouts = parent.organiseGraph();
		if ( child instanceof Node )
			( ( Node )child ).getControl().refreshPorts( ( Node )child, child.getLayout() );
			*/
	}
	

}
