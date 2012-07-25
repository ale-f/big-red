package dk.itu.big_red.editors.simulation_spec;

import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import dk.itu.big_red.interaction_managers.InteractionManager;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.io.strategies.TotalReadStrategy;
import dk.itu.big_red.utilities.ui.SaverOptionsGroup;

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
				new ExportResults(TotalReadStrategy.readString(io.getInputStream())).
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
				
				new SaverOptionsGroup(c).setSaver(exporter);
				
				setTitle("Set options");
				setMessage("Configure the exporter.");
				
				return c;
			}
		};
	}
}
