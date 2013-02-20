package org.bigraph.bigmc.red.bgm;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.utilities.ui.StockButton;

public class ImportWizardPage extends WizardPage {
	public ImportWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName);
		setDescription("Import a BigMC file as a new Big Red project.");
	}
	
	private Text text;
	
	@Override
	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		
		text = new Text(c, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite buttonBar = new Composite(c, SWT.NONE);
		buttonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		buttonBar.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		
		StockButton.OPEN.create(buttonBar, 0, true);
		
		setControl(c);
	}
	
	public String getText() {
		return text.getText().trim();
	}
}
