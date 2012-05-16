package it.uniud.bigredit.figure;

import it.uniud.bigredit.model.Reaction;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;



public class ReactionFigure extends RoundedRectangle  {
	
	public static final int MARGIN= 10;
	
	public static final int DEF_WIDTH  = Reaction.GAP_WIDTH * 3 + MARGIN * 4;
	public static final int DEF_HEIGHT = Reaction.GAP_WIDTH + MARGIN * 4;
	
	private Bigraph reactum = null;
	private Bigraph redex = null;
	private PolylineConnection arrow = null;
	
	private boolean showLabels = false;

	private Label name = new Label() {
		@Override
		public boolean containsPoint( int x, int y )
		{
			return false;
		}
	};
	private Label redexLabel = new Label() {
		@Override
		public boolean containsPoint( int x, int y )
		{
			return false;
		}
	};
	private Label reactumLabel = new Label() {
		@Override
		public boolean containsPoint( int x, int y )
		{
			return false;
		}
	};
	private XYLayout layout;

	public ReactionFigure()
	{
		layout = new XYLayout();
		setLayoutManager( layout );
		name.setForegroundColor( ColorConstants.black );
		name.setTextAlignment( PositionConstants.CENTER );
		redexLabel.setForegroundColor( new Color( null, 224, 32, 32 ) );
		redexLabel.setTextAlignment( PositionConstants.CENTER );
		redexLabel.setText( "Redex" );
		reactumLabel.setForegroundColor( new Color( null, 224, 32, 32 ) );
		reactumLabel.setTextAlignment( PositionConstants.CENTER );
		reactumLabel.setText( "Reactum" );
		add( redexLabel, new Rectangle( 0, 0, -1, -1 ) );
		add( reactumLabel, new Rectangle( 0, 0, -1, -1 ) );
		
		arrow = new PolylineConnection();
		arrow.setForegroundColor( new Color( null, 224, 32, 32 ) );
		arrow.setLineWidth( 2 );
		arrow.setAntialias( SWT.ON );
		add( arrow );
		//setShowLabel( ToggleLabelsAction.isToggled() );
		
		setLineWidth( 1 );
		setForegroundColor( new Color( null, 224, 32, 32 ) );
		setLineStyle( SWT.LINE_SOLID );
		setCornerDimensions( new Dimension( 8, 8 ) );
		setAntialias( SWT.ON );
	}
	
	
	public void setLayout( Rectangle rect )
	{
		setLayout( rect, true );
	}
	
	public void setLayout( Rectangle rect, boolean updateLabels )
	{
		getParent().setConstraint( this, rect );
		getParent().setConstraint( name, new Rectangle( rect.x, rect.y - 16, rect.width, 16 ) );
		if ( updateLabels )
			updateLabels();
	}
	
	public void setChildren( Bigraph redex,Bigraph reactum )
	{
		this.redex   = redex;
		this.reactum = reactum;
		updateLabels();
	}
	
	public void updateLabels()
	{
		int l = 0;
		int h = 0;
		
		if ( redex != null ) {
			Rectangle r = new Rectangle( ExtendedDataUtilities.getLayout(redex) );
			h = r.height;
			r.y += r.height;
			r.height = 16;
			l = 4 * MARGIN + r.width;
			setConstraint( redexLabel, r );
		}
		else {
			h = Reaction.GAP_WIDTH;
			l = 4 * MARGIN + Reaction.GAP_WIDTH;
		}
		
		if ( reactum != null ) {
			Rectangle r = new Rectangle( ExtendedDataUtilities.getLayout(reactum) );
			h = Math.max( h, r.height );
			r.y += r.height;
			r.height = 16;
			setConstraint( reactumLabel, r );
		}
		else {
			h = Math.max( h, Reaction.GAP_WIDTH );
		}
		
		int r = l - 4 * MARGIN + Reaction.GAP_WIDTH;
		redexLabel.setVisible( redex != null && showLabels );
		reactumLabel.setVisible( reactum != null && showLabels );
		
		if ( getParent() == null )
			return;
		Rectangle rect = ( Rectangle )getParent().getLayoutManager().getConstraint( this );
		arrow.setEndpoints( new Point( rect.x + l, rect.y + MARGIN * 2 + h / 2 ),
	                        new Point( rect.x + r, rect.y + MARGIN * 2 + h / 2 ) );
		PolygonDecoration d = new PolygonDecoration();
		d.setAntialias( SWT.ON );
		arrow.setTargetDecoration( d );
		if ( rect != null )
			setLayout( rect, false );
	}
	
	@Override
	public void setParent( IFigure p )
	{
		if ( getParent() != null )
			getParent().remove( name );
		super.setParent( p );
		if ( getParent() != null )
			getParent().add( name );
	}
	
	
	public void setName( String text )
	{
		name.setText( text );
	}
	
	
	public void setShowLabel( boolean show )
	{
		showLabels = show;
		name.setVisible( show );
		updateLabels();
	}
	
}
