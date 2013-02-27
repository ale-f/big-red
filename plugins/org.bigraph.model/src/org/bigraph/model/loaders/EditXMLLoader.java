package org.bigraph.model.loaders;

import org.bigraph.model.Edit;
import org.bigraph.model.Edit.ChangeDescriptorAddDescriptor;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
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

	private IChangeDescriptor makeDescriptor(Element el) {
		IChangeDescriptor cd = null;
		for (Participant p : getParticipants(Participant.class)) {
			cd = p.getDescriptor(el);
			if (cd != null)
				break;
		}
		return cd;
	}
	
	private static Element getFirstChildElement(Node n) {
		for (Element i :
				forNodeList(n.getChildNodes()).filter(Element.class))
			return i;
		return null;
	}
	
	private IChangeDescriptor makeRename(Element el) {
		Element id = getFirstChildElement(el);
		if (id == null)
			return null;
		String name = getAttributeNS(el, EDIT, "name");
		
		IChangeDescriptor cd = null;
		for (Participant p : getParticipants(Participant.class)) {
			cd = p.getRenameDescriptor(id, name);
			if (cd != null)
				break;
		}
		return cd;
	}
	
	private final Edit edit = new Edit();
	
	@Override
	public Resolver getResolver() {
		return edit;
	}
	
	@Override
	public Edit makeObject(Element el) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(el, EDIT, "src");
		if (replacement != null)
			return loadRelative(replacement, Edit.class,
					new EditXMLLoader(this));
		executeUndecorators(edit, el);
		
		int index = 0;
		for (Element i :
				forNodeList(el.getChildNodes()).filter(Element.class)) {
			String
				localName = i.getLocalName(),
				namespaceURI = i.getNamespaceURI();
			
			IChangeDescriptor cd = null;
			if (EDIT.equals(namespaceURI)) {
				if ("edit".equals(localName)) {
					cd =
						new EditXMLLoader(this).makeObject(i).getDescriptors();
				} else if ("rename".equals(localName)) {
					cd = makeRename(i);
				}
			} else cd = makeDescriptor(i);
			
			if (cd != null) {
				addChange(new ChangeDescriptorAddDescriptor(
						new Edit.Identifier(), index++, cd));
			} else throw new LoadFailedException(
					"Couldn't create a change descriptor from element " + i);
		}
		
		executeChanges();
		return edit;
	}
}
