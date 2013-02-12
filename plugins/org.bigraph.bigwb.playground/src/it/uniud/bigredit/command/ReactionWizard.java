package it.uniud.bigredit.command;

import it.uniud.bigredit.model.MatchData;
import it.uniud.bigredit.model.Reaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.uniud.bigraph.match.PlaceMatch;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.model.LayoutUtilities;



public class ReactionWizard extends Wizard {
	
	private int selection = -1;
	
	class InvalidPage extends WizardPage {
		
		public InvalidPage()
		{
			super( "Invalid" );
		}
		
		@Override
		public void createControl( Composite parent )
		{			
			String s = "This reaction rule cannot be applied.\n";
			if ( redex == null && reactum == null )
				s += "\nNeither redex nor reactum has been supplied.";
			else if ( redex == null )
				s += "\nNo redex has been supplied.";
			else if ( reactum == null )
				s += "\nNo reactum has been supplied.";
			else {
				if ( redex.getOuterNames().size() != reactum.getOuterNames().size() )
					 s += "\nRedex has " + ( redex.getOuterNames().size() == 0 ? "no" : redex.getOuterNames().size() ) +
				     	  " outer name" + ( redex.getOuterNames().size() == 1 ? "" : "s" ) + ", but reactum has " +
			         	  ( reactum.getOuterNames().size() == 0 ? "none" : reactum.getOuterNames().size() ) + ".";
				if ( reactum.getSites().size() > redex.getSites().size() )
					 s += "\nReactum has more sites than redex.";
			}
			/** TODO need to check initial components for inner name, etc.... */
			/*if ( redex != null ) {
				int t = GraphMatching.testReactionComponent( redex );
				if ( ( t & GraphMatching.TEST_NOT_SINGLE_ROOT ) > 0 )
					s += "\nRedex does not contain exactly one root.";
				if ( ( t & GraphMatching.TEST_HAS_INNER_NAMES ) > 0 )
					s += "\nRedex contains inner names.";
				if ( ( t & GraphMatching.TEST_ISOLATED_NAMES ) > 0 )
					s += "\nOuter names of redex are not all linked.";
				if ( ( t & GraphMatching.TEST_MULTIPLE_SITE_NODES ) > 0 )
					s += "\nSites of redex are not independent.";
				if ( ( t & GraphMatching.TEST_MULTIPLE_NAME_PORTS ) > 0 )
					s += "\nOuter names of redex are not independent.";
			}
			if ( reactum != null ) {
				int t = GraphMatching.testReactionComponent( reactum );
				if ( ( t & GraphMatching.TEST_NOT_SINGLE_ROOT ) > 0 )
					s += "\nReactum does not contain exactly one root.";
				if ( ( t & GraphMatching.TEST_HAS_INNER_NAMES ) > 0 )
					s += "\nReactum contains inner names.";	
			}*/
			
			Composite control = new Composite( parent, 0 );
			Label text = new Label( control, 0 );
			text.setText( s );
			control.setLayout( new RowLayout() );
			setControl( control );
		}
	}	

	class MatchesPage extends WizardPage {
		
		private List matchList;
		private Canvas canvas;
		private Label label;
		
		private ArrayList< String > matchNames;
		
		private List redexList = null;
		private List reactumList = null;
		private List redexSiteList = null;
		private List reactumSiteList = null;
		private ArrayList< Integer > permutation;
		private ArrayList< Integer > sitePermutation;
		private int select = -1;
		private int siteSelect = -1;
		private Text textAssignment;
		
		public MatchesPage()
		{
			super( "Matches" );
		}
		
