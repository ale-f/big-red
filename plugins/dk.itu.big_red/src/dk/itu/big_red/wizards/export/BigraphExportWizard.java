package dk.itu.big_red.wizards.export;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.wizards.export.assistants.WizardBigraphExportPage;
import dk.itu.big_red.wizards.export.assistants.WizardBigraphExportSelectorPage;

public class BigraphExportWizard extends Wizard implements IExportWizard {
	private WizardBigraphExportSelectorPage page1 = null;
	private WizardBigraphExportPage page2 = null;
	
	private Bigraph source = null;
	
	public Bigraph getSource() {
		return source;
	}
	
	public void setSource(Bigraph source) {
		this.source = source;
	}
	
	private IConfigurationElement cfe = null;
	private Saver exporter = null;
	
	@Override
	public boolean canFinish() {
		return (getContainer().getCurrentPage() == page2);
	}
	
	@Override
	public boolean performFinish() {
		return true;
	}
	
	@Override
	public void init(IWorkbench workbench, final IStructuredSelection selection) {
		page1 = new WizardBigraphExportSelectorPage("bigraphTextExportSelectorPage");
		page1.setTitle("Select a file format.");
		
		page2 = new WizardBigraphExportPage("bigraphTikZExportPage", selection);
		setWindowTitle("Export");
		
		addPage(page1);
		addPage(page2); 
	}

	public void setExporter(Object o) {
		if (o instanceof IConfigurationElement) {
			IConfigurationElement e = (IConfigurationElement)o;
			if (cfe != e) {
				try {
					exporter = (Saver)e.createExecutableExtension("class");
				} catch (CoreException ce) {
					return; /* XXX */
				}
				cfe = e;
				page2.setTitle("Export as " + e.getAttribute("name"));
				setWindowTitle("Export as " + e.getAttribute("name"));
				IConfigurationElement[] description =
					e.getChildren("description");
				if (description.length == 1)
					page2.setDescription(description[0].getValue());
				else page2.setDescription("Export the current bigraph in a textual form.");
				page2.reset();
			}
		}
	}

	public Saver getExporter() {
		return exporter;
	}
}
