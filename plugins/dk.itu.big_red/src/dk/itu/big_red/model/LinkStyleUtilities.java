package dk.itu.big_red.model;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static org.bigraph.model.assistants.ExtendedDataUtilities.setProperty;

import org.bigraph.model.Link;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.IChange;

import dk.itu.big_red.editors.bigraph.figures.LinkConnectionFigure.Style;

public abstract class LinkStyleUtilities {
	private LinkStyleUtilities() {}
	
	@RedProperty(fired = Style.class, retrieved = Style.class)
	public static final String STYLE =
			"eD!+org.bigraph.model.Link.style";
	
	public static Style getStyle(Link l) {
		return getStyle(null, l);
	}
	
	public static Style getStyle(PropertyScratchpad context, Link l) {
		Style s = getProperty(context, l, STYLE, Style.class);
		return (s != null ? s : Style.CURVY);
	}
	
	public static void setStyle(Link l, Style s) {
		setStyle(null, l, s);
	}
	
	public static void setStyle(PropertyScratchpad context, Link l, Style s) {
		setProperty(context, l, STYLE, s);
	}
	
	public static IChange changeStyle(Link l, Style s) {
		return l.changeExtendedData(STYLE, s);
	}
}