		@Override
		public void createControl( Composite parent )
		{
			Composite control = new Composite( parent, 0 );
			RowLayout layout = new RowLayout( SWT.VERTICAL );
			layout.fill = true;
			layout.spacing = 16;
			control.setLayout( layout );
			
			Composite parts = new Composite( control, 0 );
			layout = new RowLayout();
			layout.fill = true;
			layout.spacing = 16;
			layout.marginLeft = layout.marginRight = 0;
			layout.marginTop = layout.marginBottom = 0;
			parts.setLayout( layout );
			
			Group lGroup = new Group( parts, 0 );
			lGroup.setText( "Matches" );
			layout = new RowLayout( SWT.VERTICAL );
			layout.marginWidth = layout.marginHeight = 2;
			lGroup.setLayout( layout );
			Group rGroup = new Group( parts, 0 );
			rGroup.setText( "Details" );
			layout = new RowLayout( SWT.VERTICAL );
			layout.marginWidth = layout.marginHeight = 2;
			layout.spacing = 8;
			rGroup.setLayout( layout );
			
			if ( matches.size() > 0 ) {
				matchList = new List( lGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL );
				matchList.setLayoutData( new RowData( 128, 320 ) );
				matchNames = new ArrayList< String >();
				int i = 0;
				for ( MatchData m : matches ) {
					
					String s= "";
					for (Entry<ModelObject, ModelObject> e: m.getMappingData().entrySet()){
						
						
						ModelObject matchRootR = e.getKey();
						s += matchRootR.getType() +" "+ ((Layoutable)matchRootR).getName();
						
						ModelObject matchElementA = e.getValue();
						if (!(matchRootR instanceof Site)) {
							

							s += " -> " + matchElementA.getType() + " "
									+ ((Layoutable) matchElementA).getName()
									+ "\n";
						}else{
							s += " -> ";
							for (ModelObject mo:m.getSiteMapping(matchRootR) ){
								s += mo.getType() + " ";
								s += ((Layoutable)mo).getName()+ ", ";
							}
							s += "\n";
							
						}
//						matchList.add( "Match " + ( i + 1 ) + ": " + s );
//						matchNames.add( s );
//						i++;
						
					}
					
//					ModelObject matchRoot =  m.targetMatchRoot;
//					String s = matchRoot.reference.getName();
//					if ( matchRoot.type == GraphMatching.ROOT )
//						s += " (Root)";
//					if ( matchRoot.type == GraphMatching.NODE )
//						s += " (" + ( ( Node )matchRoot.reference ).getControl().getName() + ")";
//
					matchList.add( "Match " + ( i + 1 ) + ": " + s );
					matchNames.add( s );
					i++;
				}
			}
			else {
				Label linkLabel = new Label( lGroup, 0 );
				linkLabel.setText( "No matches found." );
				linkLabel.setLayoutData( new RowData( 128, 320 ) );
			}
			
			canvas = new Canvas( rGroup, SWT.BORDER );
		    canvas.addPaintListener( new PaintListener() {
		    	@Override
				public void paintControl( PaintEvent e ) {
					updateDetails();
				}
		    } );
			canvas.setLayoutData( new RowData( 256, 256 ) );
			label = new Label( rGroup, 0 );
			label.setLayoutData( new RowData( 256, 64 ) );
			
			if ( matchList != null ) {
				matchList.addListener( SWT.Selection, new Listener() {
				@Override
				public void handleEvent( Event e )
					{
						updateDetails();
					}
				} );
			}
			updateDetails();
			
			/** Start Group part */
			
			Group siteGroup = new Group( control, 0 );
			siteGroup.setText( "Assignment" );
			layout = new RowLayout();
			layout.center = true;
			layout.marginWidth = layout.marginHeight = 2;
			layout.spacing = 8;
			siteGroup.setLayout( layout );
			
			ScrolledComposite scrolledComposite = new ScrolledComposite(siteGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			scrolledComposite.setLayoutData(new RowData(388, SWT.DEFAULT));
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);
			
			textAssignment = new Text(scrolledComposite, SWT.BORDER | SWT.MULTI| SWT.V_SCROLL);
			textAssignment.setText("");
			scrolledComposite.setContent(textAssignment);
			scrolledComposite.setMinSize(textAssignment.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//			
//			if ( redex.getSites().size() > 0 ) {
//				ScrolledComposite scroll = new ScrolledComposite( siteGroup, SWT.V_SCROLL | SWT.BORDER );
//				Composite comp = new Composite( scroll, 0 );
//				scroll.setContent( comp );
//				
//				layout = new RowLayout();
//				layout.center = true;
//				layout.marginWidth = layout.marginHeight = 2;
//				layout.spacing = 16;
//				comp.setLayout( layout );
//				
//				redexSiteList = new List( comp, SWT.BORDER | SWT.SINGLE );				
//				Label siteLabel = new Label( comp, 0 );
//				siteLabel.setText( ">" );
//				siteLabel.setAlignment( SWT.CENTER );
//				reactumSiteList = new List( comp, SWT.BORDER | SWT.SINGLE );
//				
//				ArrayList< Site > redexSites = (ArrayList<Site>) redex.getSites();
//				for ( int j = 0; j < redexSites.size(); j++ )
//					redexSiteList.add( redexSites.get( j ).getName() );
//				sitePermutation = new ArrayList< Integer >();
//				for ( int j = 0; j < redex.getSites().size(); j++ )
//					sitePermutation.add( new Integer( j ) );
//				
//				updateLists();
//				redexSiteList.pack();
//				redexSiteList.setLayoutData( new RowData( 128, redexSiteList.getBounds().height ) );
//				reactumSiteList.pack();
//				reactumSiteList.setLayoutData( new RowData( 128, reactumSiteList.getBounds().height ) );
//				comp.pack();
//				scroll.setLayoutData( new RowData( comp.getBounds().width, Math.min( comp.getBounds().height + 4, 64 ) ) );
//				
//				Composite siteArrows = new Composite( siteGroup, 0 );
//				Button siteUp = new Button( siteArrows, 0 );
//				Button siteDown = new Button( siteArrows, 0 );
//				siteUp.setText( "Move up" );
//				siteDown.setText( "Move down" );
//				if ( redexSites.size() <= 1 ) {
//					siteUp.setEnabled( false );
//					siteDown.setEnabled( false );
//				}
//				
//				layout = new RowLayout( SWT.VERTICAL );
//				layout.fill = true;
//				siteArrows.setLayout( layout );
//				
//				redexSiteList.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						redexSiteList.setSelection( new int[ 0 ] );
//						updateLists();
//					}
//				} );
//				reactumSiteList.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						siteSelect = sitePermutation.indexOf( new Integer( reactumSiteList.getSelectionIndex() ) );
//						updateLists();
//					}
//				} );
//				siteUp.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						int p = sitePermutation.get( siteSelect );
//						int i = sitePermutation.indexOf( p - 1 );
//						if ( i >= 0 ) {
//							sitePermutation.set( siteSelect, p - 1 );
//							sitePermutation.set( i, p );
//						}
//						updateLists();
//					}
//				} );
//				siteDown.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						int p = sitePermutation.get( siteSelect );
//						int i = sitePermutation.indexOf( p + 1 );
//						if ( i >= 0 ) {
//							sitePermutation.set( siteSelect, p + 1 );
//							sitePermutation.set( i, p );
//						}
//						updateLists();
//					}
//				} );
//			}
//			else {
//				Label siteLabel = new Label( siteGroup, 0 );
//				siteLabel.setText( "Nothing to assign." );
//			}
			
			/** Link Group Part */
			
//			Group linkGroup = new Group( control, 0 );
//			linkGroup.setText( "Outer name assignment" );
//			layout = new RowLayout();
//			layout.center = true;
//			layout.marginWidth = layout.marginHeight = 2;
//			layout.spacing = 8;
//			linkGroup.setLayout( layout );
//			
//			if ( redex.getOuterNames().size() > 0 ) {
//				ScrolledComposite scroll = new ScrolledComposite( linkGroup, SWT.V_SCROLL | SWT.BORDER );
//				Composite comp = new Composite( scroll, 0 );
//				scroll.setContent( comp );
//				
//				layout = new RowLayout();
//				layout.center = true;
//				layout.marginWidth = layout.marginHeight = 2;
//				layout.spacing = 16;
//				comp.setLayout( layout );
//				
//				redexList = new List( comp, SWT.BORDER | SWT.SINGLE );				
//				Label linkLabel = new Label( comp, 0 );
//				linkLabel.setText( ">" );
//				linkLabel.setAlignment( SWT.CENTER );
//				reactumList = new List( comp, SWT.BORDER | SWT.SINGLE );
//				
//				ArrayList< OuterName > redexNames = (ArrayList<OuterName>) redex.getOuterNames();
//				for ( int j = 0; j < redexNames.size(); j++ )
//					redexList.add( redexNames.get( j ).getName() );
//				permutation = new ArrayList< Integer >();
//				for ( int j = 0; j < redex.getOuterNames().size(); j++ )
//					permutation.add( new Integer( j ) );
//				
//				updateLists();
//				redexList.pack();
//				redexList.setLayoutData( new RowData( 128, redexList.getBounds().height ) );
//				reactumList.pack();
//				reactumList.setLayoutData( new RowData( 128, reactumList.getBounds().height ) );
//				comp.pack();
//				scroll.setLayoutData( new RowData( comp.getBounds().width, Math.min( comp.getBounds().height + 4, 64 ) ) );
//				
//				Composite linkArrows = new Composite( linkGroup, 0 );
//				Button linkUp = new Button( linkArrows, 0 );
//				Button linkDown = new Button( linkArrows, 0 );
//				linkUp.setText( "Move up" );
//				linkDown.setText( "Move down" );
//				if ( redexNames.size() <= 1 ) {
//					linkUp.setEnabled( false );
//					linkDown.setEnabled( false );
//				}
//				
//				layout = new RowLayout( SWT.VERTICAL );
//				layout.fill = true;
//				linkArrows.setLayout( layout );
//				
//				redexList.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						redexList.setSelection( new int[ 0 ] );
//						updateLists();
//					}
//				} );
//				reactumList.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						select = permutation.indexOf( new Integer( reactumList.getSelectionIndex() ) );
//						updateLists();
//					}
//				} );
//				linkUp.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						int p = permutation.get( select );
//						int i = permutation.indexOf( p - 1 );
//						if ( i >= 0 ) {
//							permutation.set( select, p - 1 );
//							permutation.set( i, p );
//						}
//						updateLists();
//					}
//				} );
//				linkDown.addListener( SWT.Selection, new Listener() {
//					public void handleEvent( Event e )
//					{
//						int p = permutation.get( select );
//						int i = permutation.indexOf( p + 1 );
//						if ( i >= 0 ) {
//							permutation.set( select, p + 1 );
//							permutation.set( i, p );
//						}
//						updateLists();
//					}
//				} );
//			}
//			else {
//				Label linkLabel = new Label( linkGroup, 0 );
//				linkLabel.setText( "Nothing to assign." );
//			}
			
			control.pack();
			setControl( control );
		}
		
		private void updateDetails()
		{
			selection = matchList != null ? matchList.getSelectionIndex() : -1;
			setPageComplete( selection >= 0 );
			
			if ( selection == -1 ) {
				label.setText( "No match selected." );
				canvas.setBackground( new Color( null, 128, 128, 128 ) );
				return;
			}

			label.setText( "Match " + ( selection + 1 ) + "\nTop-level match element: " +
					       matchNames.get( selection ) +
					       "\nClick Finish to apply the reaction rule to this match." );
			label.setLayoutData( new RowData( 256, 64 ) );
			textAssignment.setText(matchNames.get(selection));
			
			ModelObject rr = matches.get( selection).getRoot();
			Rectangle r =  LayoutUtilities.getLayout(((Layoutable)matches.get( selection ).getMappingData().get(rr)));//     .match.get( rr ).reference.getEditPart().getFigure().getBounds() );
//			r.x -= BaseNode.MARGIN;
//			r.y -= BaseNode.MARGIN;
//			r.width  += 2 * BaseNode.MARGIN;
//			r.height += 2 * BaseNode.MARGIN;
			if ( r.x < 0 ) {
				r.width += r.x;
				r.x = 0;
			}
			if ( r.y < 0 ) {
				r.height += r.y;
				r.y = 0;
			}
			Dimension dim = new Dimension (300,300);//target.getDiagram().getEditor().getDiagramDimension( 1.0 );
			if ( r.x + r.width > dim.width )
				r.width = dim.width - r.x;
			if ( r.y + r.height > dim.height )
				r.height = dim.height - r.y;
			
			double scale = Math.min( 1.0, 256.0 / Math.max( r.width, r.height ) );
			//Image image = target.getDiagram().getEditor().getDiagramImage( scale );
			
			r.width = ( int )( ( r.x + r.width ) * scale + 0.5 );
			r.height = ( int )( ( r.y + r.height ) * scale + 0.5 );
			r.x = ( int )( r.x * scale + 0.5 );
			r.y = ( int )( r.y * scale + 0.5 );
			r.width -= r.x;
			r.height -= r.y;
			Rectangle target = new Rectangle( 0, 0, r.width, r.height );
			if ( r.width < 256 )
				target.x += ( 256 - r.width ) / 2;
			if ( r.height < 256 )
				target.y += ( 256 - r.height ) / 2;
			
			GC gcCanvas = new GC( canvas );
			gcCanvas.setAntialias( SWT.ON );
			gcCanvas.setBackground( new Color( null, 128, 128, 128 ) );
			gcCanvas.fillRectangle( 0, 0, 256, 256 );
			//gcCanvas.drawImage( image, r.x, r.y, r.width, r.height, target.x, target.y, target.width, target.height );
			gcCanvas.setForeground( new Color( null, 224, 32, 32 ) );
			gcCanvas.setLineStyle( SWT.LINE_DASH );
			gcCanvas.setLineWidth( 2 );
			//recursiveOutline( gcCanvas, matches.get( selection ), scale, r.x - target.x, r.y - target.y, rr );
			gcCanvas.dispose();
			//image.dispose();
		}
		
//		private void recursiveOutline( GC gc, MatchData m, double scale, int x, int y, ModelObject e )
//		{
//			for ( GraphMatching.Element t : e.children ) {
//				Rectangle r = new Rectangle( m.match.get( t ).reference.getEditPart().getFigure().getBounds() );
//				
//				r.width = ( int )( ( r.x + r.width ) * scale + 0.5 );
//				r.height = ( int )( ( r.y + r.height ) * scale + 0.5 );
//				r.x = ( int )( r.x * scale + 0.5 );
//				r.y = ( int )( r.y * scale + 0.5 );
//				r.width -= r.x;
//				r.height -= r.y;
//				
//				gc.drawRectangle( new org.eclipse.swt.graphics.Rectangle( r.x - x, r.y - y, r.width, r.height ) );
//				recursiveOutline( gc, m, scale, x, y, t );
//			}
//		}
		
		private void updateLists()
		{			
			if ( reactumList != null ) {
				reactumList.setItems( new String[ 0 ] );
				ArrayList< OuterName > names = (ArrayList<OuterName>) reactum.getOuterNames();
				for ( int i = 0; i < names.size(); i++ )
					reactumList.add( names.get( permutation.indexOf( new Integer( i ) ) ).getName() );
				if ( select < 0 || names.size() <= 1 )
					reactumList.setSelection( -1 );
				else
					reactumList.setSelection( permutation.get( select ) );
			}
			
			if ( reactumSiteList != null ) {
				reactumSiteList.setItems( new String[ 0 ] );
				ArrayList< Site > sites = (ArrayList<Site>) reactum.getSites();
				for ( int i = 0; i < redex.getSites().size(); i++ ) {
					int index = sitePermutation.indexOf( new Integer( i ) );
					if ( index < sites.size() )
						reactumSiteList.add( sites.get( index ).getName() );
					else
						reactumSiteList.add( "[No site]" );
				}
				if ( siteSelect < 0 || redex.getSites().size() <= 1 )
					reactumSiteList.setSelection( -1 );
				else
					reactumSiteList.setSelection( sitePermutation.get( siteSelect ) );
			}
		}
	}
	
