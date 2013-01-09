package org.bigraph.model.savers;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_BIG;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_SIG;

import org.bigraph.model.Container.ChangeRemoveChildDescriptor;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.NamedModelObject.ChangeNameDescriptor;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.NamedModelObject.Identifier;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.savers.EditXMLSaver.Participant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class BigraphEditSaver implements Participant {
	private IXMLSaver saver;
	
	@Override
	public void setHost(IParticipantHost host) {
		if (host instanceof IXMLSaver)
			saver = (IXMLSaver)host;
	}
	
	private final Element newElement(String ns, String qn) {
		return saver.getDocument().createElementNS(ns, qn);
	}
	
	public static Element makeID(Document d, Identifier id) {
		String name = id.getName();
		Element el = null;
		if (id instanceof Site.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:site-id");
		} else if (id instanceof Root.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:root-id");
		} else if (id instanceof Node.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:node-id");
			el.appendChild(
					makeID(d, ((Node.Identifier)id).getControl()));
		} else if (id instanceof Port.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:port-id");
			el.appendChild(
					makeID(d, ((Port.Identifier)id).getNode()));
		} else if (id instanceof InnerName.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:innername-id");
		} else if (id instanceof OuterName.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:outername-id");
		} else if (id instanceof Edge.Identifier) {
			el = d.createElementNS(EDIT_BIG, "edit-big:edge-id");
		} else if (id instanceof Control.Identifier) {
			el = d.createElementNS(EDIT_SIG, "edit-sig:control-id");
		}
		if (el != null && name != null)
			el.setAttributeNS(null, "name", name);
		return el;
	}
	
	protected Element makeID(Identifier id) {
		return makeID(saver.getDocument(), id);
	}
	
	@Override
	public Element processDescriptor(IChangeDescriptor cd_) {
		Element el = null;
		if (cd_ instanceof ChangeAddChildDescriptor) {
			ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)cd_;
			el = newElement(EDIT_BIG, "edit-big:add");
			el.appendChild(makeID(cd.getParent()));
			el.appendChild(makeID(cd.getChild()));
		} else if (cd_ instanceof ChangeRemoveChildDescriptor) {
			ChangeRemoveChildDescriptor cd = (ChangeRemoveChildDescriptor)cd_;
			el = newElement(EDIT_BIG, "edit-big:remove");
			el.appendChild(makeID(cd.getParent()));
			el.appendChild(makeID(cd.getChild()));
		} else if (cd_ instanceof ChangeNameDescriptor) {
			ChangeNameDescriptor cd = (ChangeNameDescriptor)cd_;
			el = newElement(EDIT, "edit:rename");
			el.appendChild(makeID(cd.getTarget()));
			el.setAttributeNS(null, "name", cd.getNewName());
		} else if (cd_ instanceof ChangeConnectDescriptor) {
			ChangeConnectDescriptor cd = (ChangeConnectDescriptor)cd_;
			el = newElement(EDIT_BIG, "edit-big:connect");
			el.appendChild(makeID(cd.getPoint()));
			el.appendChild(makeID(cd.getLink()));
		} else if (cd_ instanceof ChangeDisconnectDescriptor) {
			ChangeDisconnectDescriptor cd =
					(ChangeDisconnectDescriptor)cd_;
			el = newElement(EDIT_BIG, "edit-big:disconnect");
			el.appendChild(makeID(cd.getPoint()));
			el.appendChild(makeID(cd.getLink()));
		}
		return el;
	}
}