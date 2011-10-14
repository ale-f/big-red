package dk.itu.big_red.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import dk.itu.big_red.application.plugin.RedPlugin;

/**
 * Utility methods for dealing with {@link RGB} colours.
 * @author alec
 *
 */
public class Colour extends ReadonlyColour {
	private static final void putNamed(String s, int r, int g, int b) {
		NAMED_COLOURS.put(s, new Colour(r, g, b));
	}
	
	public static final Map<String, Colour> NAMED_COLOURS =
		new HashMap<String, Colour>();
	static {
		putNamed("aliceblue", 240, 248, 255);
		putNamed("antiquewhite", 250, 235, 215);
		putNamed("aqua", 0, 255, 255);
		putNamed("aquamarine", 127, 255, 212);
		putNamed("azure", 240, 255, 255);
		putNamed("beige", 245, 245, 220);
		putNamed("bisque", 255, 228, 196);
		putNamed("black", 0, 0, 0);
		putNamed("blanchedalmond", 255, 235, 205);
		putNamed("blue", 0, 0, 255);
		putNamed("blueviolet", 138, 43, 226);
		putNamed("brown", 165, 42, 42);
		putNamed("burlywood", 222, 184, 135);
		putNamed("cadetblue", 95, 158, 160);
		putNamed("chartreuse", 127, 255, 0);
		putNamed("chocolate", 210, 105, 30);
		putNamed("coral", 255, 127, 80);
		putNamed("cornflowerblue", 100, 149, 237);
		putNamed("cornsilk", 255, 248, 220);
		putNamed("crimson", 220, 20, 60);
		putNamed("cyan", 0, 255, 255);
		putNamed("darkblue", 0, 0, 139);
		putNamed("darkcyan", 0, 139, 139);
		putNamed("darkgoldenrod", 184, 134, 11);
		putNamed("darkgray", 169, 169, 169);
		putNamed("darkgreen", 0, 100, 0);
		putNamed("darkgrey", 169, 169, 169);
		putNamed("darkkhaki", 189, 183, 107);
		putNamed("darkmagenta", 139, 0, 139);
		putNamed("darkolivegreen", 85, 107, 47);
		putNamed("darkorange", 255, 140, 0);
		putNamed("darkorchid", 153, 50, 204);
		putNamed("darkred", 139, 0, 0);
		putNamed("darksalmon", 233, 150, 122);
		putNamed("darkseagreen", 143, 188, 143);
		putNamed("darkslateblue", 72, 61, 139);
		putNamed("darkslategray", 47, 79, 79);
		putNamed("darkslategrey", 47, 79, 79);
		putNamed("darkturquoise", 0, 206, 209);
		putNamed("darkviolet", 148, 0, 211);
		putNamed("deeppink", 255, 20, 147);
		putNamed("deepskyblue", 0, 191, 255);
		putNamed("dimgray", 105, 105, 105);
		putNamed("dimgrey", 105, 105, 105);
		putNamed("dodgerblue", 30, 144, 255);
		putNamed("firebrick", 178, 34, 34);
		putNamed("floralwhite", 255, 250, 240);
		putNamed("forestgreen", 34, 139, 34);
		putNamed("fuchsia", 255, 0, 255);
		putNamed("gainsboro", 220, 220, 220);
		putNamed("ghostwhite", 248, 248, 255);
		putNamed("gold", 255, 215, 0);
		putNamed("goldenrod", 218, 165, 32);
		putNamed("gray", 128, 128, 128);
		putNamed("green", 0, 128, 0);
		putNamed("greenyellow", 173, 255, 47);
		putNamed("grey", 128, 128, 128);
		putNamed("honeydew", 240, 255, 240);
		putNamed("hotpink", 255, 105, 180);
		putNamed("indianred", 205, 92, 92);
		putNamed("indigo", 75, 0, 130);
		putNamed("ivory", 255, 255, 240);
		putNamed("khaki", 240, 230, 140);
		putNamed("lavender", 230, 230, 250);
		putNamed("lavenderblush", 255, 240, 245);
		putNamed("lawngreen", 124, 252, 0);
		putNamed("lemonchiffon", 255, 250, 205);
		putNamed("lightblue", 173, 216, 230);
		putNamed("lightcoral", 240, 128, 128);
		putNamed("lightcyan", 224, 255, 255);
		putNamed("lightgoldenrodyellow", 250, 250, 210);
		putNamed("lightgray", 211, 211, 211);
		putNamed("lightgreen", 144, 238, 144);
		putNamed("lightgrey", 211, 211, 211);
		putNamed("lightpink", 255, 182, 193);
		putNamed("lightsalmon", 255, 160, 122);
		putNamed("lightseagreen", 32, 178, 170);
		putNamed("lightskyblue", 135, 206, 250);
		putNamed("lightslategray", 119, 136, 153);
		putNamed("lightslategrey", 119, 136, 153);
		putNamed("lightsteelblue", 176, 196, 222);
		putNamed("lightyellow", 255, 255, 224);
		putNamed("lime", 0, 255, 0);
		putNamed("limegreen", 50, 205, 50);
		putNamed("linen", 250, 240, 230);
		putNamed("magenta", 255, 0, 255);
		putNamed("maroon", 128, 0, 0);
		putNamed("mediumaquamarine", 102, 205, 170);
		putNamed("mediumblue", 0, 0, 205);
		putNamed("mediumorchid", 186, 85, 211);
		putNamed("mediumpurple", 147, 112, 219);
		putNamed("mediumseagreen", 60, 179, 113);
		putNamed("mediumslateblue", 123, 104, 238);
		putNamed("mediumspringgreen", 0, 250, 154);
		putNamed("mediumturquoise", 72, 209, 204);
		putNamed("mediumvioletred", 199, 21, 133);
		putNamed("midnightblue", 25, 25, 112);
		putNamed("mintcream", 245, 255, 250);
		putNamed("mistyrose", 255, 228, 225);
		putNamed("moccasin", 255, 228, 181);
		putNamed("navajowhite", 255, 222, 173);
		putNamed("navy", 0, 0, 128);
		putNamed("oldlace", 253, 245, 230);
		putNamed("olive", 128, 128, 0);
		putNamed("olivedrab", 107, 142, 35);
		putNamed("orange", 255, 165, 0);
		putNamed("orangered", 255, 69, 0);
		putNamed("orchid", 218, 112, 214);
		putNamed("palegoldenrod", 238, 232, 170);
		putNamed("palegreen", 152, 251, 152);
		putNamed("paleturquoise", 175, 238, 238);
		putNamed("palevioletred", 219, 112, 147);
		putNamed("papayawhip", 255, 239, 213);
		putNamed("peachpuff", 255, 218, 185);
		putNamed("peru", 205, 133, 63);
		putNamed("pink", 255, 192, 203);
		putNamed("plum", 221, 160, 221);
		putNamed("powderblue", 176, 224, 230);
		putNamed("purple", 128, 0, 128);
		putNamed("red", 255, 0, 0);
		putNamed("rosybrown", 188, 143, 143);
		putNamed("royalblue", 65, 105, 225);
		putNamed("saddlebrown", 139, 69, 19);
		putNamed("salmon", 250, 128, 114);
		putNamed("sandybrown", 244, 164, 96);
		putNamed("seagreen", 46, 139, 87);
		putNamed("seashell", 255, 245, 238);
		putNamed("sienna", 160, 82, 45);
		putNamed("silver", 192, 192, 192);
		putNamed("skyblue", 135, 206, 235);
		putNamed("slateblue", 106, 90, 205);
		putNamed("slategray", 112, 128, 144);
		putNamed("slategrey", 112, 128, 144);
		putNamed("snow", 255, 250, 250);
		putNamed("springgreen", 0, 255, 127);
		putNamed("steelblue", 70, 130, 180);
		putNamed("tan", 210, 180, 140);
		putNamed("teal", 0, 128, 128);
		putNamed("thistle", 216, 191, 216);
		putNamed("tomato", 255, 99, 71);
		putNamed("turquoise", 64, 224, 208);
		putNamed("violet", 238, 130, 238);
		putNamed("wheat", 245, 222, 179);
		putNamed("white", 255, 255, 255);
		putNamed("whitesmoke", 245, 245, 245);
		putNamed("yellow", 255, 255, 0);
		putNamed("yellowgreen", 154, 205, 50);
	}
	
