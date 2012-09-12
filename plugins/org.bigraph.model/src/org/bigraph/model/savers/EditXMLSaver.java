package org.bigraph.model.savers;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_BIG;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT_SIG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.Edit;
import org.bigraph.model.InnerName;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.Layoutable.ChangeNameDescriptor;
import org.bigraph.model.Layoutable.ChangeRemoveDescriptor;
import org.bigraph.model.ModelObject.Identifier;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.w3c.dom.Element;

public class EditXMLSaver extends XMLSaver {
	protected interface IParticipant {
		Element processDescriptor(IChangeDescriptor cd);
	}
	
	private final class BigraphEditHandler implements IParticipant {
		protected Element makeID(Identifier id) {
			String name = id.getName();
			Element el = null;
			if (id instanceof Site.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:site-id");
			} else if (id instanceof Root.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:root-id");
			} else if (id instanceof Node.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:node-id");
				el.appendChild(
						makeID(((Node.Identifier)id).getControl()));
			} else if (id instanceof Port.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:port-id");
				el.appendChild(
						makeID(((Port.Identifier)id).getNode()));
			} else if (id instanceof InnerName.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:innername-id");
			} else if (id instanceof OuterName.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:outername-id");
			} else if (id instanceof Edge.Identifier) {
				el = newElement(EDIT_BIG, "edit-big:edge-id");
			} else if (id instanceof Control.Identifier) {
				el = newElement(EDIT_SIG, "edit-sig:control-id");
			}
			if (el != null && name != null)
				el.setAttributeNS(null, "name", name);
			return el;
		}
		
		@Override
		public Element processDescriptor(IChangeDescriptor cd_) {
			Element el = null;
			if (cd_ instanceof ChangeAddChildDescriptor) {
				ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)cd_;
				el = newElement(EDIT_BIG, "edit-big:add");
				el.appendChild(makeID(cd.getParent()));
				el.appendChild(makeID(cd.getChild()));
			} else if (cd_ instanceof ChangeRemoveDescriptor) {
				ChangeRemoveDescriptor cd = (ChangeRemoveDescriptor)cd_;
				el = newElement(EDIT_BIG, "edit-big:remove");
				el.appendChild(makeID(cd.getTarget()));
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
			}
			return el;
		}
	}
	
	private List<IParticipant> participants = new ArrayList<IParticipant>();
	
	{
		participants.add(new BigraphEditHandler());
	}
	
	protected List<? extends IParticipant> getParticipants() {
		return (participants != null ? participants :
				Collections.<IParticipant>emptyList());
	}
	
	@Override
	public EditXMLSaver setModel(ModelObject model) {
		if (model instanceof Edit)
			super.setModel(model);
		return this;
	}

	@Override
	public Edit getModel() {
		return (Edit)super.getModel();
	}
	
	protected Element processDescriptor(IChangeDescriptor cd) {
		Element el = null;
		for (IParticipant p : getParticipants()) {
			el = p.processDescriptor(cd);
			if (el != null)
				break;
		}
		return el;
	}
	
	protected Element processGroup(ChangeDescriptorGroup ed, Element e) {
		Element ch;
		for (IChangeDescriptor cd : ed) {
			if (cd instanceof ChangeDescriptorGroup) {
				ch = processGroup((ChangeDescriptorGroup)cd,
						newElement(EDIT, "edit:edit"));
			} else ch = processDescriptor(cd);
			if (ch != null)
				e.appendChild(ch);
		}
		return e;
	}
	
	@Override
	public Element processModel(Element e) throws SaveFailedException {
		return processGroup(getModel().getChildren(), e);
	}

	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(EDIT, "edit:edit"));
		processModel(getDocumentElement());
		finish();
	}

}