	private Bigraph target = null;
	private Bigraph redex = null;
	private Bigraph reactum = null;
	private Reaction rule=null;
	private boolean isValid;
	
	private HashMap< OuterName, OuterName > nameMap = new HashMap< OuterName, OuterName >();
	private HashMap< Site, Site > siteMap = new HashMap< Site, Site >();
	private ArrayList< MatchData > matches;
	private MatchData chosenMatch = null;
	
	public ReactionWizard( Reaction rule, Bigraph target )
	{
		this.rule=rule;
		this.target = target;
		redex = rule.getRedex();
		reactum = rule.getReactum();
		
		isValid = !( redex == null || reactum == null);
				    // GraphMatching.testReactionComponent( redex ) != GraphMatching.TEST_OKAY ||
				     //redex.getOuterNames().size() != reactum.getOuterNames().size() ||
				     //reactum.getSites().size() > redex.getSites().size() );
		
		/**TODO test Matching */
//		int testReactum = GraphMatching.testReactionComponent( reactum );

//		if ( ( testReactum & GraphMatching.TEST_HAS_INNER_NAMES ) != 0 || ( testReactum & GraphMatching.TEST_NOT_SINGLE_ROOT ) != 0 )
//			isValid = false;
//		
		
		/** TODO  start Matching algorithm and visualize it*/
		if ( isValid ) {
			matches = PlaceMatch.match(target, redex);
			addPage( new MatchesPage() );
		}
		else
			addPage( new InvalidPage() );
	}
	
