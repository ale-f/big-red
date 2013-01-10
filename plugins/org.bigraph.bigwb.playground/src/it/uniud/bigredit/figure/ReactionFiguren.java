package it.uniud.bigredit.figure;

import it.uniud.bigredit.model.Reaction;

import org.bigraph.model.Bigraph;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.utilities.ui.UI;

public class ReactionFiguren extends AbstractFigure {//extends RoundedRectangle  {
	
	public static final int MARGIN = 10;
	public static final int DEF_WIDTH  = Reaction.MIN_WIDTH_BIG ;
	public static final int DEF_HEIGHT = Reaction.MIN_HIGHT_BIG;
	
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
		 
			setForegroundColor(ColorConstants.orange);
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
				graphics.setForegroundColor(ColorConstants.black);
				graphics.setFont(UI.tweakFont(graphics.getFont(), 8, SWT.ITALIC));
				graphics.setAlpha(255);
				graphics.drawText("redex",a.width/4,10);				
				graphics.drawText("reactum",(a.width/4)*3,10);
				graphics.setForegroundColor(ColorConstants.orange);
				
				
				//graphics.setLineStyle(SWT.LINE_DASH);
				
				//draw arrow
				int arrowLength = 15;
				int arrowLine = 20;
				int arrowHigh =10;
				Point a1= new Point(a.width/2-arrowLine, a.height/2);
				Point a2= new Point(a.width/2, a.height/2);
				Point a3= new Point(a.width/2, a.height/2+arrowHigh);
				Point a4= new Point(a.width/2+arrowLength, a.height/2);
				Point a5= new Point(a.width/2, a.height/2-arrowHigh);
				Point a6= new Point(a.width/2, a.height/2);
				graphics.drawLine(a1, a2);
				graphics.drawLine(a2, a3);
				graphics.drawLine(a3, a4);
				graphics.drawLine(a4, a5);
				graphics.drawLine(a5, a6);
				
				graphics.setAlpha(150);
				graphics.setLineStyle(SWT.LINE_DOT);
				a.width--; a.height--;
				graphics.drawRoundRectangle(a, 20, 20);
				
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
