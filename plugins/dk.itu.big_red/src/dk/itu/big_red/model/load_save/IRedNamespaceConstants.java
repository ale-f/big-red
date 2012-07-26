package dk.itu.big_red.model.load_save;

/**
 * A collection of bigraph-related XML namespaces.
 * @author alec
 */
public interface IRedNamespaceConstants {
	/**
	 * The XML namespace for {@code <bigraph>} documents.
	 */
	String BIGRAPH = "http://www.itu.dk/research/pls/xmlns/2010/bigraph";
	
	/**
	 * The XML namespace for {@code <signature>} documents.
	 */
	String SIGNATURE = "http://www.itu.dk/research/pls/xmlns/2010/signature";
	
	/**
	 * The XML namespace for Big Red's extensions to the other formats.
	 */
	String BIG_RED = "http://www.itu.dk/research/pls/xmlns/2010/big-red";

	String CHANGE = "http://www.itu.dk/research/pls/xmlns/2010/change";
	
	String RULE = "http://www.itu.dk/research/pls/xmlns/2011/rule";
	
	String SPEC = "http://www.itu.dk/research/pls/xmlns/2012/simulation-spec";
	
	String PARAM = "http://bigraph.org/xmlns/2012/bigraph-extension-param";
}
