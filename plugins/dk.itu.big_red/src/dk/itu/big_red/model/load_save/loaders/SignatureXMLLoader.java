package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Control;
import org.bigraph.model.ModelObject;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.resources.IResourceWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SIGNATURE;

public class SignatureXMLLoader extends XMLLoader {
	private Signature sig;
	
	@Override
	public Signature importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					RedPlugin.getResource("resources/schema/signature.xsd"));
			return makeObject(d.getDocumentElement());
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private void makeControl(Element e) throws LoadFailedException {
		Control model = new Control();
		addChange(sig.changeAddControl(model));
		addChange(model.changeName(getAttributeNS(e, SIGNATURE, "name")));
		
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
	
	public Signature makeObject(Element e) throws LoadFailedException {
		String replacement = getAttributeNS(e, SIGNATURE, "src");
		if (replacement != null) {
			if (getFile() == null)
				 throw new Error("BUG: relative path to resolve, " +
							"but no IFileWrapper set on SignatureXMLLoader");
			IResourceWrapper rw =
					getFile().getParent().getResource(replacement);
			if (rw instanceof IFileWrapper) {
				ModelObject mo = ((IFileWrapper)rw).load();
				if (mo instanceof Signature) {
					return (Signature)mo;
				} else throw new LoadFailedException(
						"Referenced document is not a signature");
			} else throw new LoadFailedException(
					"Referenced document is not valid");
		}
		
		sig = new Signature();
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "control"))
			makeControl(j);
		
		executeUndecorators(sig, e);
		executeChanges(sig);
		FileData.setFile(sig, getFile());
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
