package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.ReactionRule;

public class ReactionRuleXMLImport extends Import<ReactionRule> {

	@Override
	public ReactionRule importObject() throws ImportFailedException {
		return new ReactionRule();
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
