package dk.itu.big_red.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;

public class Colour {
	private static final Map<String, Colour> NAMED_COLOURS =
			new HashMap<String, Colour>();
	static {
		NAMED_COLOURS.put("aliceblue", new Colour(240, 248, 255));
		NAMED_COLOURS.put("antiquewhite", new Colour(250, 235, 215));
		NAMED_COLOURS.put("aqua", new Colour(0, 255, 255));
		NAMED_COLOURS.put("aquamarine", new Colour(127, 255, 212));
		NAMED_COLOURS.put("azure", new Colour(240, 255, 255));
		NAMED_COLOURS.put("beige", new Colour(245, 245, 220));
		NAMED_COLOURS.put("bisque", new Colour(255, 228, 196));
		NAMED_COLOURS.put("black", new Colour(0, 0, 0));
		NAMED_COLOURS.put("blanchedalmond", new Colour(255, 235, 205));
		NAMED_COLOURS.put("blue", new Colour(0, 0, 255));
		NAMED_COLOURS.put("blueviolet", new Colour(138, 43, 226));
		NAMED_COLOURS.put("brown", new Colour(165, 42, 42));
		NAMED_COLOURS.put("burlywood", new Colour(222, 184, 135));
		NAMED_COLOURS.put("cadetblue", new Colour(95, 158, 160));
		NAMED_COLOURS.put("chartreuse", new Colour(127, 255, 0));
		NAMED_COLOURS.put("chocolate", new Colour(210, 105, 30));
		NAMED_COLOURS.put("coral", new Colour(255, 127, 80));
		NAMED_COLOURS.put("cornflowerblue", new Colour(100, 149, 237));
		NAMED_COLOURS.put("cornsilk", new Colour(255, 248, 220));
		NAMED_COLOURS.put("crimson", new Colour(220, 20, 60));
		NAMED_COLOURS.put("cyan", new Colour(0, 255, 255));
		NAMED_COLOURS.put("darkblue", new Colour(0, 0, 139));
		NAMED_COLOURS.put("darkcyan", new Colour(0, 139, 139));
		NAMED_COLOURS.put("darkgoldenrod", new Colour(184, 134, 11));
		NAMED_COLOURS.put("darkgray", new Colour(169, 169, 169));
		NAMED_COLOURS.put("darkgreen", new Colour(0, 100, 0));
		NAMED_COLOURS.put("darkgrey", new Colour(169, 169, 169));
		NAMED_COLOURS.put("darkkhaki", new Colour(189, 183, 107));
		NAMED_COLOURS.put("darkmagenta", new Colour(139, 0, 139));
		NAMED_COLOURS.put("darkolivegreen", new Colour(85, 107, 47));
		NAMED_COLOURS.put("darkorange", new Colour(255, 140, 0));
		NAMED_COLOURS.put("darkorchid", new Colour(153, 50, 204));
		NAMED_COLOURS.put("darkred", new Colour(139, 0, 0));
		NAMED_COLOURS.put("darksalmon", new Colour(233, 150, 122));
		NAMED_COLOURS.put("darkseagreen", new Colour(143, 188, 143));
		NAMED_COLOURS.put("darkslateblue", new Colour(72, 61, 139));
		NAMED_COLOURS.put("darkslategray", new Colour(47, 79, 79));
		NAMED_COLOURS.put("darkslategrey", new Colour(47, 79, 79));
		NAMED_COLOURS.put("darkturquoise", new Colour(0, 206, 209));
		NAMED_COLOURS.put("darkviolet", new Colour(148, 0, 211));
		NAMED_COLOURS.put("deeppink", new Colour(255, 20, 147));
		NAMED_COLOURS.put("deepskyblue", new Colour(0, 191, 255));
		NAMED_COLOURS.put("dimgray", new Colour(105, 105, 105));
		NAMED_COLOURS.put("dimgrey", new Colour(105, 105, 105));
		NAMED_COLOURS.put("dodgerblue", new Colour(30, 144, 255));
		NAMED_COLOURS.put("firebrick", new Colour(178, 34, 34));
		NAMED_COLOURS.put("floralwhite", new Colour(255, 250, 240));
		NAMED_COLOURS.put("forestgreen", new Colour(34, 139, 34));
		NAMED_COLOURS.put("fuchsia", new Colour(255, 0, 255));
		NAMED_COLOURS.put("gainsboro", new Colour(220, 220, 220));
		NAMED_COLOURS.put("ghostwhite", new Colour(248, 248, 255));
		NAMED_COLOURS.put("gold", new Colour(255, 215, 0));
		NAMED_COLOURS.put("goldenrod", new Colour(218, 165, 32));
		NAMED_COLOURS.put("gray", new Colour(128, 128, 128));
		NAMED_COLOURS.put("green", new Colour(0, 128, 0));
		NAMED_COLOURS.put("greenyellow", new Colour(173, 255, 47));
		NAMED_COLOURS.put("grey", new Colour(128, 128, 128));
		NAMED_COLOURS.put("honeydew", new Colour(240, 255, 240));
		NAMED_COLOURS.put("hotpink", new Colour(255, 105, 180));
		NAMED_COLOURS.put("indianred", new Colour(205, 92, 92));
		NAMED_COLOURS.put("indigo", new Colour(75, 0, 130));
		NAMED_COLOURS.put("ivory", new Colour(255, 255, 240));
		NAMED_COLOURS.put("khaki", new Colour(240, 230, 140));
		NAMED_COLOURS.put("lavender", new Colour(230, 230, 250));
		NAMED_COLOURS.put("lavenderblush", new Colour(255, 240, 245));
		NAMED_COLOURS.put("lawngreen", new Colour(124, 252, 0));
		NAMED_COLOURS.put("lemonchiffon", new Colour(255, 250, 205));
		NAMED_COLOURS.put("lightblue", new Colour(173, 216, 230));
		NAMED_COLOURS.put("lightcoral", new Colour(240, 128, 128));
		NAMED_COLOURS.put("lightcyan", new Colour(224, 255, 255));
		NAMED_COLOURS.put("lightgoldenrodyellow", new Colour(250, 250, 210));
		NAMED_COLOURS.put("lightgray", new Colour(211, 211, 211));
		NAMED_COLOURS.put("lightgreen", new Colour(144, 238, 144));
		NAMED_COLOURS.put("lightgrey", new Colour(211, 211, 211));
		NAMED_COLOURS.put("lightpink", new Colour(255, 182, 193));
		NAMED_COLOURS.put("lightsalmon", new Colour(255, 160, 122));
		NAMED_COLOURS.put("lightseagreen", new Colour(32, 178, 170));
		NAMED_COLOURS.put("lightskyblue", new Colour(135, 206, 250));
		NAMED_COLOURS.put("lightslategray", new Colour(119, 136, 153));
		NAMED_COLOURS.put("lightslategrey", new Colour(119, 136, 153));
		NAMED_COLOURS.put("lightsteelblue", new Colour(176, 196, 222));
		NAMED_COLOURS.put("lightyellow", new Colour(255, 255, 224));
		NAMED_COLOURS.put("lime", new Colour(0, 255, 0));
		NAMED_COLOURS.put("limegreen", new Colour(50, 205, 50));
		NAMED_COLOURS.put("linen", new Colour(250, 240, 230));
		NAMED_COLOURS.put("magenta", new Colour(255, 0, 255));
		NAMED_COLOURS.put("maroon", new Colour(128, 0, 0));
		NAMED_COLOURS.put("mediumaquamarine", new Colour(102, 205, 170));
		NAMED_COLOURS.put("mediumblue", new Colour(0, 0, 205));
		NAMED_COLOURS.put("mediumorchid", new Colour(186, 85, 211));
		NAMED_COLOURS.put("mediumpurple", new Colour(147, 112, 219));
		NAMED_COLOURS.put("mediumseagreen", new Colour(60, 179, 113));
		NAMED_COLOURS.put("mediumslateblue", new Colour(123, 104, 238));
		NAMED_COLOURS.put("mediumspringgreen", new Colour(0, 250, 154));
		NAMED_COLOURS.put("mediumturquoise", new Colour(72, 209, 204));
		NAMED_COLOURS.put("mediumvioletred", new Colour(199, 21, 133));
		NAMED_COLOURS.put("midnightblue", new Colour(25, 25, 112));
		NAMED_COLOURS.put("mintcream", new Colour(245, 255, 250));
		NAMED_COLOURS.put("mistyrose", new Colour(255, 228, 225));
		NAMED_COLOURS.put("moccasin", new Colour(255, 228, 181));
		NAMED_COLOURS.put("navajowhite", new Colour(255, 222, 173));
		NAMED_COLOURS.put("navy", new Colour(0, 0, 128));
		NAMED_COLOURS.put("oldlace", new Colour(253, 245, 230));
		NAMED_COLOURS.put("olive", new Colour(128, 128, 0));
		NAMED_COLOURS.put("olivedrab", new Colour(107, 142, 35));
		NAMED_COLOURS.put("orange", new Colour(255, 165, 0));
		NAMED_COLOURS.put("orangered", new Colour(255, 69, 0));
		NAMED_COLOURS.put("orchid", new Colour(218, 112, 214));
		NAMED_COLOURS.put("palegoldenrod", new Colour(238, 232, 170));
		NAMED_COLOURS.put("palegreen", new Colour(152, 251, 152));
		NAMED_COLOURS.put("paleturquoise", new Colour(175, 238, 238));
		NAMED_COLOURS.put("palevioletred", new Colour(219, 112, 147));
		NAMED_COLOURS.put("papayawhip", new Colour(255, 239, 213));
		NAMED_COLOURS.put("peachpuff", new Colour(255, 218, 185));
		NAMED_COLOURS.put("peru", new Colour(205, 133, 63));
		NAMED_COLOURS.put("pink", new Colour(255, 192, 203));
		NAMED_COLOURS.put("plum", new Colour(221, 160, 221));
		NAMED_COLOURS.put("powderblue", new Colour(176, 224, 230));
		NAMED_COLOURS.put("purple", new Colour(128, 0, 128));
		NAMED_COLOURS.put("red", new Colour(255, 0, 0));
		NAMED_COLOURS.put("rosybrown", new Colour(188, 143, 143));
		NAMED_COLOURS.put("royalblue", new Colour(65, 105, 225));
		NAMED_COLOURS.put("saddlebrown", new Colour(139, 69, 19));
		NAMED_COLOURS.put("salmon", new Colour(250, 128, 114));
		NAMED_COLOURS.put("sandybrown", new Colour(244, 164, 96));
		NAMED_COLOURS.put("seagreen", new Colour(46, 139, 87));
		NAMED_COLOURS.put("seashell", new Colour(255, 245, 238));
		NAMED_COLOURS.put("sienna", new Colour(160, 82, 45));
		NAMED_COLOURS.put("silver", new Colour(192, 192, 192));
		NAMED_COLOURS.put("skyblue", new Colour(135, 206, 235));
		NAMED_COLOURS.put("slateblue", new Colour(106, 90, 205));
		NAMED_COLOURS.put("slategray", new Colour(112, 128, 144));
		NAMED_COLOURS.put("slategrey", new Colour(112, 128, 144));
		NAMED_COLOURS.put("snow", new Colour(255, 250, 250));
		NAMED_COLOURS.put("springgreen", new Colour(0, 255, 127));
		NAMED_COLOURS.put("steelblue", new Colour(70, 130, 180));
		NAMED_COLOURS.put("tan", new Colour(210, 180, 140));
		NAMED_COLOURS.put("teal", new Colour(0, 128, 128));
		NAMED_COLOURS.put("thistle", new Colour(216, 191, 216));
		NAMED_COLOURS.put("tomato", new Colour(255, 99, 71));
		NAMED_COLOURS.put("turquoise", new Colour(64, 224, 208));
		NAMED_COLOURS.put("violet", new Colour(238, 130, 238));
		NAMED_COLOURS.put("wheat", new Colour(245, 222, 179));
		NAMED_COLOURS.put("white", new Colour(255, 255, 255));
		NAMED_COLOURS.put("whitesmoke", new Colour(245, 245, 245));
		NAMED_COLOURS.put("yellow", new Colour(255, 255, 0));
		NAMED_COLOURS.put("yellowgreen", new Colour(154, 205, 50));
	}
	
