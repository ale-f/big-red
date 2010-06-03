package dk.itu.big_red.model;

import java.util.HashMap;

/**
 * Objects implementing IXMLisable can turn themselves into XML documents and
 * fragments (and can replace whatever they currently contain with the contents
 * of a XML document).
 * @author alec
 *
 */
public interface IXMLisable {
	public org.w3c.dom.Node toXML();
	public org.w3c.dom.Node toXML(org.w3c.dom.Node d);
	/**
	 * Populates the current Thing from the contents of the Node given.
	 * XXX - changed to void return value. Sensible? (if not, Thing)
	 * @param d
	 * @return
	 */
	public void fromXML(org.w3c.dom.Node d, HashMap<String, Thing> idRegistry);
}