	private int red = 0, green = 0, blue = 0;
	private double alpha = 1;
	
	public Colour() {
		
	}
	
	public Colour(int r, int g, int b) {
		setRed(r).setGreen(g).setBlue(b);
	}
	
	public Colour(int r, int g, int b, double a) {
		setRed(r).setGreen(g).setBlue(b).setAlpha(a);
	}
	
	public Colour(ReadonlyColour c) {
		setColour(c);
	}
	
	public Colour(RGB r) {
		setColour(r);
	}
	
	public Colour(String s) {
		setColour(s);
	}
	
	private static final int integer(String s) {
		return Integer.parseInt(s);
	}
	
	private static final int hex(String s) {
		return Integer.parseInt(s, 16);
	}
	
	private static final int percentage(String pc) {
		return (int)(2.55 * Integer.parseInt(pc));
	}
	
	private static final double num(String pc) {
		return Double.parseDouble(pc);
	}
	
	private static Pattern
		rgb = Pattern.compile("rgb\\(\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+)\\s*\\)"),
		rgba = Pattern.compile("rgba\\(\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+(?:\\.[0-9]+)?)\\s*\\)"),
		rgbp = Pattern.compile("rgb\\(\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+)%\\s*\\)"),
		rgbap = Pattern.compile("rgba\\(\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+(?:\\.[0-9]+)?)\\s*\\)"),
		hsl = Pattern.compile("hsl\\(\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+)%\\s*\\)"),
		hsla = Pattern.compile("hsla\\(\\s*([-]?[0-9]+)\\s*,\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+)%\\s*,\\s*([-]?[0-9]+(?:\\.[0-9]+)?)\\s*\\)"),
		hex3 = Pattern.compile("^#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])$"),
		hex6 = Pattern.compile("^#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$");
	