	/**
	 * Returns the red component of this colour.
	 * @return a red component (between <code>0</code> and <code>255</code>
	 * inclusive)
	 */
	public int getRed() {
		return red;
	}
	
	/**
	 * Returns the green component of this colour.
	 * @return a green component (between <code>0</code> and <code>255</code>
	 * inclusive)
	 */
	public int getGreen() {
		return green;
	}
	
	/**
	 * Returns the blue component of this colour.
	 * @return a blue component (between <code>0</code> and <code>255</code>
	 * inclusive)
	 */
	public int getBlue() {
		return blue;
	}
	
	/**
	 * Returns the alpha value of this colour.
	 * @return an alpha value (between <code>0</code> and <code>255</code>
	 * inclusive)
	 */
	public int getAlpha() {
		return alpha;
	}
	
	private int red, green, blue, alpha;
	
	private static String leftPad(String s, char pad, int length) {
		while (s.length() < length)
			s = pad + s;
		return s;
	}
	
	public String toHexString() {
		return "#" +
				leftPad(Integer.toHexString(getRed()), '0', 2) +
				leftPad(Integer.toHexString(getGreen()), '0', 2) +
				leftPad(Integer.toHexString(getBlue()), '0', 2);
	}
	
	public String toFunctionString() {
		StringBuilder s = new StringBuilder();
		s.append(getAlpha() == 255 ? "rgb(" : "rgba(");
		s.append(Integer.toString(getRed()));
		s.append(", ");
		s.append(Integer.toString(getBlue()));
		s.append(", ");
		s.append(Integer.toString(getGreen()));
		if (getAlpha() != 255) {
			s.append(", ");
			s.append(getAlpha());
		}
		s.append(")");
		return s.toString();
	}

