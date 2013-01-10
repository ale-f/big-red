package it.uniud.bigredit.figure;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;

public class GraphFigure  extends AbstractFigure{
	
	public static final int DEF_WIDTH  = 256;
	public static final int DEF_HEIGHT = 256;

	/**
	 * The width and height radii applied to each corner.
	 * 
	 * @deprecated Use {@link #getCornerDimensions()} instead.
	 */
	protected Dimension corner = new Dimension(8, 8);
	
	private Label name = new Label() {
		@Override
		public boolean containsPoint( int x, int y )
		{
			return false;
		}
	};
	private XYLayout layout;

	public GraphFigure()
	{
		layout = new XYLayout();
		setLayoutManager( layout );
		name.setForegroundColor( ColorConstants.black );
		name.setTextAlignment( PositionConstants.CENTER );
		
		
		setLineWidth( 1 );
		setForegroundColor( ColorConstants.black );
		setBackgroundColor( ColorConstants.white );
		setLineStyle( SWT.LINE_SOLID );
		corner =  new Dimension( 8, 8 ) ;
		setAntialias( SWT.ON );
	}
	
	
	public void setLayout( Rectangle rect )
	{
		getParent().setConstraint( this, rect );
		getParent().setConstraint( name, new Rectangle( rect.x, rect.y - 16, rect.width, 16 ) );
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
		name.setVisible( show );
	}
	
	
	
	/**
	 * @see Shape#fillShape(Graphics)
	 */
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.fillRoundRectangle(getBounds(), corner.width, corner.height);
	}

	/**
	 * @see Shape#outlineShape(Graphics)
	 */
	@Override
	protected void outlineShape(Graphics graphics) {
		float lineInset = Math.max(1.0f, getLineWidthFloat()) / 2.0f;
		int inset1 = (int) Math.floor(lineInset);
		int inset2 = (int) Math.ceil(lineInset);

		Rectangle r = Rectangle.SINGLETON.setBounds(getBounds());
		r.x += inset1;
		r.y += inset1;
		r.width -= inset1 + inset2;
		r.height -= inset1 + inset2;

		graphics.drawRoundRectangle(r,
				Math.max(0, corner.width - (int) lineInset),
				Math.max(0, corner.height - (int) lineInset));
	}

	/*@SuppressWarnings( "unchecked" )
	public void sortChildren()
	{
		ArrayList< IFigure > t = new ArrayList< IFigure >();
		ArrayList< Rectangle > rt = new ArrayList< Rectangle >();
		for ( Object o : new ArrayList( getChildren() ) ) {
			if ( o instanceof EdgeFigure ) {
				rt.add( ( Rectangle )getLayoutManager().getConstraint( ( IFigure )o ) );
				remove( ( IFigure )o );
				t.add( ( IFigure )o );
			}
		}
		for ( int i = 0; i < t.size(); i++ ) {
			add( t.get( i ) );
			( ( EdgeFigure )t.get( i ) ).setLayout( rt.get( i ) );
		}
	}*/
	
}
