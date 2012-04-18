package it.uniud.bigredit.figure;

import it.uniud.bigredit.model.Reaction;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
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






import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.model.Bigraph;

public class ReactionFiguren extends AbstractFigure {//extends RoundedRectangle  {
	
	public static final int MARGIN = 200;
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


		private int innerLine = 0;
		private int outerLine = 0;
		

		private XYLayout layout;

		public ReactionFiguren() {
			super();
			
			name.setForegroundColor(ColorConstants.black);
			add(name, 0);
			
			//setConstraint(name, new Rectangle(100, 100, 100, 100));
		 
			setForegroundColor(ColorConstants.red);
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
				
				graphics.setAlpha(63);
				
				//graphics.setLineStyle(SWT.LINE_DASH);
				
				graphics.setLineStyle(SWT.LINE_DOT);
				a.width--; a.height--;
				graphics.drawRoundRectangle(a, 20, 20);
				if(innerLine != 0){
					graphics.drawLine(0, innerLine, a.width, innerLine);
				}
				if(outerLine != 0){
					graphics.drawLine(0, outerLine, a.width, outerLine);
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
