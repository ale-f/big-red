package dk.itu.big_red.model.interfaces;

import org.w3c.dom.Node;

/**
 * Objects implementing IXMLisable can turn themselves into XML documents and
 * fragments (and can replace whatever they currently contain with the contents
 * of a XML document).
 * @author alec
 *
 */
public interface IXMLisable {
	public Node toXML(Node d);
	public void fromXML(Node d);
}
