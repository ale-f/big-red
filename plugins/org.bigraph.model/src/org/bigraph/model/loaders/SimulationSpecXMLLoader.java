package org.bigraph.model.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;
import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;

public class SimulationSpecXMLLoader extends XMLLoader {
	public SimulationSpecXMLLoader() {
	}
	
	public SimulationSpecXMLLoader(Loader parent) {
		super(parent);
	}
	
	@Override
	public SimulationSpec importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					Schemas.getSpecSchema());
			SimulationSpec ss = makeObject(d.getDocumentElement());
			FileData.setFile(ss, getFile());
			return ss;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	@Override
	public SimulationSpec makeObject(Element e) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(e, SPEC, "src");
		if (replacement != null)
			return loadRelative(replacement, SimulationSpec.class,
					new SimulationSpecXMLLoader(this));
		SimulationSpec ss = new SimulationSpec();
		
		Signature s = loadSub(
				selectFirst(
					getNamedChildElement(e, SIGNATURE, "signature"),
					getNamedChildElement(e, SPEC, "signature")),
				SPEC, Signature.class, new SignatureXMLLoader(this));
		if (s != null)
			addChange(ss.changeSignature(s));
		
		NodeList nl = e.getChildNodes();
		for (int i_ = 0; i_ < nl.getLength(); i_++) {
			Node n = nl.item(i_);
			if (!(n instanceof Element))
				continue;
			String ns = n.getNamespaceURI();
			if ((SPEC.equals(ns) || RULE.equals(ns)) &&
					"rule".equals(n.getLocalName())) {
				ReactionRule rr = loadSub((Element)n, SPEC, ReactionRule.class,
						new ReactionRuleXMLLoader(this));
				if (rr != null)
					addChange(ss.changeAddRule(rr));
			}
		}
		
		Bigraph b = loadSub(
				selectFirst(
					getNamedChildElement(e, BIGRAPH, "bigraph"),
					getNamedChildElement(e, SPEC, "model")),
				SPEC, Bigraph.class, new BigraphXMLLoader(this));
		if (b != null)
			addChange(ss.changeModel(b));
		
		executeUndecorators(ss, e);
		executeChanges(ss);
		return ss;
	}
	
	@Override
	public SimulationSpecXMLLoader setFile(IFileWrapper f) {
		return (SimulationSpecXMLLoader)super.setFile(f);
	}
}