package org.bigraph.model.loaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bigraph.model.Edit;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;

public class EditXMLLoader extends XMLLoader {
	protected interface IParticipant {
		IChangeDescriptor getDescriptor(Element descriptor);
		IChangeDescriptor getRenameDescriptor(Element id, String name);
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
					cd = makeRename(el);
				}
			} else cd = makeDescriptor(el);
			
			if (cd != null)
				addChange(ed.changeDescriptorAdd(index++, cd));
		}
		
		executeUndecorators(ed, el);
		executeChanges(ed);
		return ed;
	}
}
