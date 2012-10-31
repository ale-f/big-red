package org.bigraph.bigmc.red.bgm;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class ImportWizardPage extends WizardPage {
	public ImportWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName);
		setDescription("Import a BigMC file as a new Big Red project.");
	}
	
	@Override
	public void createControl(Composite parent) {
		setControl(new Composite(parent, 0));
	}
}
