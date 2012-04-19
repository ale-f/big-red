package dk.itu.big_red.editors.simulation_spec;


import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import dk.itu.big_red.interaction_managers.InteractionManager;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Saver.Option;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.ui.UI;

class BasicCommandLineInteractionManager extends InteractionManager {
	public static final int TO_TOOL_ID = 1000;
	public static final String TO_TOOL_LABEL = "To tool...";
	
	public static final int SAVE_ID = 1001;
	public static final String SAVE_LABEL = "Save...";
	
	public static final int COPY_ID = 1002;
	public static final String COPY_LABEL = "Copy";
	
	private Saver exporter;
	
	public BasicCommandLineInteractionManager(Saver exporter) {
		this.exporter = exporter;
	}
	
	@Override
	public void run(Shell parent) {
		try {
			IOAdapter io = new IOAdapter();
			exporter.setModel(getSimulationSpec()).
				setOutputStream(io.getOutputStream());
			int r =
				(exporter.getOptions().size() != 0 ?
					createOptionsWindow(parent).open() :
					Dialog.OK);
			if (r == Dialog.OK) {
				exporter.exportObject();
				new ExportResults(IOAdapter.readString(io.getInputStream())).
					new ExportResultsDialog(parent).open();
			}
		} catch (SaveFailedException ex) {
			ex.printStackTrace();
		}
	}

	private Dialog createOptionsWindow(Shell shell) {
		return new TitleAreaDialog(shell) {
			@Override
			protected Control createDialogArea(Composite parent) {
				Composite c = (Composite)super.createDialogArea(parent);
				
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.heightHint = 0;
				ScrolledComposite sc =
					UI.chain(new ScrolledComposite(c, SWT.V_SCROLL)).
					layoutData(gd).done();
				
				RowLayout rl = new RowLayout(SWT.VERTICAL);
				rl.marginLeft = rl.marginRight = rl.marginTop = 10;
				rl.spacing = 5;
				Composite optionsGroup =
					UI.chain(new Composite(sc, SWT.NONE)).layout(rl).
					done();
				
				sc.setContent(optionsGroup);
				
				ArrayList<Control> optionControls = new ArrayList<Control>();
				for (final Option d : exporter.getOptions()) {
					Composite opt = new Composite(optionsGroup, SWT.NONE);
					opt.setLayout(new RowLayout(SWT.VERTICAL));
					optionControls.add(opt);
					
					Object ov = d.get();
					if (ov instanceof Boolean) {
						final Button b =
							UI.chain(new Button(opt, SWT.CHECK)).
							text(d.getDescription()).done();
						b.setSelection((Boolean)ov);
						b.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								d.set(b.getSelection());
							}
						});
					}
				}
				
				optionsGroup.setSize(
					optionsGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				
				setTitle("Set options");
				setMessage("Configure the exporter.");
				
				return c;
			}
		};
	}
}
