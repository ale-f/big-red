package it.uniud.bigredit.command;

import it.uniud.bigredit.CompositionWizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Color;
import dk.itu.big_red.utilities.ui.UI;



public class CompositionCommand extends Command {
	
	private Bigraph inner;
	private Bigraph outer;
	//private IWorkbenchPart part;
	
	private HashMap< Site, Root > placeMap = null;
	private HashMap< InnerName, OuterName > linkMap = null;
	private HashMap< Layoutable, Rectangle > oldLayouts;
	private ArrayList< Link > links = null;
	private HashMap< Layoutable, Rectangle > oldLayouts2 = null;
	private HashMap< Layoutable, Layoutable > oldParents = null;
	private InnerName oldInnerNames = null;
	private boolean executed = false;
	private boolean disabled = false;
	
	private HashMap< Edge, Color > oldColors = null;
	private HashMap< Edge, String > oldNames = null;
	private boolean reactumSpecialCase = false;
	
	public CompositionCommand( Bigraph inner, Bigraph outer)//, IWorkbenchPart part )
	{
		
		this.inner = inner;
		this.outer = outer;
		//this.part  = part;
	}
	
	public void disable()
	{
		disabled = true;
	}
	public boolean wasExecuted()
	{
		return executed;
	}
	
	@Override
	public boolean canExecute()
	{
		return inner != null && outer != null ;//&& part != null; //&& !disabled;
	}
	
	@Override
	public void execute()
	{
		System.out.println("start Compose");
		setLabel( "Compose" );
		CompositionWizard wizard = new CompositionWizard( inner, outer );
		WizardDialog dialog = new WizardDialog(UI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);// part.getSite().getShell(), wizard );
		dialog.create();
		dialog.setTitle( "Bigraph composition" );
		dialog.setMessage( "Composing " + outer.getName() + " with " + inner.getName() );
		if ( dialog.open() != WizardDialog.CANCEL ) {
			placeMap = wizard.getPlaceMap();
			linkMap = wizard.getLinkMap();
			executed = true;
			redo();
		}
	}
	
	class Pair {
		public Pair( Layoutable a, Layoutable b ) {
			this.a = a;
			this.b = b;
		}
		Layoutable a;
		Layoutable b;
	}
	
	private class ColorMix {
		int r = 0;
		int g = 0;
		int b = 0;
		int parts = 0;
	}

