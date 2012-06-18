package it.uniud.bigredit;

import java.util.ArrayList;
import java.util.HashMap;

import org.bigraph.model.Bigraph;
import org.bigraph.model.InnerName;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.uniud.bigraph.match.PlaceMatch;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;





public class CompositionWizard extends Wizard {
	
	class InvalidPage extends WizardPage {
		
		private Bigraph inner;
		private Bigraph outer;
		
		public InvalidPage( Bigraph inner, Bigraph outer )
		{
			super( "Invalid" );
			
			this.inner = inner;
			this.outer = outer;
		}
		
		@Override
		public void createControl( Composite parent )
		{
			ArrayList< Site > sites = (ArrayList<Site>) outer.getSites();
			ArrayList< Root > roots = (ArrayList<Root>) inner.getRoots();
			ArrayList< InnerName > innerNames = (ArrayList<InnerName>) outer.getInnerNames();
			ArrayList< OuterName > outerNames = (ArrayList<OuterName>) inner.getOuterNames();
			
			String s = "These graphs cannot be composed. Their interfaces do not match.\n";
			if ( sites.size() != roots.size() )
				s += "\nDropped inner graph has " + ( roots.size() == 0 ? "no" : roots.size() ) + 
				     " root" + ( roots.size() == 1 ? "" : "s" ) + ", but target outer graph has " + ( sites.size() == 0 ? "no" : sites.size() ) +
				     " site" + ( sites.size() == 1 ? "" : "s" ) +  ".";
			if ( innerNames.size() != outerNames.size() )
				s += "\nDropped inner graph has " + ( outerNames.size() == 0 ? "no" : outerNames.size() ) + 
				     " outer name" + ( outerNames.size() == 1 ? "" : "s" ) + ", but target outer graph has " + ( innerNames.size() == 0 ? "no" : innerNames.size() ) +
				     " inner name" + ( innerNames.size() == 1 ? "" : "s" ) + ".";
			
			Composite control = new Composite( parent, 0 );
			Label text = new Label( control, 0 );
			text.setText( s );
			control.setLayout( new RowLayout() );
			setControl( control );
		}
	}

	class ReorderPage extends WizardPage {
		
		private Bigraph inner;
		private Bigraph outer;
		private List rootList;
		private List siteList;
		private List innerNameList;
		private List outerNameList;
		private int rootSelect = -1;
		private int nameSelect = -1;
		private ArrayList< Integer > rootPermutation;
		private ArrayList< Integer > namePermutation;
		
		public ReorderPage( Bigraph inner, Bigraph outer )
		{
			super( "Reorder" );
			
			this.outer = outer;
			this.inner = inner;
		}
		
