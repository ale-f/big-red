package org.bigraph.model.savers;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;

import java.util.Collections;
import java.util.List;

import org.bigraph.model.Edit;
import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.w3c.dom.Element;

@Deprecated
public class EditXMLSaver extends XMLSaver {
	protected interface IParticipant {
		Element processDescriptor(IChangeDescriptor cd);
	}
	
	private List<IParticipant> participants;
	
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
			ch = null;
			if (cd instanceof ChangeDescriptorGroup) {
				ch = processGroup((ChangeDescriptorGroup)cd,
						newElement(EDIT, "edit:edit"));
			} else ch = processDescriptor(ed);
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
