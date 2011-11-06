package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.util.DOM;

public class ReactionRuleXMLExport extends XMLExport<ReactionRule> {

	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.RULE, "rule:rule"));
		processRule(getDocumentElement(), getModel());
		finish();
	}

	private Element processRule(Element e, ReactionRule rr) throws ExportFailedException {
		DOM.appendChildIfNotNull(e,
			processRedex(newElement(XMLNS.RULE, "rule:redex"), rr.getRedex()));
		return e;
	}
	
	private Element processRedex(Element e, Bigraph redex) throws ExportFailedException {
		DOM.applyAttributes(e,
				"xmlns:bigraph", XMLNS.BIGRAPH);
		BigraphXMLExport ex = new BigraphXMLExport();
		ex.setModel(redex);
		ex.setDocument(getDocument());
		return ex.processBigraph(e, ex.getModel());
	}
}
