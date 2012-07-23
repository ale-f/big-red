package dk.itu.big_red.model.load_save.savers;

import org.bigraph.model.Control;
import org.bigraph.model.ModelObject;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.names.policies.BooleanNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.LongNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;
import org.bigraph.model.savers.SaveFailedException;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SIGNATURE;

public class SignatureXMLSaver extends XMLSaver {
	public SignatureXMLSaver() {
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
		processObject(getDocumentElement(), getModel());
		finish();
	}

	@Override
	public Element processObject(Element e, Object s_) throws SaveFailedException {
		if (!(s_ instanceof Signature))
			throw new SaveFailedException(s_ + " isn't a Signature");
		Signature s = (Signature)s_;
		
		applyAttributes(e, "xmlns:big-red", BIG_RED);
		
		for (Control c : s.getControls())
			appendChildIfNotNull(e,
				processControl(newElement(SIGNATURE, "signature:control"), c));
		return executeDecorators(s, e);
	}
	
	private Element processControl(Element e, Control c) {
		applyAttributes(e,
				"name", c.getName(),
				"kind", c.getKind().toString());
		
		INamePolicy parameterPolicy = ExtendedDataUtilities.getParameterPolicy(c);
		if (parameterPolicy instanceof LongNamePolicy) {
			applyAttributes(e, "parameter", "LONG");
		} else if (parameterPolicy instanceof StringNamePolicy) {
			applyAttributes(e, "parameter", "STRING");
		} else if (parameterPolicy instanceof BooleanNamePolicy) {
			applyAttributes(e, "parameter", "BOOLEAN");
		}
		
		for (PortSpec p : c.getPorts())
			e.appendChild(processPort(
				newElement(SIGNATURE, "signature:port"), p));
		
		return executeDecorators(c, e);
	}
	
	private Element processPort(Element e, PortSpec p) {
		return executeDecorators(p, applyAttributes(e, "name", p.getName()));
	}
}