	public Colour setColour(String s) {
		Colour n = NAMED_COLOURS.get(s.toLowerCase(Locale.ENGLISH));
		if (n != null)
			return setColour(n);
		
		Matcher
			rgbm = rgb.matcher(s), rgbam = rgba.matcher(s),
			rgbpm = rgbp.matcher(s), rgbapm = rgbap.matcher(s),
			hslm = hsl.matcher(s), hslam = hsla.matcher(s),
			hex3m = hex3.matcher(s), hex6m = hex6.matcher(s);
		if (rgbm.matches()) {
			setRed(integer(rgbm.group(1))).
				setGreen(integer(rgbm.group(2))).
				setBlue(integer(rgbm.group(3))).
				setAlpha(1);
		} else if (rgbam.matches()) {
			setRed(integer(rgbam.group(1))).
				setGreen(integer(rgbam.group(2))).
				setBlue(integer(rgbam.group(3))).
				setAlpha(num(rgbam.group(4)));
		} else if (rgbpm.matches()) {
			setRed(percentage(rgbpm.group(1))).
				setGreen(percentage(rgbpm.group(2))).
				setBlue(percentage(rgbpm.group(3))).
				setAlpha(1);
		} else if (rgbapm.matches()) {
			setRed(percentage(rgbapm.group(1))).
				setGreen(percentage(rgbapm.group(2))).
				setBlue(percentage(rgbapm.group(3))).
				setAlpha(num(rgbam.group(4)));
		} else if (hslm.matches()) {
			return null;
		} else if (hslam.matches()) {
			return null;
		} else if (hex3m.matches()) {
			setRed(hex(hex3m.group(1) + hex3m.group(1))).
				setGreen(hex(hex3m.group(2) + hex3m.group(2))).
				setBlue(hex(hex3m.group(3) + hex3m.group(3))).
				setAlpha(1);
		} else if (hex6m.matches()) {
			setRed(hex(hex6m.group(1))).
				setGreen(hex(hex6m.group(2))).
				setBlue(hex(hex6m.group(3))).
				setAlpha(1);
		} else {
			return null;
		}
		return this;
	}
	
	public Colour setColour(RGB r) {
		return setRed(r.red).setGreen(r.green).setBlue(r.blue).setAlpha(1);
	}
	
	public Colour setColour(ReadonlyColour c) {
		return setRed(c.getRed()).setGreen(c.getGreen()).
				setBlue(c.getBlue()).setAlpha(c.getAlpha());
	}
	
	public Colour setRed(int red) {
		if (red == this.red) {
			return this;
		} else invalidateSWTColor();
		if (red < 0)
			red = 0;
		else if (red > 255)
			red = 255;
		this.red = red;
		return this;
	}

	public Colour setGreen(int green) {
		if (green == this.green) {
			return this;
		} else invalidateSWTColor();
		if (green < 0)
			green = 0;
		else if (green > 255)
			green = 255;
		this.green = green;
		return this;
	}
	
	public Colour setBlue(int blue) {
		if (blue == this.blue) {
			return this;
		} else invalidateSWTColor();
		if (blue < 0)
			blue = 0;
		else if (blue > 255)
			blue = 255;
		this.blue = blue;
		return this;
	}
	
	public Colour setAlpha(double alpha) {
		if (alpha == this.alpha) {
			return this;
		} else invalidateSWTColor();
		if (alpha < 0)
			alpha = 0;
		else if (alpha > 1)
			alpha = 1;
		this.alpha = alpha;
		return this;
	}
	
	@Override
	public int getRed() {
		return red;
	}
	
	@Override
	public int getGreen() {
		return green;
	}
	
	@Override
	public int getBlue() {
		return blue;
	}
	
	@Override
	public double getAlpha() {
		return alpha;
	}
	
	/**
	 * Randomises the red, green, and blue values of this {@link Colour}, and
	 * sets its alpha value to <code>1.0</code>.
	 * @return <code>this</code>, for convenience
	 */
	public Colour randomise() {
		Random r = RedPlugin.getRandom();
		return setRed(r.nextInt(256)).setGreen(r.nextInt(256)).
				setBlue(r.nextInt(256)).setAlpha(1);
	}
}