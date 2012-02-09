package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Project;

public class SimulationSpecXMLImport extends Import implements IFileBackable {

	@Override
	public SimulationSpec importObject() throws ImportFailedException {
		try {
			Document d = DOM.parse(source);
			return makeSpec(d.getDocumentElement()).setFile(getFile());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
	
	private Signature makeSignature(Element e) throws ImportFailedException {
		String signaturePath =
				DOM.getAttributeNS(e, XMLNS.SPEC, "src");
		if (signaturePath != null && getFile() != null) {
			return (Signature)Import.fromFile(
					Project.findFileByPath(getFile().getParent(),
							new Path(signaturePath)));
		} else {
			return new SignatureXMLImport().makeSignature(e);
		}
	}
	
	private Bigraph makeBigraph(Element e) throws ImportFailedException {
		String bigraphPath =
				DOM.getAttributeNS(e, XMLNS.SPEC, "src");
		if (bigraphPath != null && getFile() != null) {
			return (Bigraph)Import.fromFile(
					Project.findFileByPath(getFile().getParent(),
							new Path(bigraphPath)));
		} else {
			return new BigraphXMLImport().setFile(getFile()).makeBigraph(e);
		}
	}
	
	private ReactionRule makeRule(Element e) throws ImportFailedException {
		String rulePath =
				DOM.getAttributeNS(e, XMLNS.SPEC, "src");
		if (rulePath != null && getFile() != null) {
			return (ReactionRule)Import.fromFile(
					Project.findFileByPath(getFile().getParent(),
							new Path(rulePath)));
		} else {
			return new ReactionRuleXMLImport().setFile(getFile()).makeRule(e);
		}
	}
	
	public SimulationSpec makeSpec(Element e) throws ImportFailedException {
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
			throw new ImportFailedException(cre);
		}
		
		return ss;
	}

	private IFile file;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public SimulationSpecXMLImport setFile(IFile file) {
		this.file = file;
		return this;
	}
}
