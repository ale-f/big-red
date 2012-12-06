package org.bigraph.model.loaders;

import org.bigraph.model.Edit;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.process.IParticipant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.utilities.ArrayIterable.forNodeList;

public class EditXMLLoader extends XMLLoader {
	public interface Participant extends IParticipant {
		IChangeDescriptor getDescriptor(Element descriptor);
		IChangeDescriptor getRenameDescriptor(Element id, String name);
	}
	
	{
		addParticipant(new BigraphEditLoader());
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

	private final Participant bigraphEditHandler =
			new BigraphEditLoader();
	
	private IChangeDescriptor makeDescriptor(Element el) {
		IChangeDescriptor cd = null;
		if ((cd = bigraphEditHandler.getDescriptor(el)) != null)
			return cd;
		for (Participant p : getParticipants(Participant.class)) {
			cd = p.getDescriptor(el);
			if (cd != null)
				break;
		}
		return cd;
	}
	
	private IChangeDescriptor makeRename(Element el) {
		Node id_ = el.getFirstChild();
		if (!(id_ instanceof Element))
			return null;
		Element id = (Element)id_;
		String name = getAttributeNS(el, EDIT, "name");
		
		IChangeDescriptor cd = null;
		if ((cd = bigraphEditHandler.getRenameDescriptor(id, name)) != null)
			return cd;
		for (Participant p : getParticipants(Participant.class)) {
			cd = p.getRenameDescriptor(id, name);
			if (cd != null)
				break;
		}
		return cd;
	}
	
	@Override
	public Edit makeObject(Element el) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(el, EDIT, "src");
		if (replacement != null)
			return loadRelative(replacement, Edit.class,
					new EditXMLLoader(this));
		Edit ed = new Edit();
		
		int index = 0;
		for (Element i :
				forNodeList(el.getChildNodes()).filter(Element.class)) {
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
