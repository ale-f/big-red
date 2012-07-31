package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.load_save.LoaderUtilities;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SPEC;

public class SimulationSpecXMLLoader extends XMLLoader {
	@Override
	public SimulationSpec importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					LoaderUtilities.getSpecSchema());
			return makeObject(d.getDocumentElement());
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Signature makeSignature(Element e) throws LoadFailedException {
		String signaturePath = getAttributeNS(e, SPEC, "src");
		SignatureXMLLoader si = new SignatureXMLLoader();
		if (signaturePath != null && getFile() != null) {
			return loadRelative(signaturePath, Signature.class, si);
		} else return si.setFile(getFile()).makeObject(e);
	}
	
	private Bigraph makeBigraph(Element e) throws LoadFailedException {
		String bigraphPath = getAttributeNS(e, SPEC, "src");
		BigraphXMLLoader bi = new BigraphXMLLoader();
		if (bigraphPath != null && getFile() != null) {
			return loadRelative(bigraphPath, Bigraph.class, bi);
		} else return bi.setFile(getFile()).makeObject(e);
	}
	
	private ReactionRule makeRule(Element e) throws LoadFailedException {
		String rulePath = getAttributeNS(e, SPEC, "src");
		ReactionRuleXMLLoader re = new ReactionRuleXMLLoader();
		if (rulePath != null && getFile() != null) {
			return loadRelative(rulePath, ReactionRule.class, re);
		} else {
			return re.setFile(getFile()).makeObject(e);
		}
	}
	
	public SimulationSpec makeObject(Element e) throws LoadFailedException {
		SimulationSpec ss = loadRelative(
				getAttributeNS(e, SPEC, "src"), SimulationSpec.class, this);
		if (ss != null) {
			return ss;
		} else ss = new SimulationSpec();
		
		Element signatureElement = getNamedChildElement(e, SPEC, "signature");
		if (signatureElement != null)
			addChange(ss.changeSignature(makeSignature(signatureElement)));
		
		for (Element i : getNamedChildElements(e, SPEC, "rule"))
			addChange(ss.changeAddRule(makeRule(i)));
		
		Element modelElement = getNamedChildElement(e, SPEC, "model");
		if (modelElement != null)
			addChange(ss.changeModel(makeBigraph(modelElement)));
		
		executeUndecorators(ss, e);
		executeChanges(ss);
		FileData.setFile(ss, getFile());
		return ss;
	}
	
	@Override
	public SimulationSpecXMLLoader setFile(IFileWrapper f) {
		return (SimulationSpecXMLLoader)super.setFile(f);
	}
}
