package dk.itu.big_red.actions;


import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import dk.itu.big_red.util.Utility;

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
		
		setImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
		setHoverImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
		setDisabledImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED));
		setEnabled(false);
	}

	@Override
	public void run() {
		GraphicalViewer viewer;
		viewer = (GraphicalViewer)getWorkbenchPart().getAdapter(GraphicalViewer.class);
				
		PrintDialog dialog = 
			new PrintDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		PrinterData data = dialog.open();
		if (data != null) {
			Printer p = new Printer(data);
			
			/*
			 * XXX: Trim calculation?
			 */
			Rectangle trim = p.computeTrim(0, 0, 0, 0);

			ZoomManager zm =
				((ScalableRootEditPart)(viewer.getRootEditPart())).getZoomManager();
			double oldZoom = zm.getZoom();
			zm.setZoom(1.0);
			
			PrintGraphicalViewerOperation op = new PrintGraphicalViewerOperation(p, viewer);
			op.setPrintMode(PrintGraphicalViewerOperation.FIT_PAGE);
			op.setPrintMargin(new Insets(28, 28, 28, 28)); // -trim.x, -trim.y, trim.x + trim.width, trim.y + trim.height));
			op.run(getWorkbenchPart().getTitle());
			
			zm.setZoom(oldZoom);
			p.dispose();
		}
	}
}
