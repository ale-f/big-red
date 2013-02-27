package org.bigraph.model.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;
import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;
import static org.bigraph.model.utilities.ArrayIterable.forNodeList;

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
	
	private final SimulationSpec ss = new SimulationSpec();
	
	@Override
	public Resolver getResolver() {
		return ss;
	}
	
	@Override
	public SimulationSpec makeObject(Element e) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(e, SPEC, "src");
		if (replacement != null)
			return loadRelative(replacement, SimulationSpec.class,
					new SimulationSpecXMLLoader(this));
		executeUndecorators(ss, e);
		
		Signature s = loadSub(
				selectFirst(
					getNamedChildElement(e, SIGNATURE, "signature"),
					getNamedChildElement(e, SPEC, "signature")),
				SPEC, Signature.class, new SignatureXMLLoader(this));
		if (s != null)
			addChange(new SimulationSpec.ChangeSetSignatureDescriptor(
					new SimulationSpec.Identifier(), null, s));
		
		int index = 0;
		for (Element n :
				forNodeList(e.getChildNodes()).filter(Element.class)) {
			String ns = n.getNamespaceURI();
			if ((SPEC.equals(ns) || RULE.equals(ns)) &&
					"rule".equals(n.getLocalName())) {
				ReactionRule rr = loadSub(n, SPEC, ReactionRule.class,
						new ReactionRuleXMLLoader(this));
				if (rr != null)
					addChange(new SimulationSpec.ChangeAddRuleDescriptor(
							new SimulationSpec.Identifier(), index++, rr));
			}
		}
		
		Bigraph b = loadSub(
				selectFirst(
					getNamedChildElement(e, BIGRAPH, "bigraph"),
					getNamedChildElement(e, SPEC, "model")),
				SPEC, Bigraph.class, new BigraphXMLLoader(this));
		if (b != null)
			addChange(new SimulationSpec.ChangeSetModelDescriptor(
					new SimulationSpec.Identifier(), null, b));
		
		executeChanges();
		return ss;
	}
	
	@Override
	public SimulationSpecXMLLoader setFile(IFileWrapper f) {
		return (SimulationSpecXMLLoader)super.setFile(f);
	}
}
