package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.util.DOM;

public class ReactionRuleXMLImport extends Import<ReactionRule> {
	private ReactionRule rr = null;
	
	@Override
	public ReactionRule importObject() throws ImportFailedException {
		try {
			Document d = DOM.parse(source);
			source.close();
			return makeRule(d.getDocumentElement());
		} catch (Exception e) {
			if (e instanceof ImportFailedException) {
				throw (ImportFailedException)e;
			} else throw new ImportFailedException(e);
		}
	}

	private ReactionRule makeRule(Element e) throws ImportFailedException {
		rr = new ReactionRule();
		
		BigraphXMLImport im = new BigraphXMLImport();
		Bigraph b =
			im.makeBigraph(DOM.getNamedChildElement(e, XMLNS.RULE, "redex"));
		System.out.println(b);
		rr.setRedex(b);
		
		return rr;
	}
	
	public static ReactionRule importFile(IFile file) throws ImportFailedException {
		ReactionRuleXMLImport b = new ReactionRuleXMLImport();
		try {
			b.setInputStream(file.getContents());
		} catch (CoreException e) {
			throw new ImportFailedException(e);
		}
		return b.importObject().setFile(file);
	}
}
