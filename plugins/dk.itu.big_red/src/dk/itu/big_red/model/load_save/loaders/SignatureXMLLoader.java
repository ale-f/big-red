package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.names.policies.BooleanNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.LongNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;
import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.load_save.LoadFailedException;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SIGNATURE;

public class SignatureXMLLoader extends XMLLoader {
	private ChangeGroup cg = new ChangeGroup();
	private Signature sig;
	
	@Override
	public Signature importObject() throws LoadFailedException {
		try {
			Document d =
				validate(parse(source), "resources/schema/signature.xsd");
			Signature s = makeObject(d.getDocumentElement());
			ExtendedDataUtilities.setFile(s, getFile());
			return s;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private void makeControl(Element e) throws LoadFailedException {
		Control model = new Control();
		cg.add(sig.changeAddControl(model));
		cg.add(model.changeName(getAttributeNS(e, SIGNATURE, "name")));
		
		String kind = getAttributeNS(e, SIGNATURE, "kind");
		if (kind != null) {
			cg.add(model.changeKind(
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
				cg.add(ExtendedDataUtilities.changeParameterPolicy(model, n));
		}
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "port"))
			makePortSpec(j, model);
		
		executeUndecorators(model, e);
	}
	
	@Override
	public Signature makeObject(Element e) throws LoadFailedException {
		sig = new Signature();
		
		cg.clear();
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "control"))
			makeControl(j);
		
		try {
			if (cg.size() != 0)
				sig.tryApplyChange(cg);
		} catch (ChangeRejectedException ex) {
			throw new LoadFailedException(ex);
		}
		
		return executeUndecorators(sig, e);
	}
	
	private PortSpec makePortSpec(Element e, Control c) {
		PortSpec model = new PortSpec();
		cg.add(c.changeAddPort(model, getAttributeNS(e, SIGNATURE, "name")));
		return executeUndecorators(model, e);
	}
	
	@Override
	public SignatureXMLLoader setFile(IFile f) {
		return (SignatureXMLLoader)super.setFile(f);
	}
}
