package dk.itu.big_red.util;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOM {

	/**
	 * Gets all the children of the specified node with the given tag name.
	 * (Note that this method only searches immediate children.)
	 * @param d a Node containing children
	 * @param n the tag name to search for
	 * @return an ArrayList of child nodes
	 */
	public static ArrayList<Node> getNamedChildNodes(Node d, String n) {
		ArrayList<Node> r = new ArrayList<Node>();
		NodeList p = d.getChildNodes();
		for (int i = 0; i < p.getLength(); i++) {
			Node t = p.item(i);
			if (t.getNodeName().equals(n)) r.add(t);
		}
		return r;
	}

	/**
	 * Returns the unique child of the specified node which has the given tag
	 * name.
	 * @param d a Node containing children
	 * @param n the tag name to search for
	 * @return the unique named child, or <code>null</code> if there were zero
	 *         or more than one matches
	 * @see DOM#getNamedChildNodes
	 */
	public static Node getNamedChildNode(Node d, String n) {
		ArrayList<Node> r = getNamedChildNodes(d, n);
		if (r.size() != 0)
			return r.get(0);
		else return null;
	}

	/**
	 * Retrieves the given named attribute from the specified node's node map,
	 * so you can get an attribute with one function call rather than four of
	 * them.
	 * @param d a Node with attributes set
	 * @param n the attribute name to search for
	 * @return the attribute's value, or <code>null</code> if the attribute
	 *         couldn't be found
	 */
	public static String getAttribute(Node d, String n) {
		NamedNodeMap attrs = d.getAttributes();
		if (attrs != null) {
			Node value = attrs.getNamedItem(n);
			if (value != null) {
				String result = value.getNodeValue().trim();
				if (result.length() > 0)
					return result;
					else return null;
			} else return null;
		} else return null;
	}

	/**
	 * Retrieves the given named attribute from the specified node's node map,
	 * automatically converting the result from a string into an integer.
	 * @param d a Node with attributes set
	 * @param n the attribute name to search for
	 * @return the attribute's value as an integer, or 0 if the attribute
	 *         couldn't be found
	 * @see DOM#getAttribute
	 */
	public static int getIntAttribute(Node d, String n) {
		try {
			return Integer.parseInt(getAttribute(d, n));
		} catch (Exception e) {
			return 0;
		}
	}
}
