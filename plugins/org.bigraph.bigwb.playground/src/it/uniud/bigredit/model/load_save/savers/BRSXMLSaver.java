package it.uniud.bigredit.model.load_save.savers;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIG_RED;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.savers.BigraphXMLSaver;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;

import org.bigraph.model.savers.XMLSaver;

public class BRSXMLSaver extends XMLSaver {
	
	public static final String BRS =
			"http://www.itu.dk/research/pls/xmlns/2012/brs";
	
	public static final String BIGREDIT_EDIT =
			"http://www.itu.dk/research/pls/xmlns/2012/b";
	
	public BRSXMLSaver() {
		setDefaultNamespace(BRS);
	}
	
	@Override
	public BRS getModel() {
		return (BRS)super.getModel();
	}
	
	@Override
	public BRSXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof BRS)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(BRS, "brs:brs"));
		processModel(getDocumentElement());
		finish();
	}
	
	@Override
	public Element processModel(Element e) throws SaveFailedException {
		BRS ss = getModel();
		
//		appendChildIfNotNull(e,
//			processOrReference(newElement(BRS, "brs:signature"),
//				ss.getSignature(), SignatureXMLSaver.class));
		
		appendChildIfNotNull(e,
				processOrReference(
					newElement(SIGNATURE, "signature:signature"),
					ss.getSignature(), new SignatureXMLSaver(this)));
		
		for (Reaction rr : ss.getRules()){
			Element t1=newElement(BRS, "brs:rule");
			rr.setSign(ss.getSignature());
			ReactionXMLSaver rxs = new ReactionXMLSaver();
			rxs.setDocument(getDocument());
			appendChildIfNotNull(e, processOrReference(t1, rr, rxs));
			Element pE = newElement(BIG_RED, "big-red:appearance");
			Rectangle rect= ss.getChildrenConstraint(rr);
			rectangleToElement(pE, rect);
			t1.appendChild(pE);

		}
		

		for (Bigraph bb : ss.getModels()){
			bb.setSignature(ss.getSignature());
			Element t1=newElement(BRS, "brs:model");
			
			appendChildIfNotNull(e,
					processOrReference(t1,
							bb, new BigraphXMLSaver(this)));
			
			Element pE = newElement(BIG_RED, "big-red:appearance");
			Rectangle rect= ss.getChildrenConstraint(bb);
			rectangleToElement(pE, rect);
			t1.appendChild(pE);
		}
		
		return executeDecorators(ss, e);
	}
	
	
	public static Element rectangleToElement(Element e, Rectangle r) {
		return XMLSaver.applyAttributes(e,
			"width", r.width(), "height", r.height(), "x", r.x(), "y", r.y());
	}
}