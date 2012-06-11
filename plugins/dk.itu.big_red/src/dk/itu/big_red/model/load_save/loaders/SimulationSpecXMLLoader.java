package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.utilities.resources.Project;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SPEC;

public class SimulationSpecXMLLoader extends XMLLoader {
	@Override
	public SimulationSpec importObject() throws LoadFailedException {
		try {
			Document d =
					validate(parse(source), "resources/schema/spec.xsd");
			SimulationSpec ss = makeObject(d.getDocumentElement());
			ExtendedDataUtilities.setFile(ss, getFile());
			return ss;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Signature makeSignature(Element e) throws LoadFailedException {
		String signaturePath = getAttributeNS(e, SPEC, "src");
		SignatureXMLLoader l = newLoader(SignatureXMLLoader.class);
		if (signaturePath != null && getFile() != null) {
			IFile f = Project.findFileByPath(
					getFile().getParent(), new Path(signaturePath));
			try {
				l.setFile(f).setInputStream(f.getContents());
			} catch (CoreException ex) {
				throw new LoadFailedException(ex);
			}
			return l.importObject();
		} else {
			return l.setFile(getFile()).makeObject(e);
		}
	}
	
	private Bigraph makeBigraph(Element e) throws LoadFailedException {
		String bigraphPath = getAttributeNS(e, SPEC, "src");
		BigraphXMLLoader l = newLoader(BigraphXMLLoader.class);
		if (bigraphPath != null && getFile() != null) {
			IFile f = Project.findFileByPath(
					getFile().getParent(), new Path(bigraphPath));
			try {
				l.setFile(f).setInputStream(f.getContents());
			} catch (CoreException ex) {
				throw new LoadFailedException(ex);
			}
			return l.importObject();
		} else {
			return l.setFile(getFile()).makeObject(e);
		}
	}
	
	private ReactionRule makeRule(Element e) throws LoadFailedException {
		String rulePath = getAttributeNS(e, SPEC, "src");
		ReactionRuleXMLLoader l = newLoader(ReactionRuleXMLLoader.class);
		if (rulePath != null && getFile() != null) {
			IFile f = Project.findFileByPath(
					getFile().getParent(), new Path(rulePath));
			try {
				l.setFile(f).setInputStream(f.getContents());
			} catch (CoreException ex) {
				throw new LoadFailedException(ex);
			}
			return l.importObject();
		} else {
			return l.setFile(getFile()).makeObject(e);
		}
	}
	
	@Override
	public SimulationSpec makeObject(Element e) throws LoadFailedException {
		SimulationSpec ss = new SimulationSpec();
		ChangeGroup cg = new ChangeGroup();
		
		Element signatureElement = getNamedChildElement(e, SPEC, "signature");
		if (signatureElement != null)
			cg.add(ss.changeSignature(makeSignature(signatureElement)));
		
		for (Element i : getNamedChildElements(e, SPEC, "rule"))
			cg.add(ss.changeAddRule(makeRule(i)));
		
		Element modelElement = getNamedChildElement(e, SPEC, "model");
		if (modelElement != null)
			cg.add(ss.changeModel(makeBigraph(modelElement)));
		
		try {
			if (cg.size() != 0)
				ss.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
		
		return executeUndecorators(ss, e);
	}
	
	@Override
	public SimulationSpecXMLLoader setFile(IFile f) {
		return (SimulationSpecXMLLoader)super.setFile(f);
	}
}
