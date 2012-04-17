package it.uniud.bigredit.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import dk.itu.big_red.editors.bigraph.figures.AbstractFigure;
import dk.itu.big_red.utilities.ui.UI;

public class BRSFigure extends AbstractFigure  {

	private XYLayout layout;

	public BRSFigure()
	{
		layout = new XYLayout();
		setLayoutManager( layout );
	}
	
	@Override
	protected void outlineShape(Graphics g) {
		Rectangle r = start(g);
		int width = r.width;
		try {
			g.setAlpha(80);
			
			g.setLineStyle(SWT.LINE_CUSTOM);
			
			g.setFont(UI.tweakFont(g.getFont(), 8, SWT.ITALIC));
			
			g.getFont().dispose();
		} finally {
			stop(g);
		}
	}
	
	
	/*public void setLayout( Rectangle rect )
	{
		setBounds( rect );
	}*/
	
	public void setName( String text )
	{
	}
	
	
	public void setShowLabel( boolean show )
	{
	}

}