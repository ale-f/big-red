package it.uniud.bigredit.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;



public class NestedBigraphFigure extends AbstractFigure {
	
	public static final int DEF_WIDTH  = 256;
	public static final int DEF_HEIGHT = 256;

	private int innerLine = 0;
	private int outerLine = 0;
	
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
		
		//setConstraint(name, new Rectangle(100, 100, 100, 100));
	 
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
			
			graphics.setAlpha(100);
			
			//graphics.setLineStyle(SWT.LINE_DASH);
			
			graphics.setLineStyle(SWT.LINE_DOT);
			a.width--; a.height--;
			graphics.drawRoundRectangle(a, 20, 20);
			if(innerLine != 0){
				graphics.drawLine(0, innerLine, a.width, innerLine);
				graphics.drawText("inner name boundary", 10, innerLine - 14);
			}
			if(outerLine != 0){
				graphics.drawLine(0, outerLine, a.width, outerLine);
				graphics.drawText("outer name boundary", 10, outerLine + 2 );
			}
			
		} finally {
			stop(graphics);
		}
	}





	public int getInnerLine() {
		return innerLine;
	}





	public void setInnerLine(int innerLine) {
		this.innerLine = innerLine;
	}





	public int getOuterLine() {
		return outerLine;
	}





	public void setOuterLine(int outerLine) {
		this.outerLine = outerLine;
	}


	
}
