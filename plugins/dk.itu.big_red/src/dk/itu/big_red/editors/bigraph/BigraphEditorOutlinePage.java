package dk.itu.big_red.editors.bigraph;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.IPageSite;

import dk.itu.big_red.editors.bigraph.parts.tree.TreePartFactory;
import dk.itu.big_red.editors.bigraph.parts.tree.TreePartFactory.Mode;

public class BigraphEditorOutlinePage extends ContentOutlinePage {
	/**
	 */
	private final BigraphEditor bigraphEditor;
	/*
	 * This ContentOutlinePage has been tweaked slightly to contain a
	 * second EditPartViewer (one for the place graph, one for the link).
	 */
	private EditPartViewer viewer2;
	private Control control2;
	
	private SashForm sash;
	private TabFolder tabs;
	private ScrollableThumbnail thumbnail;
	private DisposeListener disposeListener;
	
	public BigraphEditorOutlinePage(BigraphEditor bigraphEditor) {
		super(new TreeViewer());
		this.bigraphEditor = bigraphEditor;
		setViewer2(new TreeViewer());
	}
	
	@Override
	public void createControl(Composite parent) {
		//IActionBars bars = getSite().getActionBars();
		//ActionRegistry ar = this.bigraphEditor.getActionRegistry();
		
		sash = new SashForm(parent, SWT.VERTICAL);
		tabs = new TabFolder(sash, SWT.NONE);
		
		TabItem placeGraphTab = new TabItem(tabs, SWT.NONE);
		placeGraphTab.setText("Place graph");
		placeGraphTab.setControl(getViewer().createControl(tabs));
		
		TabItem linkGraphTab = new TabItem(tabs, SWT.NONE);
		linkGraphTab.setText("Link graph");
		linkGraphTab.setControl(getViewer2().createControl(tabs));
		
		getViewer().setEditDomain(bigraphEditor.getEditDomain());
		getViewer().setEditPartFactory(new TreePartFactory(Mode.PLACE));
		getViewer().setContents(bigraphEditor.getModel());
		
		getViewer2().setEditDomain(bigraphEditor.getEditDomain());
		getViewer2().setEditPartFactory(new TreePartFactory(Mode.LINK));
		getViewer2().setContents(bigraphEditor.getModel());
		
		bigraphEditor.getSelectionSynchronizer().addViewer(getViewer());
		bigraphEditor.getSelectionSynchronizer().addViewer(getViewer2());
		
		Canvas canvas = new Canvas(sash, SWT.BORDER);
		LightweightSystem lws = new LightweightSystem(canvas);
		
		ScalableRootEditPart re = (ScalableRootEditPart)
				bigraphEditor.getGraphicalViewer().getRootEditPart();
		thumbnail = new ScrollableThumbnail((Viewport)re.getFigure());
		thumbnail.setSource(re.getLayer(LayerConstants.PRINTABLE_LAYERS));
		lws.setContents(thumbnail);
		
		disposeListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (thumbnail != null) {
					thumbnail.deactivate();
					thumbnail = null;
				}
			}
		};
		
		//bars.setGlobalActionHandler(ActionFactory.COPY.getId(), ar.getAction(ActionFactory.COPY.getId()));
		//bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), ar.getAction(ActionFactory.PASTE.getId()));
		
		bigraphEditor.getGraphicalViewer().getControl().addDisposeListener(disposeListener);
	}
	
	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		
		/*getViewer().setContextMenu(
			new BigraphEditorContextMenuProvider(getViewer(), this.bigraphEditor.getActionRegistry()));
		
		IActionBars bars = getSite().getActionBars();
		
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), this.bigraphEditor.getActionRegistry().getAction(ActionFactory.UNDO.getId()));
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), this.bigraphEditor.getActionRegistry().getAction(ActionFactory.REDO.getId()));
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), this.bigraphEditor.getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		bars.updateActionBars();*/
		
		getViewer().setKeyHandler(bigraphEditor.getGraphicalViewer().getKeyHandler());
	}
	
	@Override
	public Control getControl() {
		return sash;
	}
	
	@Override
	public void dispose() {
		bigraphEditor.getSelectionSynchronizer().removeViewer(getViewer());
		bigraphEditor.getSelectionSynchronizer().removeViewer(getViewer2());
		
		if (bigraphEditor.getGraphicalViewer().getControl() != null &&
			!bigraphEditor.getGraphicalViewer().getControl().isDisposed()) {
			bigraphEditor.getGraphicalViewer().getControl().removeDisposeListener(disposeListener);
		}
		
        Control c = getControl2();
        if (c != null && !c.isDisposed())
			c.dispose();
        
		super.dispose();
	}

	public void setViewer2(EditPartViewer viewer2) {
		this.viewer2 = viewer2;
	}

	public EditPartViewer getViewer2() {
		return viewer2;
	}

	public void setControl2(Control control2) {
		this.control2 = control2;
	}

	public Control getControl2() {
		return control2;
	}
	
	public void createControl2(Composite parent) {
		control2 = getViewer2().createControl(parent);
	}
}