	@Override
	public void redo(){
		if ( !executed )
			return;
		
		System.out.println("REDO");
		ChangeGroup cgA = new ChangeGroup(); //change in A
		ChangeGroup cgB = new ChangeGroup(); //change in B
		
		/**start placing roots in sites */
		for (Site site : placeMap.keySet()) {
			Container parent = site.getParent();
			Root root = placeMap.get(site);
			cgA.add(0, site.changeRemove());
			for (Layoutable children : root.getChildren()) {
				System.out.println(children.getType());

				String name = children.getName();
				cgB.add(children.changeRemove());
				cgA.add(parent.changeAddChild(children, name));
			}
			cgB.add(root.changeRemove());
		}
		

		
		/** connect link and ports */
		HashMap<Link, List<? extends Point>> connection=
				new HashMap<Link, List<? extends Point>> ();
		
		for(InnerName innerName : linkMap.keySet()){
			
			Link link=innerName.getLink();

			OuterName outerName=linkMap.get(innerName);
			System.out.println("compute innerName"+ innerName.getName());
			
			List<? extends Point> points=outerName.getPoints();
			connection.put(link, points);

			if(innerName.getLink()!=null){
				/* XXX: untested PointChange replacement! */
				cgA.add(new BoundDescriptor(outer,
						new Point.ChangeDisconnectDescriptor(
								innerName.getIdentifier(),
								innerName.getLink().getIdentifier())));
			}
			cgA.add(innerName.changeRemove());
			cgB.add(outerName.changeRemove());
		}
		
		for (InnerName iNames:inner.getInnerNames()){
			cgB.add(iNames.changeRemove());
			cgA.add(outer.changeAddChild(iNames, iNames.getName()));
		}
		
		/** from hashMap connection create update links*/
		for(Link link : connection.keySet()){
			List<? extends Point> points=connection.get(link);
			System.out.println(points.size());
			for(int i=0;i<points.size();i++){
				System.out.println(points.size());
				Point p=points.get(i);

				if(p.getLink() != null){
					/* XXX: untested PointChange replacement! */
					cgA.add(new BoundDescriptor(outer,
							new Point.ChangeDisconnectDescriptor(
									p.getIdentifier(),
									p.getLink().getIdentifier())));
				}
				/* XXX: untested PointChange replacement! */
				cgA.add(new BoundDescriptor(outer,
						new Point.ChangeConnectDescriptor(
								p.getIdentifier(),
								link.getIdentifier())));

			}
		}
		
		try {
			ExecutorManager.getInstance().tryApplyChange(cgB);
			ExecutorManager.getInstance().tryApplyChange(cgA);
			
		} catch (ChangeCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cgA.clear();
		cgB.clear();
		
		// outer.relayout();
		
		
	}
	
	private void changeConnection(){
		
	}
	
	
	/*@Override
	public void redo()
	{
		if ( !executed )
			return;
		//if ( inner.getParent() instanceof Reaction && ( ( Reaction )inner.getParent() ).getReactum() == inner )
		//	reactumSpecialCase = true;
		ArrayList< Layoutable > startingChildren = new ArrayList< Layoutable >( outer.getChildren() );
		
		links = new ArrayList< Link >();
		for ( InnerName innerName : linkMap.keySet() ) {
			OuterName outerName = linkMap.get( innerName );
			
			ArrayList< Layoutable > sources = new ArrayList< Layoutable >();
			ArrayList< Layoutable > targets = new ArrayList< Layoutable >();
			
			while ( innerName.getLink() != null ) {
				Link link = innerName.getLink();
				Layoutable t = link;
				startingChildren.remove( t );
				if ( !targets.contains( t ) )
					targets.add( t );
				//link.disconnect();
				links.add( link );
			}
			while ( !outerName.getPoints().isEmpty() ) {
				Link link = outerName.getAllConnections().get( 0 );
				
				
				Layoutable t = link.getOther( outerName );
				startingChildren.remove( t );
				if ( !sources.contains( t ) )
					sources.add( t );
				link.disconnect();
				links.add( link );
				
			}
			
			for ( int a = 0; a < sources.size(); a++ ) {
				for ( int b = 0; b < targets.size(); b++ ) {
					boolean already = false;
					for ( Link link : links ) {
						if ( link.getSource() == sources.get( a ) && link.getTarget() == targets.get( b ) ) {
							already = true;
							break;
						}
					}
					if ( !already )
						links.add( new Link( sources.get( a ), targets.get( b ) ) );
				}
			}
		}
		
		oldInnerNames = outer.getInnerNames();
		outer.removeChild( oldInnerNames );
		NameBar innerInnerNames = inner.getInnerNames();
		inner.removeChild( innerInnerNames );
		outer.addChild( innerInnerNames );
		oldLayouts2 = new HashMap< Layoutable, Rectangle >();
		oldParents = new HashMap< Layoutable, Layoutable >();
		
		for ( Site site : placeMap.keySet() ) {
			Root root = placeMap.get( site );
			
			oldParents.put( site, site.getParent() );
			Layoutable parent = site.getParent();
			parent.removeChild( site );
			
			int xMin = root.getLayout().width;
			int yMin = root.getLayout().height;
			int xMax = 0;
			int yMax = 0;
			
			for ( Layoutable t : root.getChildren() ) {
				xMin = Math.min( xMin, t.getLayout().x );
				yMin = Math.min( yMin, t.getLayout().y );
				xMax = Math.max( xMax, t.getLayout().x + t.getLayout().width );
				yMax = Math.max( yMax, t.getLayout().y + t.getLayout().height );
			}
			
			int xCentre = site.getLayout().x + site.getLayout().width  / 2 - xMin - ( xMax - xMin ) / 2;
			int yCentre = site.getLayout().y + site.getLayout().height / 2 - yMin - ( yMax - yMin ) / 2;
			
			while ( !root.getChildren().isEmpty() ) {
				Layoutable t = root.getChildren().get( 0 );
				oldParents.put( t, root );
				t.getParent().removeChild( t );
				parent.addChild( t );
				Rectangle r = new Rectangle( t.getLayout() );
				r.x += xCentre;
				r.y += yCentre;
				oldLayouts2.put( t, new Rectangle( t.getLayout() ) );
				t.setLayout( r );
			}
		}
		
		HashMap< Edge, ColorMix > mixMap = new HashMap< Edge, ColorMix >();
		
		oldColors = new HashMap< Edge, Color >();
		for ( Layoutable t : new ArrayList< Layoutable >( inner.getChildren() ) ) {
			if ( t instanceof Edge ) {				
				ArrayList< Layoutable > otherEdges = new ArrayList< Layoutable >();
				for ( Link link : t.getAllConnections() ) {
					if ( link.getOther( t ) instanceof Edge ) {
						link.disconnect();
						links.add( link );
						otherEdges.add( link.getOther( t ) );
						startingChildren.remove( link.getOther( t ) );
					}
				}
				if ( otherEdges.size() == 0 )
					continue;
				boolean first = true;
				Point avg = new Point( 0, 0 );
				for ( Layoutable edge : otherEdges ) {
					avg.translate( edge.getLayout().getTopLeft() );
					if ( first )
						first = false;
					else {
						oldParents.put( edge, edge.getParent() );
						edge.getParent().removeChild( edge );
						for ( Link link : edge.getAllConnections() ) {
							Layoutable other = link.getOther( edge );
							link.disconnect();
							links.add( link );
							links.add( new Link( other, otherEdges.get( 0 ) ) );
							startingChildren.remove( other );
						}
					}
				}
				avg.x /= otherEdges.size();
				avg.y /= otherEdges.size();
				oldLayouts2.put( otherEdges.get( 0 ), otherEdges.get( 0 ).getLayout() );
				otherEdges.get( 0 ).setLayout( new Rectangle( avg.x, avg.y, EdgeFigure.DEF_WIDTH, EdgeFigure.DEF_HEIGHT ) );
				
				ColorMix mix = new ColorMix();
				ArrayList< Layoutable > mixers = new ArrayList< Layoutable >( otherEdges );
				mixers.add( t );
				for ( Layoutable mixer : mixers ) {
					Edge e = ( Edge )mixer;
					if ( mixMap.containsKey( e ) ) {
						mix.r += mixMap.get( e ).r;
						mix.g += mixMap.get( e ).g;
						mix.b += mixMap.get( e ).b;
						mix.parts += mixMap.get( e ).parts;
					}
					else {
						mix.r += e.getColor().getRed();
						mix.g += e.getColor().getGreen();
						mix.b += e.getColor().getBlue();
						mix.parts++;
					}
				}
				mixMap.put( ( Edge )otherEdges.get( 0 ), mix );
				
				for ( Link link : t.getAllConnections() ) {
					link.disconnect();
					links.add( link );
					Layoutable other = link.getOther( t );
					links.add( new Link( otherEdges.get( 0 ), other ) );
				}
			}
		}
		for ( Layoutable t : new ArrayList< Layoutable >( inner.getChildren() ) ) {
			if ( t instanceof Edge ) {
				boolean b = false;
				for ( Link link : t.getAllConnections() ) {
					if ( link.getOther( t ).getGraph() == outer ) {
						b = true;
						break;
					}
				}
				if ( !b )
					continue;
				oldParents.put( t, t.getParent() );
				t.getParent().removeChild( t );
				outer.addChild( t );
			}
		}
		for ( Layoutable t : new ArrayList< Layoutable >( outer.getChildren() ) ) {
			if ( t instanceof Edge && t.getAllConnections().size() == 0 ) {
				for ( Link link : t.getAllConnections() ) {
					link.disconnect();
					links.add( link );
				}
				oldParents.put( t, t.getParent() );
				t.getParent().removeChild( t );
			}
			else if ( t instanceof Edge && !startingChildren.contains( t ) ) {
				int count = 0;
				Point avg = new Point( 0, 0 );
				for ( Link link : t.getAllConnections() ) {
					count++;
					Rectangle r = link.getOther( t ).getLayout();
					Point p = new Point( r.x + r.width / 2, r.y + r.height / 2 );
					link.getOther( t ).getParent().translateToAbsolute( p );
					avg.translate( p );
				}
				avg.x /= count;
				avg.y /= count;
				outer.translateToRelative( avg );
				
				if ( count == 1 ) {
					Layoutable source = t.getAllConnections().get( 0 ).getOther( t );
					Point p1 = source.getLayout().getCenter();
					source.getParent().translateToAbsolute( p1 );
					Point p2 = source.getParent().getLayout().getCenter();
					source.getParent().getParent().translateToAbsolute( p2 );
					
					if ( source instanceof Port )
						avg = p1.scale( 2 ).translate( p2.negate() );
					else
						avg = new Point( p1.x, p2.y + ( ( ( NameBar )source.getParent() ).isInner() ? -source.getParent().getLayout().height : source.getParent().getLayout().height ) );
					outer.translateToRelative( avg );
				}
				
				if ( !oldLayouts2.containsKey( t ) )
					oldLayouts2.put( t, t.getLayout() );
				t.setLayout( new Rectangle( avg.x - EdgeFigure.DEF_WIDTH / 2, avg.y - EdgeFigure.DEF_HEIGHT / 2,
						                    EdgeFigure.DEF_WIDTH, EdgeFigure.DEF_HEIGHT ) );
			}
		}
		
		oldNames = new HashMap< Edge, String >();
		for ( Edge t : mixMap.keySet() ) {
			if ( t.getGraph() != outer )
				continue;
			oldColors.put( t, t.getColor() );
			ColorMix mix = mixMap.get( t );
			t.setColor( new Color( null, mix.r / mix.parts, mix.g / mix.parts, mix.b / mix.parts ) );
			oldNames.put( t, t.getName() );
			t.setName( "Composed edge" );
		}
		
		oldParents.put( inner, inner.getParent() );
		inner.getParent().removeChild( inner );
		oldLayouts = outer.organiseGraph();
	}*/
//	
//	@Override
//	public void undo()
//	{
//		if ( !executed )
//			return;
//		for ( Edge edge : oldColors.keySet() ) {
//			edge.setColor( oldColors.get( edge ) );
//			edge.setName( oldNames.get( edge ) );
//		}
//		outer.undoOrganiseGraph( oldLayouts );
//		
//		for ( Layoutable t : oldParents.keySet() ) {
//			if ( t.getParent() != null )
//				t.getParent().removeChild( t );
//			if ( oldLayouts2.keySet().contains( t ) )
//				t.setLayout( oldLayouts2.get( t ) );
//			if ( t == inner && reactumSpecialCase )
//				( ( Reaction )oldParents.get( t ) ).addChild( t, true );
//			else
//				oldParents.get( t ).addChild( t );
//		}
//		for ( Layoutable t : oldLayouts2.keySet() ) {
//			if ( oldParents.keySet().contains( t ) )
//				continue;
//			t.setLayout( oldLayouts2.get( t ) );
//		}
//		
//		NameBar innerNames = outer.getInnerNames();
//		outer.removeChild( innerNames );
//		inner.addChild( innerNames );
//		outer.addChild( oldInnerNames );
//		
//		for ( Link link : links ) {
//			if ( link.isConnected() )
//				link.disconnect();
//			else
//				link.reconnect();
//		}
//		inner.checkLabels( inner );
//		outer.checkLabels( outer );
//	}

}
