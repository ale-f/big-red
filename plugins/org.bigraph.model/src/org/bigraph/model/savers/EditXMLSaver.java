package org.bigraph.model.savers;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bigraph.model.Edit;
import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Element;

public class EditXMLSaver extends XMLSaver {
	public interface Participant {
		void setSaver(IXMLSaver saver);
		
		Element processDescriptor(IChangeDescriptor cd);
	}
	
	private List<Participant> participants = new ArrayList<Participant>();
	
	public EditXMLSaver addParticipant(Participant one) {
		participants.add(one);
		one.setSaver(this);
		return this;
	}
	
	public EditXMLSaver addParticipants(
			Collection<? extends Participant> many) {
		for (Participant p : many)
			addParticipant(p);
		return this;
	}
	
	{
		addParticipant(new BigraphEditHandler());
	}
	
	protected List<Participant> getParticipants() {
		return participants;
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
		for (Participant p : getParticipants()) {
			el = p.processDescriptor(cd);
			if (el != null)
				break;
		}
		return el;
	}
	
	protected Element processGroup(ChangeDescriptorGroup cdg, Element e) {
		Element ch;
		for (IChangeDescriptor cd : cdg) {
			if (cd instanceof Edit) {
				ch = newElement(EDIT, "edit:edit");
				Edit ed = (Edit)cd;
				IFileWrapper
					me = getFile(),
					them = FileData.getFile(ed);
				if (me != null && them != null) {
					ch.setAttributeNS(null, "src", them.getRelativePath(
							me.getParent().getPath()));
				} else ch = processGroup(ed.getChildren(), ch);
			} else if (cd instanceof ChangeDescriptorGroup) {
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
