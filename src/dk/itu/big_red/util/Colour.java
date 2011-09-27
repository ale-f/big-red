package dk.itu.big_red.util;

import java.util.Random;

import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.application.plugin.RedPlugin;

/**
 * Utility methods for dealing with {@link RGB} colours.
 * @author alec
 *
 */
public class Colour {

	/**
	 * Converts a {@link RGB} colour to a string of the format
	 * <code>#rrggbb</code>.
	 * 
	 * @param c a RGB colour
	 * @return a string representation of the specified colour
	 */
	public static String colourToString(RGB c) {
		String r = Integer.toHexString(c.red);
		if (r.length() == 1)
			r = "0" + r;
		String g = Integer.toHexString(c.green);
		if (g.length() == 1)
			g = "0" + g;
		String b = Integer.toHexString(c.blue);
		if (b.length() == 1)
			b = "0" + b;
		return "#" + r + g + b;
	}

	/**
	 * Converts a string description of a colour to a {@link RGB} colour.
	 * 
	 * <p>At the moment, the string must be of the format <code>#rrggbb</code>,
	 * but this will become more lenient in future.
	 * @param c a string description of a colour
	 * @return a new RGB colour, or <code>null</code> if the string was invalid
	 */
	public static RGB colourFromString(String c) {
		if (c == null || c.length() != 7 || c.charAt(0) != '#')
			return new RGB(0, 0, 0);
		int r = Integer.parseInt(c.substring(1, 3), 16);
		int g = Integer.parseInt(c.substring(3, 5), 16);
		int b = Integer.parseInt(c.substring(5, 7), 16);
		return new RGB(r, g, b);
	}

	/**
	 * Returns a random colour.
	 * @return a new {@link RGB} with random red, green, and blue values
	 */
	public static RGB randomRGB() {
		Random r = RedPlugin.getRandom();
		return new RGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
	}

}