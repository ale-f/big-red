package dk.itu.big_red.model.import_export;

/**
 * A collection of bigraph-related XML namespaces.
 * @author alec
 *
 */
public interface XMLNS {
	/**
	 * The XML namespace for {@code <bigraph>} documents.
	 */
	public static final String BIGRAPH =
		"http://pls.itu.dk/bigraphs/2010/bigraph";
	
	/**
	 * The XML namespace for {@code <signature>} documents.
	 */
	public static final String SIGNATURE =
		"http://pls.itu.dk/bigraphs/2010/signature";
	
	/**
	 * The XML namespace for Big Red's extensions to the other formats.
	 */
	public static final String BIG_RED =
		"http://pls.itu.dk/bigraphs/2010/big-red";
}
