package dk.itu.big_red.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * Miscellaneous utility methods.
 * @author alec
 *
 */
public final class Utility {
	private static Map<String, Image> sharedImages = new HashMap<String, Image>();
	
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
	
	public static Image getImage(String symbolicName) {
		return UI.getWorkbench().getSharedImages().getImage(symbolicName);
	}
	
	public static ImageDescriptor getImageDescriptor(String symbolicName) {
		return UI.getWorkbench().getSharedImages().getImageDescriptor(symbolicName);
	}
	
	/**
	 * Gets a shared image from the Big Red-specific set, creating it if
	 * necessary.
	 * <p>Shared images are found in the <code>src/resource</code> folder.
	 * @param path an absolute path <i>relative</i> (!) to <code>src/</code>
	 * @return the shared image, or <code>null</code> if it couldn't be found 
	 */
	public static Image getBigRedImage(String path) {
		Image r;
		if ((r = sharedImages.get(path)) == null) {
			InputStream s = Utility.class.getResourceAsStream(path);
			if (s != null) {
				r = new Image(null, s);
				sharedImages.put(path, r);
			}
		}
		return r;
	}
	
	/**
	 * Returns a copy of the given Font with the formatting properties changed.
	 * @param original the font to adjust
	 * @param properties a combination of {@link SWT#BOLD},
	 * {@link SWT#ITALIC}, and {@link SWT#NORMAL}
	 * @return a new Font (make sure to {@link Font#dispose() dispose} it!)
	 */
	public static Font tweakFont(Font original, int properties) {
		return tweakFont(original, 0, properties);
	}
	
	/**
	 * Returns a copy of the given Font with the formatting properties changed.
	 * @param original the font to adjust
	 * @param pt the size of the new font, in points
	 * @param properties a combination of {@link SWT#BOLD},
	 * {@link SWT#ITALIC}, and {@link SWT#NORMAL}
	 * @return a new Font (make sure to {@link Font#dispose() dispose} it!)
	 */
	public static Font tweakFont(Font original, int pt, int properties) {
		FontData[] orig = original.getFontData();
		if (orig.length < 1)
			return null;
		if (pt < 1)
			pt = orig[0].getHeight();
		FontData f = new FontData(orig[0].getName(), pt, properties);
		return new Font(null, f);
	}
}
