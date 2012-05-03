package dk.itu.big_red.utilities.ui;

import org.eclipse.swt.graphics.Color;

import dk.itu.big_red.model.assistants.Colour;

public class ColorWrapper {
	private Colour colour;
	private Color swtColor;
	
	public Color update(Colour c) {
		if (c != null) {
			if (!c.equals(colour)) {
				if (swtColor != null)
					swtColor.dispose();
				swtColor = c.getSWTColor();
			}
		} else {
			if (swtColor != null) {
				swtColor.dispose();
				swtColor = null;
			}
		}
		colour = c;
		return swtColor;
	}
}
