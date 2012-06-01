package dk.itu.big_red.wizards.creation.assistants;

import org.eclipse.jface.viewers.IStructuredSelection;

public class WizardNewRuleCreationPage extends WizardNewAgentCreationPage {
	public WizardNewRuleCreationPage(
			String pageName, IStructuredSelection selection) {
		super(pageName, selection);
		setPageComplete(false);
	}
	
	@Override
	public String getFileName() {
		String s = nameText.getText().trim();
		return (!s.endsWith(".bigraph-rule") ? s + ".bigraph-rule" : s);
	}
}
