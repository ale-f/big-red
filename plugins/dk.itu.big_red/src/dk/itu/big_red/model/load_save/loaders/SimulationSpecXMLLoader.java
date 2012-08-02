package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.Schemas;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.load_save.LoaderUtilities;

import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;

public class SimulationSpecXMLLoader extends XMLLoader {
	@Override
	public SimulationSpec importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					Schemas.getSpecSchema());
			SimulationSpec ss = makeObject(d.getDocumentElement());
			FileData.setFile(ss, getFile());
			return ss;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Signature makeSignature(Element e) throws LoadFailedException {
		return loadEmbedded(e, SPEC, "src", Signature.class,
				new SignatureXMLLoader().addNewUndecorators(getUndecorators()));
	}
	
	private Bigraph makeBigraph(Element e) throws LoadFailedException {
		return loadEmbedded(e, SPEC, "src", Bigraph.class,
				new BigraphXMLLoader().addNewUndecorators(getUndecorators()));
	}
	
	private ReactionRule makeRule(Element e) throws LoadFailedException {
		return loadEmbedded(e, SPEC, "src", ReactionRule.class,
				new ReactionRuleXMLLoader().addNewUndecorators(getUndecorators()));
	}
	
	@Override
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
		return ss;
	}
	
	@Override
	public SimulationSpecXMLLoader setFile(IFileWrapper f) {
		return (SimulationSpecXMLLoader)super.setFile(f);
	}
}
