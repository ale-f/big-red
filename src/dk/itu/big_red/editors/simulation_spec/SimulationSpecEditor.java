package dk.itu.big_red.editors.simulation_spec;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import dk.itu.big_red.util.UI;

public class SimulationSpecEditor extends EditorPart {

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInputWithNotify(input);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite base = new Composite(parent, SWT.NONE);
		base.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		base.setLayout(new GridLayout(3, false));
		
		UI.newLabel(base, SWT.RIGHT, "Signature:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		new ColorSelector(base).getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Button b = new Button(base, SWT.NONE);
		b.setText("&Change..."); b.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
		
		UI.newLabel(base, SWT.RIGHT, "Reaction rules:").setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		new List(base, SWT.BORDER).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		b = new Button(base, SWT.NONE);
		b.setText("Something..."); b.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
		
		UI.newLabel(base, SWT.RIGHT, "Model:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		new ColorSelector(base).getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		b = new Button(base, SWT.NONE);
		b.setText("A thing..."); b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		new Label(base, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		UI.newLabel(base, SWT.RIGHT, "Tool:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		new ColorSelector(base).getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		b = new Button(base, SWT.NONE);
		b.setText("Two thing(s)..."); b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
