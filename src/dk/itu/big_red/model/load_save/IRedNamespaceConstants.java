package dk.itu.big_red.model.load_save;

/**
 * A collection of bigraph-related XML namespaces.
 * @author alec
 *
 */
public interface IRedNamespaceConstants {
	/**
	 * The XML namespace for {@code <bigraph>} documents.
	 */
	public static final String BIGRAPH =
		"http://www.itu.dk/research/pls/xmlns/2010/bigraph";
	
	/**
	 * The XML namespace for {@code <signature>} documents.
	 */
	public static final String SIGNATURE =
		"http://www.itu.dk/research/pls/xmlns/2010/signature";
	
	/**
	 * The XML namespace for Big Red's extensions to the other formats.
	 */
	public static final String BIG_RED =
		"http://www.itu.dk/research/pls/xmlns/2010/big-red";

	public static final String CHANGE =
		"http://www.itu.dk/research/pls/xmlns/2010/change";
	
	public static final String RULE =
		"http://www.itu.dk/research/pls/xmlns/2011/rule";
	
	public static final String SPEC =
		"http://www.itu.dk/research/pls/xmlns/2012/simulation-spec";
}
