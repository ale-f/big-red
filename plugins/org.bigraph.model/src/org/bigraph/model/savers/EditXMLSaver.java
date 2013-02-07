package org.bigraph.model.savers;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;

import org.bigraph.model.Edit;
import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.process.IParticipant;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Element;

public class EditXMLSaver extends XMLSaver {
	public EditXMLSaver() {
		this(null);
	}
	
	public EditXMLSaver(ISaver parent) {
		super(parent);
		setDefaultNamespace(EDIT);
	}
	
	public interface Participant extends IParticipant {
		Element processDescriptor(IChangeDescriptor cd);
	}
	
	{
		addParticipant(new BigraphEditSaver());
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
		for (Participant p : getParticipants(Participant.class)) {
			el = p.processDescriptor(cd);
			if (el != null)
				break;
		}
		return el;
	}
	
	protected Element processGroup(IChangeDescriptor.Group cdg, Element e) {
		if (cdg.size() == 0)
			return null;
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
				} else ch = processGroup(ed.getDescriptors(), ch);
			} else if (cd instanceof IChangeDescriptor.Group) {
				ch = processGroup((IChangeDescriptor.Group)cd,
						newElement(EDIT, "edit:edit"));
			} else ch = processDescriptor(cd);
			if (ch != null)
				e.appendChild(ch);
		}
		return e;
	}
	
	@Override
	public Element processModel(Element e) throws SaveFailedException {
		return processGroup(getModel().getDescriptors(), e);
	}

	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(EDIT, "edit:edit"));
		processModel(getDocumentElement());
		finish();
	}
}