	/**
	 * Returns a new {@link RGB} object corresponding to this colour.
	 * @return a new {@link RGB} object
	 */
	public RGB getRGB() {
		return new RGB(getRed(), getGreen(), getBlue());
	}
	
	@Override
	public Colour clone() {
		return this;
	}
	
	@Override
	public int hashCode() {
		return (getRed() << 24) | (getGreen() << 16) | (getBlue() << 8) + getAlpha();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Colour) {
			Colour c = (Colour)obj;
			return (c.getRed() == getRed() &&
					c.getGreen() == getGreen() &&
					c.getBlue() == getBlue() &&
					c.getAlpha() == getAlpha());
		} else return false;
	}
	
	@Override
	public String toString() {
		return "Colour(" + getRed() + ", " + getGreen() +
				", " + getBlue() + ", " + getAlpha() + ")";
	}
	
	public Colour() {
		this(0, 0, 0);
	}
	
	/**
	 * Initialises this {@link Colour} with the components given.
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 */
	public Colour(int r, int g, int b) {
		this(r, g, b, 255);
	}
	
	/**
	 * Initialises this {@link Colour} with the components given.
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha value
	 */
	public Colour(int r, int g, int b, int a) {
		setRed(r).setGreen(g).setBlue(b).setAlpha(a);
	}
	
	/**
	 * Initialises this {@link Colour} from another colour.
	 * @see #setColour(RGB)
	 * @param s a {@link RGB} colour
	 */
	public Colour(RGB r) {
		setColour(r);
	}
	
	/**
	 * Initialises this {@link Colour} from a string.
	 * @see #setColour(String)
	 * @param s a {@link String} specifying a colour
	 */
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
	
	/**
	 * Parses the given {@link String} into a colour. Most of the colours
	 * specified by CSS Color Module Level 3 are supported:
	 * <ul>
	 * <li>all extended colour keywords;
	 * <li><code>#RGB</code> and <code>#RRGGBB</code>;
	 * <li><code>rgb(r, g, b)</code> and <code>rgb(r%, g%,
	 * b%)</code>; and
	 * <li><code>rgba(r, g, b, alpha)</code> and <code>rgba(r%, g%, b%,
	 * alpha)</code>.
	 * </ul>
	 * <p>HSL colours are <i>not</i> currently supported, and they will cause
	 * this function to return <code>null</code>.
	 * @param s a {@link String} specifying a colour
	 * @return <code>this</code>, for convenience, or <code>null</code> if the
	 * colour string couldn't be parsed
	 */
	private Colour setColour(String s) {
		if (s == null)
			return this;
		
		Colour n = NAMED_COLOURS.get(s.toLowerCase(Locale.ENGLISH));
		if (n != null)
			return setColour(n);
		
		try {
			Matcher m = null;
			if ((m = rgb.matcher(s)).matches()) {
				setRed(integer(m.group(1))).
					setGreen(integer(m.group(2))).
					setBlue(integer(m.group(3))).
					setAlpha(255);
			} else if ((m = rgba.matcher(s)).matches()) {
				setRed(integer(m.group(1))).
					setGreen(integer(m.group(2))).
					setBlue(integer(m.group(3))).
					setAlpha((int)(num(m.group(4)) * 255.0));
			} else if ((m = rgbp.matcher(s)).matches()) {
				setRed(percentage(m.group(1))).
					setGreen(percentage(m.group(2))).
					setBlue(percentage(m.group(3))).
					setAlpha(255);
			} else if ((m = rgbap.matcher(s)).matches()) {
				setRed(percentage(m.group(1))).
					setGreen(percentage(m.group(2))).
					setBlue(percentage(m.group(3))).
					setAlpha((int)(num(m.group(4)) * 255.0));
			} else if ((m = hsl.matcher(s)).matches()) {
				return null;
			} else if ((m = hsla.matcher(s)).matches()) {
				return null;
			} else if ((m = hex3.matcher(s)).matches()) {
				setRed(hex(m.group(1) + m.group(1))).
					setGreen(hex(m.group(2) + m.group(2))).
					setBlue(hex(m.group(3) + m.group(3))).
					setAlpha(255);
			} else if ((m = hex6.matcher(s)).matches()) {
				setRed(hex(m.group(1))).
					setGreen(hex(m.group(2))).
					setBlue(hex(m.group(3))).
					setAlpha(255);
			} else {
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return this;
	}
	
	private Colour setColour(RGB r) {
		return setRed(r.red).setGreen(r.green).setBlue(r.blue).setAlpha(255);
	}
	
	private Colour setColour(Colour c) {
		return setRed(c.getRed()).setGreen(c.getGreen()).
				setBlue(c.getBlue()).setAlpha(c.getAlpha());
	}
	
	private Colour setRed(int red) {
		if (red == this.red)
			return this;
		if (red < 0)
			red = 0;
		else if (red > 255)
			red = 255;
		this.red = red;
		return this;
	}

	private Colour setGreen(int green) {
		if (green == this.green)
			return this;
		if (green < 0)
			green = 0;
		else if (green > 255)
			green = 255;
		this.green = green;
		return this;
	}
	
	private Colour setBlue(int blue) {
		if (blue == this.blue)
			return this;
		if (blue < 0)
			blue = 0;
		else if (blue > 255)
			blue = 255;
		this.blue = blue;
		return this;
	}
	
	private Colour setAlpha(int alpha) {
		if (alpha == this.alpha)
			return this;
		if (alpha < 0)
			alpha = 0;
		else if (alpha > 255)
			alpha = 255;
		this.alpha = alpha;
		return this;
	}
	
	/**
	 * Returns a new opaque {@link Colour} with random red, green, and blue
	 * values.
	 * @return a new {@link Colour}
	 */
	public static Colour random() {
		Random r = new Random();
		return new Colour(r.nextInt(256), r.nextInt(256), r.nextInt(256), 255);
	}
}
