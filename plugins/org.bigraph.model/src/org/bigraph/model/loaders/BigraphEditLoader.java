package org.bigraph.model.loaders;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_BIG;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_SIG;

import java.util.Arrays;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.Store;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.EditXMLLoader.Participant;
import org.bigraph.model.process.IParticipantHost;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;

public class BigraphEditLoader implements Participant {
	public static ModelObject.Identifier getIdentifier(Element el) {
		if (el == null)
			return null;
		String
			localName = el.getLocalName(),
			namespace = el.getNamespaceURI();
		if (EDIT_BIG.equals(namespace)) {
			if ("bigraph-id".equals(localName)) {
				return new Bigraph.Identifier();
			} else if ("root-id".equals(localName)) {
				return new Root.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"));
			} else if ("edge-id".equals(localName)) {
				return new Edge.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"));
			} else if ("innername-id".equals(localName)) {
				return new InnerName.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"));
			} else if ("outername-id".equals(localName)) {
				return new OuterName.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"));
			} else if ("node-id".equals(localName)) {
				Control.Identifier cID = getIdentifier(
						EditXMLLoader.getNamedChildElement(
								el, EDIT_SIG, "control-id"),
						Control.Identifier.class);
				return new Node.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"), cID);
			} else if ("port-id".equals(localName)) {
				Node.Identifier nID = getIdentifier(
						EditXMLLoader.getNamedChildElement(
								el, EDIT_BIG, "node-id"),
						Node.Identifier.class);
				return new Port.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"), nID);
			} else if ("site-id".equals(localName)) {
				return new Site.Identifier(
						getAttributeNS(el, EDIT_BIG, "name"));
			} else return null;
		} else if (EDIT_SIG.equals(namespace)) {
			if ("control-id".equals(localName)) {
				return new Control.Identifier(
						getAttributeNS(el, EDIT_SIG, "name"));
			} else return null;
		} else return null;
	}
	
	public static <T extends ModelObject.Identifier> T getIdentifier(
			Element el, Class<T> klass) {
		return ModelObject.require(getIdentifier(el), klass);
	}
	
	@Override
	public IChangeDescriptor getDescriptor(Element descriptor) {
		String
			nsURI = descriptor.getNamespaceURI(),
			localName = descriptor.getLocalName();
		if (EDIT_BIG.equals(nsURI)) {
			List<Element> ids = EditXMLLoader.getChildElements(descriptor);
			if ("add".equals(localName)) {
				Container.Identifier parent = getIdentifier(
						ids.get(0), Container.Identifier.class);
				Layoutable.Identifier child = getIdentifier(
						ids.get(1), Layoutable.Identifier.class);
				
				Container.ChangeAddChildDescriptor add =
						new Container.ChangeAddChildDescriptor(parent, child);
				
				String store =
						getAttributeNS(descriptor, EDIT_BIG, "store");
				if (store != null) {
					Store.EntryIdentifier eID =
							new Store.EntryIdentifier(Long.parseLong(store));
					return new ChangeDescriptorGroup(Arrays.asList(
							add, new Store.FromStoreDescriptor(child, eID)));
				} else return add;
			} else if ("remove".equals(localName)) {
				String store =
						getAttributeNS(descriptor, EDIT_BIG, "store");
				Store.EntryIdentifier eID;
				if (store != null) {
					eID = new Store.EntryIdentifier(Long.parseLong(store));
				} else eID = Store.getInstance().createID();
				Container.Identifier parent = getIdentifier(
						ids.get(0), Container.Identifier.class);
				Layoutable.Identifier child = getIdentifier(
						ids.get(1), Layoutable.Identifier.class);
				return new ChangeDescriptorGroup(Arrays.asList(
						new Store.ToStoreDescriptor(child, eID),
						new Container.ChangeRemoveChildDescriptor(
								parent, child)));
			} else if ("connect".equals(localName)) {
				Point.Identifier point = getIdentifier(
						ids.get(0), Point.Identifier.class);
				Link.Identifier link = getIdentifier(
						ids.get(1), Link.Identifier.class);
				return new Point.ChangeConnectDescriptor(point, link);
			} else if ("disconnect".equals(localName)) {
				Point.Identifier point = getIdentifier(
						ids.get(0), Point.Identifier.class);
				Link.Identifier link = getIdentifier(
						ids.get(1), Link.Identifier.class);
				return new Point.ChangeDisconnectDescriptor(point, link);
			} else return null;
		} else return null;
	}
	
	@Override
	public IChangeDescriptor getRenameDescriptor(Element id, String name) {
		Layoutable.Identifier
			lid = getIdentifier(id, Layoutable.Identifier.class);
		return (lid != null ?
				new NamedModelObject.ChangeNameDescriptor(lid, name) : null);
	}
	
	@Override
	public void setHost(IParticipantHost host) {
		/* do nothing */
	}
}