		@Override
		public void createControl( Composite parent )
		{
			Composite control = new Composite( parent, 0 );
			RowLayout layout = new RowLayout( SWT.VERTICAL );
			layout.fill = true;
			layout.spacing = 16;
			control.setLayout( layout );
			
			Group placeGroup = new Group( control, 0 );
			placeGroup.setText( "Place graph interface assignment" );
			layout = new RowLayout();
			layout.center = true;
			layout.marginWidth = layout.marginHeight = 2;
			layout.spacing = 8;
			placeGroup.setLayout( layout );
			
			if ( outer.getSites().size() > 0 ) {
				ScrolledComposite scroll = new ScrolledComposite( placeGroup, SWT.V_SCROLL | SWT.BORDER );
				Composite comp = new Composite( scroll, 0 );
				scroll.setContent( comp );
				
				layout = new RowLayout();
				layout.center = true;
				layout.marginWidth = layout.marginHeight = 2;
				layout.spacing = 16;
				comp.setLayout( layout );
				
				siteList = new List( comp, SWT.BORDER | SWT.SINGLE );
				Label placeLabel = new Label( comp, 0 );
				placeLabel.setText( ">" );
				placeLabel.setAlignment( SWT.CENTER );
				rootList = new List( comp, SWT.BORDER | SWT.SINGLE );
				
				ArrayList< Site > sites = (ArrayList<Site>) outer.getSites();
				for ( int i = 0; i < sites.size(); i++ )
					siteList.add( sites.get( i ).getName() );
				rootPermutation = new ArrayList< Integer >();
				for ( int i = 0; i < inner.getRoots().size(); i++ )
					rootPermutation.add( new Integer( i ) );
				
				updateLists();
				siteList.pack();
				siteList.setLayoutData( new RowData( 128, siteList.getBounds().height ) );
				rootList.pack();
				rootList.setLayoutData( new RowData( 128, rootList.getBounds().height ) );
				comp.pack();
				scroll.setLayoutData( new RowData( comp.getBounds().width, Math.min( comp.getBounds().height + 4, 192 ) ) );
				
				Composite placeArrows = new Composite( placeGroup, 0 );
				Button placeUp = new Button( placeArrows, 0 );
				Button placeDown = new Button( placeArrows, 0 );
				placeUp.setText( "Move up" );
				placeDown.setText( "Move down" );
				if ( sites.size() <= 1 ) {
					placeUp.setEnabled( false );
					placeDown.setEnabled( false );
				}
				
				layout = new RowLayout( SWT.VERTICAL );
				layout.fill = true;
				placeArrows.setLayout( layout );
				
				siteList.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						siteList.setSelection( new int[ 0 ] );
						updateLists();
					}
				} );
				rootList.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						rootSelect = rootPermutation.indexOf( new Integer( rootList.getSelectionIndex() ) );
						updateLists();
					}
				} );
				placeUp.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						int p = rootPermutation.get( rootSelect );
						int i = rootPermutation.indexOf( p - 1 );
						if ( i >= 0 ) {
							rootPermutation.set( rootSelect, p - 1 );
							rootPermutation.set( i, p );
						}
						updateLists();
					}
				} );
				placeDown.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						int p = rootPermutation.get( rootSelect );
						int i = rootPermutation.indexOf( p + 1 );
						if ( i >= 0 ) {
							rootPermutation.set( rootSelect, p + 1 );
							rootPermutation.set( i, p );
						}
						updateLists();
					}
				} );
			}
			else {
				Label placeLabel = new Label( placeGroup, 0 );
				placeLabel.setText( "Nothing to assign." );
			}
			
			Group linkGroup = new Group( control, 0 );
			linkGroup.setText( "Link graph interface assignment" );
			layout = new RowLayout();
			layout.center = true;
			layout.marginWidth = layout.marginHeight = 2;
			layout.spacing = 8;
			linkGroup.setLayout( layout );
			
			if ( outer.getInnerNames().size() > 0 ) {
				ScrolledComposite scroll = new ScrolledComposite( linkGroup, SWT.V_SCROLL | SWT.BORDER );
				Composite comp = new Composite( scroll, 0 );
				scroll.setContent( comp );
				
				layout = new RowLayout();
				layout.center = true;
				layout.marginWidth = layout.marginHeight = 2;
				layout.spacing = 16;
				comp.setLayout( layout );
				
				innerNameList = new List( comp, SWT.BORDER | SWT.SINGLE );
				Label linkLabel = new Label( comp, 0 );
				linkLabel.setText( ">" );
				linkLabel.setAlignment( SWT.CENTER );
				outerNameList = new List( comp, SWT.BORDER | SWT.SINGLE );
				
				ArrayList< InnerName > innerNames = (ArrayList<InnerName>) outer.getInnerNames();
				for ( int i = 0; i < innerNames.size(); i++ )
					innerNameList.add( innerNames.get( i ).getName() );
				namePermutation = new ArrayList< Integer >();
				for ( int i = 0; i < inner.getOuterNames().size(); i++ )
					namePermutation.add( new Integer( i ) );
				
				updateLists();
				innerNameList.pack();
				innerNameList.setLayoutData( new RowData( 128, innerNameList.getBounds().height ) );
				outerNameList.pack();
				outerNameList.setLayoutData( new RowData( 128, outerNameList.getBounds().height ) );
				comp.pack();
				scroll.setLayoutData( new RowData( comp.getBounds().width, Math.min( comp.getBounds().height + 4, 192 ) ) );
				
				Composite linkArrows = new Composite( linkGroup, 0 );
				Button linkUp = new Button( linkArrows, 0 );
				Button linkDown = new Button( linkArrows, 0 );
				linkUp.setText( "Move up" );
				linkDown.setText( "Move down" );
				if ( innerNames.size() <= 1 ) {
					linkUp.setEnabled( false );
					linkDown.setEnabled( false );
				}
				
				layout = new RowLayout( SWT.VERTICAL );
				layout.fill = true;
				linkArrows.setLayout( layout );
				
				innerNameList.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						innerNameList.setSelection( new int[ 0 ] );
						updateLists();
					}
				} );
				outerNameList.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						nameSelect = namePermutation.indexOf( new Integer( outerNameList.getSelectionIndex() ) );
						updateLists();
					}
				} );
				linkUp.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						int p = namePermutation.get( nameSelect );
						int i = namePermutation.indexOf( p - 1 );
						if ( i >= 0 ) {
							namePermutation.set( nameSelect, p - 1 );
							namePermutation.set( i, p );
						}
						updateLists();
					}
				} );
				linkDown.addListener( SWT.Selection, new Listener() {
					public void handleEvent( Event e )
					{
						int p = namePermutation.get( nameSelect );
						int i = namePermutation.indexOf( p + 1 );
						if ( i >= 0 ) {
							namePermutation.set( nameSelect, p + 1 );
							namePermutation.set( i, p );
						}
						updateLists();
					}
				} );
			}
			else {
				Label linkLabel = new Label( linkGroup, 0 );
				linkLabel.setText( "Nothing to assign." );
			}
			
			updateLists();
			control.pack();
			setControl( control );
		}
		
		private void updateLists()
		{
			if ( rootList != null ) {
				rootList.setItems( new String[ 0 ] );
				ArrayList< Root > roots = (ArrayList<Root>) inner.getRoots();
				for ( int i = 0; i < roots.size(); i++ )
					rootList.add( roots.get( rootPermutation.indexOf( new Integer( i ) ) ).getName() );
				if ( rootSelect < 0 || roots.size() <= 1 )
					rootList.setSelection( -1 );
				else
					rootList.setSelection( rootPermutation.get( rootSelect ) );
			}
			
			if ( outerNameList != null ) {
				outerNameList.setItems( new String[ 0 ] );
				ArrayList< OuterName > outerNames = (ArrayList<OuterName>) inner.getOuterNames();
				for ( int i = 0; i < outerNames.size(); i++ )
					outerNameList.add( outerNames.get( namePermutation.indexOf( new Integer( i ) ) ).getName() );
				if ( nameSelect < 0 || outerNames.size() <= 1 )
					outerNameList.setSelection( -1 );
				else
					outerNameList.setSelection( namePermutation.get( nameSelect ) );
			}
		}
	}
	
	private boolean isValid;
	private HashMap< Site, Root > placeMap = new HashMap< Site, Root >();
	private HashMap< InnerName, OuterName > linkMap = new HashMap< InnerName, OuterName >();
	
	public CompositionWizard( Bigraph inner, Bigraph outer )
	{
		isValid = inner.getOuterNames().size() == outer.getInnerNames().size() &&
		          inner.getRoots().size()      == outer.getSites().size();
		if ( isValid )
			addPage( new ReorderPage( inner, outer ) );
		else
			addPage( new InvalidPage( inner, outer ) );
	}
	
	@Override
	public boolean canFinish()
	{
		return isValid;
	}
	
	@Override
	public boolean performFinish()
	{		
		
		System.out.println("perform Finish");
		ReorderPage reorder = ( ReorderPage )getPage( "Reorder" );
		//testing matching
		//PlaceMatch.match(reorder.outer, reorder.inner);
		
		ArrayList< Site > sites = (ArrayList<Site>) reorder.outer.getSites();
		ArrayList< Root > roots = (ArrayList<Root>) reorder.inner.getRoots();
		ArrayList< InnerName > innerNames = (ArrayList<InnerName>) reorder.outer.getInnerNames();
		ArrayList< OuterName > outerNames = (ArrayList<OuterName>) reorder.inner.getOuterNames();
		
		for ( int i = 0; i < sites.size(); i++ )
			placeMap.put( sites.get( i ), roots.get( reorder.rootPermutation.indexOf( new Integer( i ) ) ) );
		for ( int i = 0; i < innerNames.size(); i++ )
			linkMap.put( innerNames.get( i ), outerNames.get( reorder.namePermutation.indexOf( new Integer( i ) ) ) );
		return true;
	}
	
	public HashMap< Site, Root > getPlaceMap()
	{
		return placeMap;
	}
	public HashMap< InnerName, OuterName > getLinkMap()
	{
		return linkMap;
	}
}
