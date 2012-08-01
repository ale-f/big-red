package org.bigraph.model.loaders;

/**
 * A collection of bigraph-related XML namespaces.
 * @author alec
 */
public abstract class RedNamespaceConstants {
	private RedNamespaceConstants() {}
	
	/**
	 * The XML namespace for {@code <bigraph>} documents.
	 */
	public static final
	String BIGRAPH = "http://www.itu.dk/research/pls/xmlns/2010/bigraph";
	
	/**
	 * The XML namespace for {@code <signature>} documents.
	 */
	public static final
	String SIGNATURE = "http://www.itu.dk/research/pls/xmlns/2010/signature";
	
	/**
	 * The XML namespace for Big Red's visual extensions.
	 */
	public static final
	String BIG_RED = "http://www.itu.dk/research/pls/xmlns/2010/big-red";

	/**
	 * The XML namespace for {@code <change>} documents.
	 */
	public static final
	String CHANGE = "http://www.itu.dk/research/pls/xmlns/2010/change";
	
	/**
	 * The XML namespace for {@code <rule>} documents.
	 */
	public static final
	String RULE = "http://www.itu.dk/research/pls/xmlns/2011/rule";
	
	/**
	 * The XML namespace for {@code <spec>} documents.
	 */
	public static final
	String SPEC = "http://www.itu.dk/research/pls/xmlns/2012/simulation-spec";
	
	/**
	 * The XML namespace for the parameterised control extensions.
	 */
	public static final
	String PARAM = "http://bigraph.org/xmlns/2012/bigraph-extension-param";
}
