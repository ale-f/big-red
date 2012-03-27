package it.uniud.bigredit.figure;

import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;



public class NestedBigraphFigure extends AbstractFigure {
	
	public static final int DEF_WIDTH  = 256;
	public static final int DEF_HEIGHT = 256;

	private Label name = new Label() {
		@Override
		public boolean containsPoint( int x, int y )
		{
			return false;
		}
	};
	private XYLayout layout;

	public NestedBigraphFigure() {
		super();
		
		name.setForegroundColor(ColorConstants.black);
		add(name, 0);
		
		setConstraint(name, new Rectangle(100, 100, 100, 100));
	 
		setForegroundColor(ColorConstants.black);
		setBackgroundColor(ColorConstants.white);
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
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.fillRoundRectangle(a, 20, 20);
		} finally {
			stop(graphics);
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setLineStyle(SWT.LINE_DOT);
			a.width--; a.height--;
			graphics.drawRoundRectangle(a, 20, 20);
			//graphics.drawLine(0, 100, 200, 100);
		} finally {
			stop(graphics);
		}
	}


	
}
