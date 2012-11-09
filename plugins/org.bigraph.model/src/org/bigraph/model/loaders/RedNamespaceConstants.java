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
	
	public static final
	String EDIT = "http://bigraph.org/xmlns/2012/edit";
	
	public static final
	String EDIT_BIG = "http://bigraph.org/xmlns/2012/edit-big";
	
	public static final
	String EDIT_SIG = "http://bigraph.org/xmlns/2012/edit-sig";
}
