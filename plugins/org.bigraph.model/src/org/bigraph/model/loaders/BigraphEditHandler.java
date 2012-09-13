package org.bigraph.model.loaders;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_BIG;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_SIG;

import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.EditXMLLoader.Participant;
import org.w3c.dom.Element;

final class BigraphEditHandler implements Participant {
	private Root.Identifier getRootIdentifier(Element el) {
		return new Root.Identifier(EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"));
	}
	
	private Site.Identifier getSiteIdentifier(Element el) {
		return new Site.Identifier(EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"));
	}
	
	private Edge.Identifier getEdgeIdentifier(Element el) {
		return new Edge.Identifier(EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"));
	}
	
	private InnerName.Identifier getInnerNameIdentifier(Element el) {
		return new InnerName.Identifier(
				EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"));
	}
	
	private OuterName.Identifier getOuterNameIdentifier(Element el) {
		return new OuterName.Identifier(
				EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"));
	}
	
	private Bigraph.Identifier getBigraphIdentifier() {
		return new Bigraph.Identifier();
	}
	
	private Port.Identifier getPortIdentifier(Element el) {
		org.bigraph.model.Node.Identifier id = getNodeIdentifier(
				EditXMLLoader.getNamedChildElement(el, EDIT_BIG, "node-id"));
		return new Port.Identifier(
				EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"), id);
	}
	
	private org.bigraph.model.Node.Identifier getNodeIdentifier(
			Element el) {
		Control.Identifier id = getControlIdentifier(
				EditXMLLoader.getNamedChildElement(el, EDIT_SIG, "control-id"));
		return new org.bigraph.model.Node.Identifier(
				EditXMLLoader.getAttributeNS(el, EDIT_BIG, "name"), id);
	}
	
	private Control.Identifier getControlIdentifier(Element el) {
		return new Control.Identifier(
				EditXMLLoader.getAttributeNS(el, EDIT_SIG, "name"));
	}
	
	private Container.Identifier getParentIdentifier(Element el) {
		String localName = el.getLocalName();
		if ("bigraph-id".equals(localName)) {
			return getBigraphIdentifier();
		} else if ("root-id".equals(localName)) {
			return getRootIdentifier(el);
		} else if ("node-id".equals(localName)) {
			return getNodeIdentifier(el);
		} else return null;
	}
	
	private Layoutable.Identifier getChildIdentifier(Element el) {
		String localName = el.getLocalName();
		if ("root-id".equals(localName)) {
			return getRootIdentifier(el);
		} else if ("node-id".equals(localName)) {
			return getNodeIdentifier(el);
		} else if ("site-id".equals(localName)) {
			return getSiteIdentifier(el);
		} else if ("edge-id".equals(localName)) {
			return getEdgeIdentifier(el);
		} else if ("innername-id".equals(localName)) {
			return getInnerNameIdentifier(el);
		} else if ("outername-id".equals(localName)) {
			return getOuterNameIdentifier(el);
		} else return null;
	}
	
	private Point.Identifier getPointIdentifier(Element el) {
		String localName = el.getLocalName();
		if ("port-id".equals(localName)) {
			return getPortIdentifier(el);
		} else if ("innername-id".equals(localName)) {
			return getInnerNameIdentifier(el);
		} else return null;
	}
	
	private Link.Identifier getLinkIdentifier(Element el) {
		String localName = el.getLocalName();
		if ("edge-id".equals(localName)) {
			return getEdgeIdentifier(el);
		} else if ("outername-id".equals(localName)) {
			return getOuterNameIdentifier(el);
		} else return null;
	}
	
	@Override
	public IChangeDescriptor getDescriptor(Element descriptor) {
		String
			nsURI = descriptor.getNamespaceURI(),
			localName = descriptor.getLocalName();
		if (EDIT_BIG.equals(nsURI)) {
			List<Element> ids = EditXMLLoader.getChildElements(descriptor);
			if ("add".equals(localName)) {
				Container.Identifier parent =
					getParentIdentifier(ids.get(0));
				Layoutable.Identifier child =
					getChildIdentifier(ids.get(1));
				return new Container.ChangeAddChildDescriptor(
						parent, child);
			} else if ("remove".equals(localName)) {
				Layoutable.Identifier target =
					getChildIdentifier(ids.get(0));
				return new Layoutable.ChangeRemoveDescriptor(target);
			} else if ("connect".equals(localName)) {
				Point.Identifier point =
					getPointIdentifier(ids.get(0));
				Link.Identifier link =
					getLinkIdentifier(ids.get(1));
				return new Point.ChangeConnectDescriptor(point, link);
			} else if ("disconnect".equals(localName)) {
				Point.Identifier point =
					getPointIdentifier(ids.get(0));
				return new Point.ChangeDisconnectDescriptor(point);
			} else return null;
		} else return null;
	}
	
	@Override
	public IChangeDescriptor getRenameDescriptor(Element id, String name) {
		Layoutable.Identifier lid = getChildIdentifier(id);
		return (lid != null ?
				new Layoutable.ChangeNameDescriptor(lid, name) : null);
	}
}