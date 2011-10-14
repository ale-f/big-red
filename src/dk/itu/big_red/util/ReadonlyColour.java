package dk.itu.big_red.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public abstract class ReadonlyColour {
	public abstract int getRed();
	public abstract int getGreen();
	public abstract int getBlue();
	public abstract double getAlpha();
	
	private String leftPad(String s, char pad, int length) {
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
		s.append(getAlpha() == 1 ? "rgb(" : "rgba(");
		s.append(Integer.toString(getRed()));
		s.append(", ");
		s.append(Integer.toString(getBlue()));
		s.append(", ");
		s.append(Integer.toString(getGreen()));
		if (getAlpha() != 1) {
			s.append(", ");
			s.append(getAlpha());
		}
		s.append(")");
		return s.toString();
	}

	public RGB getRGB() {
		return new RGB(getRed(), getGreen(), getBlue());
	}
	
	private Color swtColor = null;
	
	/**
	 * Returns the SWT {@link Color} corresponding to this {@link Colour},
	 * creating it if necessary.
	 * <p>Changing any of this {@link Colour}'s properties will invalidate the
	 * object returned by this function.
	 * @return a {@link Color}
	 */
	public Color getSWTColor() {
		if (swtColor == null)
			swtColor = new Color(null, getRed(), getGreen(), getBlue());
		return swtColor;
	}
	
	/**
	 * Disposes of this {@link Colour}'s corresponding SWT {@link Color}, if
	 * one has been created (by a call to {@link #getSWTColor()}).
	 */
	public void invalidateSWTColor() {
		if (swtColor != null) {
			swtColor.dispose();
			swtColor = null;
		}
	}
	
	@Override
	protected Colour clone() {
		return getCopy();
	}
	
	public Colour getCopy() {
		return new Colour(this);
	}
}
