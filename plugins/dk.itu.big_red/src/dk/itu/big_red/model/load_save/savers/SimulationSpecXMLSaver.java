package dk.itu.big_red.model.load_save.savers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;

public class SimulationSpecXMLSaver extends XMLSaver {
	@Override
	public SimulationSpec getModel() {
		return (SimulationSpec)super.getModel();
	}
	
	@Override
	public SimulationSpecXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof SimulationSpec)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(IRedNamespaceConstants.SPEC, "spec:spec"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object ss_) throws SaveFailedException {
		if (!(ss_ instanceof SimulationSpec))
			throw new SaveFailedException(ss_ + " isn't a SimulationSpec");
		SimulationSpec ss = (SimulationSpec)ss_;
		IFile ssFile = ss.getFile();
		IContainer ssContainer = (ssFile != null ? ssFile.getParent() : null);
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(IRedNamespaceConstants.SPEC, "spec:signature"),
				ssContainer, ss.getSignature(), SignatureXMLSaver.class));
		
		for (ReactionRule rr : ss.getRules())
			appendChildIfNotNull(e,
				processOrReference(
					newElement(IRedNamespaceConstants.SPEC, "spec:rule"),
					ssContainer, rr, ReactionRuleXMLSaver.class));
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(IRedNamespaceConstants.SPEC, "spec:model"),
				ssContainer, ss.getModel(), BigraphXMLSaver.class));
		
		return e;
	}
}
