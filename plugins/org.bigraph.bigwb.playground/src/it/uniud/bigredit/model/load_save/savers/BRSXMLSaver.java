package it.uniud.bigredit.model.load_save.savers;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SPEC;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;

import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import dk.itu.big_red.model.load_save.savers.ReactionRuleXMLSaver;
import dk.itu.big_red.model.load_save.savers.SignatureXMLSaver;
import dk.itu.big_red.model.load_save.savers.XMLSaver;

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
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object ss_) throws SaveFailedException {
		if (!(ss_ instanceof BRS))
			throw new SaveFailedException(ss_ + " isn't a valid BRS");
		BRS ss = (BRS)ss_;
		
		appendChildIfNotNull(e,
			processOrReference(newElement(BRS, "brs:signature"),
				ss.getSignature(), SignatureXMLSaver.class));
		
		for (Reaction rr : ss.getRules()){
			appendChildIfNotNull(e,
				processOrReference(newElement(BRS, "brs:rule"),
					rr, ReactionRuleXMLSaver.class));
			
//			Element pE = newElement(BIGREDIT_EDIT, "bigredit:rectangle");
//			pE.setAttributeNS(BIGREDIT_EDIT, "big-red:x", "" + rr.);
//			pE.setAttributeNS(BIGREDIT_EDIT, "big-red:y", "" + p.y);
//			pE.setAttributeNS(BIGREDIT_EDIT, "big-red:width", "" + p.y);
//			pE.setAttributeNS(BIGREDIT_EDIT, "big-red:hight", "" + p.y);
//			
//			appendChildIfNotNull(e, pE);
		}
		
		

		
		
		
		
		for (Bigraph bb : ss.getModels()){
			bb.setSignature(ss.getSignature());
			Element t1=newElement(BRS, "brs:model");
			
			appendChildIfNotNull(e,
					processOrReference(t1,
							bb, BigraphXMLSaver.class));
			
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