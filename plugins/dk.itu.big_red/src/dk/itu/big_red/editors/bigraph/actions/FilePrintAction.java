package dk.itu.big_red.editors.bigraph.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.utilities.ui.UI;

public class FilePrintAction extends org.eclipse.gef.ui.actions.PrintAction {
	/*
	 * If you think this looks remarkably similar to
	 * org.eclipse.gef.ui.actions.PrintAction, then you're right - the
	 * only real difference is that this one also imposes margins and
	 * makes sure the whole thing fits onto a page.
	 */
	
	public FilePrintAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
		setHoverImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
		setDisabledImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED));
		setEnabled(false);
	}

	@Override
	public void run() {
		GraphicalViewer viewer;
		viewer = (GraphicalViewer)getWorkbenchPart().getAdapter(GraphicalViewer.class);
				
		PrintDialog dialog =
				new PrintDialog(getWorkbenchPart().getSite().getShell());
		PrinterData data = dialog.open();
		if (data != null) {
			Printer p = new Printer(data);

			PrintGraphicalViewerOperation op =
					new PrintGraphicalViewerOperation(p, viewer);
			op.setPrintMode(PrintGraphicalViewerOperation.FIT_PAGE);
			op.run(getWorkbenchPart().getTitle());
			
			p.dispose();
		}
	}
}
