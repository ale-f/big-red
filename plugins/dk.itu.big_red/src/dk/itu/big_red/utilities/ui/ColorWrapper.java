package dk.itu.big_red.utilities.ui;

import org.eclipse.swt.graphics.Color;

import dk.itu.big_red.model.Colour;

public class ColorWrapper {
	private Colour colour;
	private Color swtColor;
	
	public Color update(Colour c) {
		if (c != null) {
			if (!c.equals(colour)) {
				if (swtColor != null)
					swtColor.dispose();
				swtColor = new Color(UI.getDisplay(),
						c.getRed(), c.getGreen(), c.getBlue());
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
