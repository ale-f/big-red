package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.names.policies.BooleanNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.LongNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SIGNATURE;

public class SignatureXMLLoader extends XMLLoader {
	private Signature sig;
	
	@Override
	public Signature importObject() throws LoadFailedException {
		try {
			Document d =
				validate(parse(getInputStream()), "resources/schema/signature.xsd");
			Signature s = makeObject(d.getDocumentElement());
			FileData.setFile(s, getFile());
			return s;
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
		
		String parameter = getAttributeNS(e, SIGNATURE, "parameter");
		if (parameter != null) {
			INamePolicy n = null;
			if (parameter.equals("LONG")) {
				n = new LongNamePolicy();
			} else if (parameter.equals("STRING")) {
				n = new StringNamePolicy();
			} else if (parameter.equals("BOOLEAN")) {
				n = new BooleanNamePolicy();
			}
			if (n != null)
				addChange(
						ExtendedDataUtilities.changeParameterPolicy(model, n));
		}
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "port"))
			makePortSpec(j, model);
		
		executeUndecorators(model, e);
	}
	
	public Signature makeObject(Element e) throws LoadFailedException {
		sig = new Signature();
		
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
