package it.uniud.bigredit.editparts;

import it.uniud.bigredit.figure.SitePlusFigure;

import org.eclipse.draw2d.IFigure;


import dk.itu.big_red.editors.bigraph.parts.SitePart;

public class SitePlusPart extends SitePart{
	
	@Override
	protected IFigure createFigure() {
		return new SitePlusFigure();
	}

	
	
	
}