	@Override
	public boolean canFinish()
	{
		for ( int i = 0; i < getPages().length; i++ )
			if ( !getPages()[ i ].isPageComplete() )
				return false;
		return isValid;
	}
	
	@Override
	public boolean performFinish()
	{
		ChangeDescriptorGroup cgA = new ChangeDescriptorGroup();
		ChangeDescriptorGroup cgLink = new ChangeDescriptorGroup();
		HashMap<Root,Root> mapReactionRoots= rule.getMapRedexRootToReactum();
		chosenMatch= matches.get(selection);
		
		//inizializeMap;
		rule.initializeMap();
		
		//MatchesPage matchesPage = ( MatchesPage )getPage( "Matches" );
		for (Root root : redex.getRoots()){
			
			Root reactumRoot=mapReactionRoots.get(root);
			Container dest=(Container)chosenMatch.getMappingData().get(root);
			int rand=(int)(Math.random()*100)%10;
			
			for(ModelObject obj: root.getChildren()){
				//cgA.add(((Layoutable)chosenMatch.getMappingData().get(obj)).changeRemove());
				if(!(obj instanceof Site)){
					eraseNodeandChild(((Layoutable)chosenMatch.getMappingData().get(obj)),cgA);
				}else{
					for (ModelObject mo:chosenMatch.getSiteMapping(obj) ){
						eraseNodeandChild((Layoutable)mo,cgA);
					}
				}
			}
			
			for(Layoutable child: reactumRoot.getChildren()){
				//cgA.add(dest.changeAddChild(child.clone(null), child.getName()+ rand));
				fillAddChangeItemReactum(child, dest, cgA,cgLink);
			}
		
		}
		//cgA.add(ExtendedDataUtilities.relayout(target));
		//System.out.println(cgA);
		//System.out.println(cgLink);
		
		if (cgA.size() != 0){
			try {
				DescriptorExecutorManager.getInstance().tryApplyChange(cgA);
			} catch (ChangeCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		if (cgLink.size() != 0){
			try {
				DescriptorExecutorManager.getInstance().tryApplyChange(cgLink);
				cgA.clear();
				cgA.add(LayoutUtilities.relayout(target));
				DescriptorExecutorManager.getInstance().tryApplyChange(cgA);
				
//				Rectangle rectTest=LayoutUtilities.getLayout(target);
//				System.out.println(rectTest);
//				Point mainBigraphBottomRight= rectTest.getBottomRight();
//				
//				for (Root root :target.getRoots()){
//					Rectangle rect=LayoutUtilities.getLayout(root);
//					//Point topLeft= rect.getTopLeft();
//					Point bottomRight = rect.getBottomRight();
//					if (mainBigraphBottomRight.x < bottomRight.x){
//						rectTest.setWidth(bottomRight.x + 10);
//					}
//					if (mainBigraphBottomRight.y < bottomRight.y){
//						rectTest.setHeight(bottomRight.y + 10);
//					}	
//				}
					
				
			} catch (ChangeCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		//OLD
//		ArrayList< OuterName > redexNames = (ArrayList<OuterName>) redex.getOuterNames();
//		ArrayList< OuterName > reactumNames = (ArrayList<OuterName>) reactum.getOuterNames();
//		
//		for ( int i = 0; i < redexNames.size(); i++ )
//			nameMap.put( redexNames.get( i ), reactumNames.get( matchesPage.permutation.indexOf( new Integer( i ) ) ) );
//		
//		ArrayList< Site > redexSites = (ArrayList<Site>) redex.getSites();
//		ArrayList< Site > reactumSites = (ArrayList<Site>) reactum.getSites();
//		
//		for ( int i = 0; i < redexSites.size(); i++ ) {
//			int index = matchesPage.sitePermutation.indexOf( new Integer( i ) );
//			if ( index < reactumSites.size() )
//				siteMap.put( redexSites.get( i ), reactumSites.get( index ) );
//			else
//				siteMap.put( redexSites.get( i ), null );
//		}
//		chosenMatch = matches.get( matchesPage.selection );
		
		
		
		LayoutUtilities.relayout(target);
		return true;
	}
	
	public HashMap< OuterName, OuterName > getNameMap()
	{
		return nameMap;
	}
	
	public HashMap< Site, Site > getSiteMap()
	{
		return siteMap;
	}
	
	public MatchData getMatchData()
	{
		return chosenMatch;
	}
	
		
	private HashMap <Link,Link> mapEdges =new HashMap<Link,Link>();
		
		
		private void fillAddChangeReactum(Root rootReactum, Layoutable itemAgent,
				ChangeDescriptorGroup cg, MatchData ma) {
	
			for (Layoutable l : rootReactum.getChildren()) {
				if (itemAgent instanceof Container) {
					
				}
	
			}
	
		}
		
	@Deprecated
	private static final void setLayout(Layoutable l, Rectangle r) {
		l.setExtendedData(LayoutUtilities.LAYOUT, r);
	}
		
	private void fillAddChangeItemReactum(Layoutable l, Layoutable itemAgent,
			ChangeDescriptorGroup cg, ChangeDescriptorGroup cgL) {

		if (l instanceof Site) {
			addSite((Site)l, itemAgent, cg, cgL);
		}else{

			Layoutable newNodeAgent = (Layoutable) l.newInstance();

			if (rule.getRedexMapName().containsKey(l.getName())) {

				// same element in the redex
				Layoutable la = (Layoutable) getMatchData().getMappingData()
						.get(rule.getRedexMapName().get(l.getName()));
				System.out.println("redex name: "
						+ rule.getRedexMapName().get(l.getName()));
				System.out.println("agent name: " + la.getName());

				cg.add(((Container) itemAgent).changeAddChild(newNodeAgent,
						la.getName() + ""));
				setLayout(newNodeAgent, LayoutUtilities.getLayout(l));

			} else {
				// element in reactum is non contained i redex
				cg.add(((Container) itemAgent).changeAddChild(newNodeAgent,
						l.getName() + ((int) (Math.random() * 100)) + "'"));
				setLayout(newNodeAgent, LayoutUtilities.getLayout(l));

			}
			if (l instanceof Container) {
				for (Layoutable son : ((Container) l).getChildren()) {
					fillAddChangeItemReactum(son, newNodeAgent, cg, cgL);
				}
			}

			/* fare il collegamento dei link */
			/* e aggiungere nuovi edge nel caso */
			HashMap<Link, Link> mapLinksRule = rule.getMapLinksReactumRedex();

			if (((Node) l).getPorts().size() > 0) {
				for (Port p : ((Node) l).getPorts()) {
					Port pOnNewNode = ((Node) newNodeAgent)
							.getPort(p.getName());

					Link link = p.getLink();
					if (mapLinksRule.containsKey(link)) {
						Link lagent = chosenMatch.getLinkMap().get(
								mapLinksRule.get(link));
						/* XXX: untested PointChange replacement! */
						cgL.add(new BoundDescriptor(target,
								new Point.ChangeConnectDescriptor(
										pOnNewNode.getIdentifier(),
										lagent.getIdentifier())));
					} else {
						Edge edge;
						if (mapEdges.containsKey(link)) {
							edge = (Edge) mapEdges.get(link);

						} else {
							edge = new Edge();
							cg.add(target.changeAddChild(edge, "e"
									+ ((int) (Math.random() * 100))));
							mapEdges.put(link, edge);
						}
						/* XXX: untested PointChange replacement! */
						cgL.add(new BoundDescriptor(target,
								new Point.ChangeConnectDescriptor(
										pOnNewNode.getIdentifier(),
										edge.getIdentifier())));
					}

					// ((Node)newNodeAgent).getPort(p.getName());
				}
			}
		}
	}
		

		
	private void addSite(Site siteReactum, Layoutable agentParent, ChangeDescriptorGroup cgAdd,
			ChangeDescriptorGroup cgRem) {
		
		Site redexS= rule.mapReactumSiteToRedex.get(siteReactum);
		ArrayList<Layoutable> matches= new ArrayList<Layoutable>();
		for (ModelObject obj : chosenMatch.getSiteMapping(redexS)){
			matches.add((Layoutable)obj);
		}
		switch(rule.mapRedexSiteSon.get(redexS)){
			case 0:
				// delete (1 site in redex -> 0 site in reactum)
				System.out.println("add no site");
				//eraseNodeandChild
				break;
				
			case 1:
				// no biection on site (1 site in redex -> 1 site in reactum) no
				// renaming of controls
				//addOneSite()
				System.out.println("add one site");
				addOneSite(matches,agentParent,cgAdd,cgRem);
				break;
				
			default:
				// biection more site in reactum (1 site in redex -> * in reactum)
				// renaming of controls
				System.out.println("add more sites");
				addOneSiteRenaming(matches,agentParent,cgAdd,cgRem);				
				break;

		}		

	}
		
	private void removeSite(ArrayList<Layoutable> agentMatchedComponets,
			ChangeDescriptorGroup cg) {
		for (Layoutable match : agentMatchedComponets) {
			eraseNodeandChild(match, cg);
		}
	}
		
		
	private void addOneSite(ArrayList<Layoutable> agentMatchedComponets,
			Layoutable newParentAgent, ChangeDescriptorGroup cgAdd, ChangeDescriptorGroup cgRem) {
		for (Layoutable match : agentMatchedComponets) {
			Layoutable newNodeAgent = (Layoutable) match.newInstance();
			String name = match.getName();
			//newNodeAgent.changeName(name + ((int) (Math.random() * 100)));
			
			cgAdd.add(((Container) newParentAgent).changeAddChild(newNodeAgent,name));
			
			Rectangle parentRect= LayoutUtilities.getLayout(newParentAgent);
			Rectangle rect=LayoutUtilities.getLayout(match);
			
			rect.width=(rect.width < parentRect.width) ? rect.width : (parentRect.width*2)/3;
			rect.height=(rect.height < parentRect.height) ? rect.height : (parentRect.height*2)/3;
			setLayout(newNodeAgent, new Rectangle(parentRect.x+1,parentRect.y+1,rect.width,rect.height));
					
			
			
			if (match instanceof Container) {
				addOneSite(
						(ArrayList<Layoutable>) ((Container) match)
								.getChildren(),
						newNodeAgent, cgAdd, cgRem);
			}

			if (((Node) match).getPorts().size() > 0) {
				for (Port p : ((Node) match).getPorts()) {
					Port pOnNewNode = ((Node) newNodeAgent)
							.getPort(p.getName());

					Link link = p.getLink();

					/* XXX: untested PointChange replacement! */
					cgRem.add(new BoundDescriptor(target,
							new Point.ChangeConnectDescriptor(
									pOnNewNode.getIdentifier(),
									link.getIdentifier())));

					// ((Node)newNodeAgent).getPort(p.getName());
				}

			}

		}
	}

	private void addOneSiteRenaming(
			ArrayList<Layoutable> agentMatchedComponets,
			Layoutable newParentAgent, ChangeDescriptorGroup cgAdd, ChangeDescriptorGroup cgRem) {
		for (Layoutable match : agentMatchedComponets) {
			Layoutable newNodeAgent = (Layoutable) match.newInstance();
			String name = match.getName();
			name=name + ((int) (Math.random() * 100));
			
			//newNodeAgent.changeName(name + ((int) (Math.random() * 100)));
			
			cgAdd.add(((Container) newParentAgent).changeAddChild(newNodeAgent,name));
			setLayout(newNodeAgent, LayoutUtilities.getLayout(match));
			
			

			if (match instanceof Container) {
				addOneSite(
						(ArrayList<Layoutable>) ((Container) match)
								.getChildren(),
						newNodeAgent, cgAdd, cgRem);
			}

			if (((Node) match).getPorts().size() > 0) {
				for (Port p : ((Node) match).getPorts()) {
					Port pOnNewNode = ((Node) newNodeAgent)
							.getPort(p.getName());

					Link link = p.getLink();

					/* XXX: untested PointChange replacement! */
					cgRem.add(new BoundDescriptor(target,
							new Point.ChangeConnectDescriptor(
									pOnNewNode.getIdentifier(),
									link.getIdentifier())));

					// ((Node)newNodeAgent).getPort(p.getName());
				}

			}
		}

	}
		
		
		
		
		private void eraseNodeandChild(Layoutable node, ChangeDescriptorGroup cg){
			
			
			if(node instanceof Container){
				for(Layoutable son: ((Container) node).getChildren()){
					eraseNodeandChild(son, cg);
				}
			} 
			
			//disconnect node
			if (node instanceof Node){
				for (Port p :((Node) node).getPorts()){
					System.out.println("get port");
					
						/* XXX: untested PointChange replacement! */
						cg.add(new BoundDescriptor(target,
								new Point.ChangeDisconnectDescriptor(
										p.getIdentifier(),
										p.getLink().getIdentifier())));

					
				}
				
				cg.add(node.changeRemove());
			}	
		}
		
}
