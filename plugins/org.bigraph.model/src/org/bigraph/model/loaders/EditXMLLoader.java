package org.bigraph.model.loaders;

import org.bigraph.model.Edit;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;

@SuppressWarnings("deprecation")
public class EditXMLLoader extends XMLLoader {
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
		return null;
	}
	
	@Override
	protected Edit makeObject(Element el) throws LoadFailedException {
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
			
			IChangeDescriptor cd = null;
			if (EDIT.equals(i.getNamespaceURI())) {
				cd = new EditXMLLoader(this).makeObject(i);
			} else makeDescriptor(el);
			
			if (cd != null)
				addChange(ed.changeDescriptorAdd(index++, cd));
		}
		
		executeUndecorators(ed, el);
		executeChanges(ed);
		return ed;
	}
}
