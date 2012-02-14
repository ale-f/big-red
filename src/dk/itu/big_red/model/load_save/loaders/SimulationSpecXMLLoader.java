package dk.itu.big_red.model.load_save.loaders;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.XMLNS;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Project;

public class SimulationSpecXMLLoader extends Loader implements IFileBackable {

	@Override
	public SimulationSpec importObject() throws LoadFailedException {
		try {
			Document d = DOM.parse(source);
			return makeSpec(d.getDocumentElement()).setFile(getFile());
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Signature makeSignature(Element e) throws LoadFailedException {
		String signaturePath =
				DOM.getAttributeNS(e, XMLNS.SPEC, "src");
		if (signaturePath != null && getFile() != null) {
			return (Signature)Loader.fromFile(
					Project.findFileByPath(getFile().getParent(),
							new Path(signaturePath)));
		} else {
			return new SignatureXMLLoader().makeSignature(e);
		}
	}
	
	private Bigraph makeBigraph(Element e) throws LoadFailedException {
		String bigraphPath =
				DOM.getAttributeNS(e, XMLNS.SPEC, "src");
		if (bigraphPath != null && getFile() != null) {
			return (Bigraph)Loader.fromFile(
					Project.findFileByPath(getFile().getParent(),
							new Path(bigraphPath)));
		} else {
			return new BigraphXMLLoader().setFile(getFile()).makeBigraph(e);
		}
	}
	
	private ReactionRule makeRule(Element e) throws LoadFailedException {
		String rulePath =
				DOM.getAttributeNS(e, XMLNS.SPEC, "src");
		if (rulePath != null && getFile() != null) {
			return (ReactionRule)Loader.fromFile(
					Project.findFileByPath(getFile().getParent(),
							new Path(rulePath)));
		} else {
			return new ReactionRuleXMLLoader().setFile(getFile()).makeRule(e);
		}
	}
	
	public SimulationSpec makeSpec(Element e) throws LoadFailedException {
		SimulationSpec ss = new SimulationSpec();
		ChangeGroup cg = new ChangeGroup();
		
		Element signatureElement =
				DOM.getNamedChildElement(e, XMLNS.SPEC, "signature");
		if (signatureElement != null)
			cg.add(ss.changeSignature(makeSignature(signatureElement)));
		
		for (Element i :
		     DOM.getNamedChildElements(e, XMLNS.SPEC, "rule"))
			cg.add(ss.changeAddRule(makeRule(i)));
		
		Element modelElement =
				DOM.getNamedChildElement(e, XMLNS.SPEC, "model");
		if (modelElement != null)
			cg.add(ss.changeModel(makeBigraph(modelElement)));
		
		try {
			if (cg.size() != 0)
				ss.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
		
		return ss;
	}

	private IFile file;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public SimulationSpecXMLLoader setFile(IFile file) {
		this.file = file;
		return this;
	}
}
