package org.bigraph.model.savers;

import org.bigraph.model.Control;
import org.bigraph.model.ModelObject;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;

public class SignatureXMLSaver extends XMLSaver {
	public SignatureXMLSaver() {
		this(null);
	}
	
	public SignatureXMLSaver(ISaver parent) {
		super(parent);
		setDefaultNamespace(SIGNATURE);
	}
	
	@Override
	public Signature getModel() {
		return (Signature)super.getModel();
	}
	
	@Override
	public SignatureXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof Signature)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(SIGNATURE, "signature:signature"));
		processModel(getDocumentElement());
		finish();
	}

	@Override
	public Element processModel(Element e) throws SaveFailedException {
		Signature s = getModel();
		
		for (Signature t : s.getSignatures())
			appendChildIfNotNull(e, processOrReference(
					newElement(SIGNATURE, "signature:signature"),
					t, new SignatureXMLSaver(this)));
		
		for (Control c : s.getControls())
			appendChildIfNotNull(e,
				processControl(newElement(SIGNATURE, "signature:control"), c));
		return executeDecorators(s, e);
	}
	
	private Element processControl(Element e, Control c) {
		applyAttributes(e,
				"name", c.getName(),
				"kind", c.getKind().toString());
		
		for (PortSpec p : c.getPorts())
			e.appendChild(processPort(
				newElement(SIGNATURE, "signature:port"), p));
		
		return executeDecorators(c, e);
	}
	
	private Element processPort(Element e, PortSpec p) {
		return executeDecorators(p, applyAttributes(e, "name", p.getName()));
	}
}
