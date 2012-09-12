package org.bigraph.model.loaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.Edit;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_BIG;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_SIG;

public class EditXMLLoader extends XMLLoader {
	protected interface IParticipant {
		IChangeDescriptor getDescriptor(Element descriptor);
		IChangeDescriptor getRenameDescriptor(Element id, String name);
	}
	
	private final class BigraphEditHandler implements IParticipant {
		private Root.Identifier getRootIdentifier(Element el) {
			return new Root.Identifier(getAttributeNS(el, EDIT_BIG, "name"));
		}
		
		private Site.Identifier getSiteIdentifier(Element el) {
			return new Site.Identifier(getAttributeNS(el, EDIT_BIG, "name"));
		}
		
		private Edge.Identifier getEdgeIdentifier(Element el) {
			return new Edge.Identifier(getAttributeNS(el, EDIT_BIG, "name"));
		}
		
		private InnerName.Identifier getInnerNameIdentifier(Element el) {
			return new InnerName.Identifier(
					getAttributeNS(el, EDIT_BIG, "name"));
		}
		
		private OuterName.Identifier getOuterNameIdentifier(Element el) {
			return new OuterName.Identifier(
					getAttributeNS(el, EDIT_BIG, "name"));
		}
		
		private Bigraph.Identifier getBigraphIdentifier() {
			return new Bigraph.Identifier();
		}
		
		private Port.Identifier getPortIdentifier(Element el) {
			org.bigraph.model.Node.Identifier id = getNodeIdentifier(
					getNamedChildElement(el, EDIT_BIG, "node-id"));
			return new Port.Identifier(
					getAttributeNS(el, EDIT_BIG, "name"), id);
		}
		
		private org.bigraph.model.Node.Identifier getNodeIdentifier(
				Element el) {
			Control.Identifier id = getControlIdentifier(
					getNamedChildElement(el, EDIT_SIG, "control-id"));
			return new org.bigraph.model.Node.Identifier(
					getAttributeNS(el, EDIT_BIG, "name"), id);
		}
		
		private Control.Identifier getControlIdentifier(Element el) {
			return new Control.Identifier(
					getAttributeNS(el, EDIT_SIG, "name"));
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
				List<Element> ids = getChildElements(descriptor);
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
	
	private List<IParticipant> participants = new ArrayList<IParticipant>();
	
	public EditXMLLoader addParticipant(IParticipant one) {
		participants.add(one);
		return this;
	}
	
	public EditXMLLoader addParticipants(
			Collection<? extends IParticipant> many) {
		participants.addAll(many);
		return this;
	}
	
	{
		addParticipant(new BigraphEditHandler());
	}
	
	protected List<IParticipant> getParticipants() {
		return participants;
	}
	
	public EditXMLLoader() {
	}
	
	public EditXMLLoader(Loader parent) {
		super(parent);
	}

	@Override
	public Edit importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					Schemas.getEditSchema());
			Edit ed = makeObject(d.getDocumentElement());
			FileData.setFile(ed, getFile());
			return ed;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private IChangeDescriptor makeDescriptor(Element el) {
		IChangeDescriptor cd = null;
		for (IParticipant p : getParticipants()) {
			cd = p.getDescriptor(el);
			if (cd != null)
				break;
		}
		return cd;
	}
	
	private IChangeDescriptor makeRename(Element el) {
		Node id = el.getFirstChild();
		if (!(id instanceof Element))
			return null;
		String name = getAttributeNS(el, EDIT, "name");
		
		IChangeDescriptor cd = null;
		for (IParticipant p : getParticipants()) {
			cd = p.getRenameDescriptor((Element)id, name);
			if (cd != null)
				break;
		}
		return cd;
	}
	
	@Override
	public Edit makeObject(Element el) throws LoadFailedException {
		cycleCheck();
		Edit ed = loadRelative(
				getAttributeNS(el, EDIT, "src"), Edit.class, this);
		if (ed != null) {
			return ed;
		} else ed = new Edit();
		
		NodeList nl = el.getChildNodes();
		int len = nl.getLength(), index = 0;
		for (int i__ = 0; i__ < len; i__++) {
			Node i_ = nl.item(i__);
			if (!(i_ instanceof Element))
				continue;
			Element i = (Element)i_;
			String
				localName = i.getLocalName(),
				namespaceURI = i.getNamespaceURI();
			
			IChangeDescriptor cd = null;
			if (EDIT.equals(namespaceURI)) {
				if ("edit".equals(localName)) {
					cd = new EditXMLLoader(this).makeObject(i);
				} else if ("rename".equals(localName)) {
					cd = makeRename(i);
				}
			} else cd = makeDescriptor(i);
			
			if (cd != null)
				addChange(ed.changeDescriptorAdd(index++, cd));
		}
		
		executeUndecorators(ed, el);
		executeChanges(ed);
		return ed;
	}
}
