package org.bigraph.model.loaders;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;

public class SignatureXMLLoader extends XMLLoader {
	public SignatureXMLLoader() {
	}
	
	public SignatureXMLLoader(Loader parent) {
		super(parent);
	}
	
	private Signature sig;
	
	@Override
	public Signature importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					Schemas.getSignatureSchema());
			Signature s = makeObject(d.getDocumentElement());
			FileData.setFile(s, getFile());
			return s;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private void makeControl(Element e) throws LoadFailedException {
		Control model = new Control();
		addChange(sig.changeAddControl(
				model, getAttributeNS(e, SIGNATURE, "name")));
		
		String kind = getAttributeNS(e, SIGNATURE, "kind");
		if (kind != null) {
			addChange(model.changeKind(
				kind.equals("active") ? Kind.ACTIVE :
				kind.equals("passive") ? Kind.PASSIVE : Kind.ATOMIC));
		}
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "port"))
			makePortSpec(j, model);
		
		executeUndecorators(model, e);
	}
	
	private void makeSignature(Element e) throws LoadFailedException {
		SignatureXMLLoader si = new SignatureXMLLoader(this);
		Signature t = si.makeObject(e);
		if (t != null)
			addChange(new BoundDescriptor(sig,
					new Signature.ChangeAddSignatureDescriptor(
							new Signature.Identifier(), -1, t)));
	}
	
	@Override
	public Signature makeObject(Element e) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(e, SIGNATURE, "src");
		if (replacement != null) {
			return loadRelative(replacement, Signature.class,
					new SignatureXMLLoader(this));
		} else sig = new Signature();
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "signature"))
			makeSignature(j);
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "control"))
			makeControl(j);
		
		executeUndecorators(sig, e);
		executeChanges(sig);
		return sig;
	}
	
	private PortSpec makePortSpec(Element e, Control c) {
		PortSpec model = new PortSpec();
		addChange(
				c.changeAddPort(model, getAttributeNS(e, SIGNATURE, "name")));
		return executeUndecorators(model, e);
	}
	
	@Override
	public SignatureXMLLoader setFile(IFileWrapper f) {
		return (SignatureXMLLoader)super.setFile(f);
	}
}
