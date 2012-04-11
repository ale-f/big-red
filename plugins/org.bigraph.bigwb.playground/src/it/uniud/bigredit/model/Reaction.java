package it.uniud.bigredit.model;


import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;






public class Reaction  extends ModelObject{
	
	public static final int GAP_WIDTH = 96;
	
	private Bigraph redex = null;
	private Bigraph reactum = null;
	
	public Bigraph getRedex()
	{
		return redex;
	}
	
	public Bigraph getReactum()
	{
		return reactum;
	}


	
	/*@Override
	public boolean addChild( BaseNode child )
	{
		if ( child instanceof Graph ) {
			if ( redex == null )
				redex = ( Graph )child;
			else if ( reactum == null )
				reactum = ( Graph )child;
		}
		return super.addChild( child );
	}
	
	public boolean addChild( BaseNode child, boolean isReactum )
	{
		if ( child instanceof Graph ) {
			if ( reactum == null )
				reactum = ( Graph )child;
			else if ( redex == null )
				redex = ( Graph )child;
		}
		return super.addChild( child );
	}
	
	@Override
	public boolean removeChild( BaseNode child )
	{
		if ( child instanceof Graph ) {
			if ( child == redex )
				redex = null;
			if ( child == reactum )
				reactum = null;
		}
		return super.removeChild( child );
	}
	
	@Override
	public void organiseLayout( HashMap< BaseNode, Rectangle > old )
	{
		for ( BaseNode t : getChildren() )
			t.organiseLayout( old );
		if ( !old.containsKey( this ) )
			old.put( this, new Rectangle( getLayout() ) );
		Rectangle r1;
		Rectangle r2;
		if ( redex != null ) {
			if ( !old.containsKey( redex ) )
				old.put( redex, redex.getLayout() );
			r1 = new Rectangle( redex.getLayout() );
		}
		else
			r1 = new Rectangle( 0, 0, GAP_WIDTH, GAP_WIDTH );
		if ( reactum != null ) {
			if ( !old.containsKey( reactum ) )
				old.put( reactum, reactum.getLayout() );
			r2 = new Rectangle( reactum.getLayout() );
		}
		else
			r2 = new Rectangle( 0, 0, GAP_WIDTH, GAP_WIDTH );
		
		int width = r1.width + r2.width + GAP_WIDTH + 4 * MARGIN;
		int height = Math.max( r1.height, r2.height ) + 4 * MARGIN;
		
		Rectangle r = new Rectangle( getLayout() );
		if ( r.width != width ) {
			r.x += ( r.width - width ) / 2;
			r.width = width;
		}
		if ( r.height != height ) {
			r.y += ( r.height - height ) / 2;
			r.height = height;
		}
		
		r1.x = 2 * MARGIN;
		r1.y = 2 * MARGIN + ( Math.max( r1.height, r2.height ) - r1.height ) / 2;
		r2.x = 2 * MARGIN + r1.width + GAP_WIDTH;
		r2.y = 2 * MARGIN + ( Math.max( r1.height, r2.height ) - r2.height ) / 2;
		setLayout( r );
		if ( redex != null )
			redex.setLayout( r1 );
		if ( reactum != null )
			reactum.setLayout( r2 );
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Reaction reaction = new Reaction();
		reaction.setName( getName() );
		reaction.setDescription( getDescription() );
		reaction.setLayout( new Rectangle( getLayout() ) );

		for ( BaseNode base : getChildren() ) {
			BaseNode clone = ( BaseNode )base.clone();
			if ( redex == null && reactum != null )
				reaction.addChild( clone, true );
			else
				reaction.addChild( clone );
			clone.setLayout( base.getLayout() );
		}
		
		return reaction;
	}
	
	@Override
	public void copyName()
	{
		setName( getCopyName( Reaction.class ) );
	}
	
	@Override
	public void save( Writer out ) throws IOException
	{
		out.start( "reaction" );
		out.write( "prop-name", getName() );
		out.write( "prop-desc", getDescription() );
		out.write( "prop-layout", getLayout() );
		out.write( "prop-reactum", redex == null && reactum != null );
		super.save( out );
		out.end();
	}*/

}