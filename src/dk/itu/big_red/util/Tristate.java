package dk.itu.big_red.util;

/**
 * <strong>Tristate</strong>s are enumerations with three possible values:
 * {@link #TRUE}, {@link #FALSE}, and <s>FileNotFound</s> {@link #UNKNOWN}.
 * @author alec
 *
 */
public enum Tristate {
	/**
	 * Neither true nor false.
	 */
	UNKNOWN,
	
	/**
	 * True.
	 */
	TRUE,
	
	/**
	 * False.
	 */
	FALSE
}