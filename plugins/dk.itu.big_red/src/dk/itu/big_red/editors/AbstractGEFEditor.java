package dk.itu.big_red.editors;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.assistants.Ellipse;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.ChangePropertySheetEntry;
import dk.itu.big_red.editors.bigraph.ModelFactory;
import dk.itu.big_red.editors.bigraph.NodeFactory;
import dk.itu.big_red.editors.bigraph.parts.NodePart;
import dk.itu.big_red.utilities.ui.ColorWrapper;
import dk.itu.big_red.utilities.ui.UI;

public abstract class AbstractGEFEditor extends AbstractEditor
implements CommandStackEventListener, ISelectionListener,
INullSelectionListener {
	private DefaultEditDomain editDomain;
	
	private List<String> selectionActions;
	
	protected List<String> getSelectionActions() {
		if (selectionActions == null)
			selectionActions = new ArrayList<String>();
		return selectionActions;
	}
	
	protected static ScalableRootEditPart getScalableRoot(GraphicalViewer v) {
		RootEditPart r = v.getRootEditPart();
		return (r instanceof ScalableRootEditPart ?
				(ScalableRootEditPart)r : null);
	}
	
	protected void setEditDomain(DefaultEditDomain editDomain) {
		this.editDomain = editDomain;
		getEditDomain().setPaletteRoot(getPaletteRoot());
	}
	
	protected DefaultEditDomain getEditDomain() {
		return editDomain;
	}
	
	public AbstractGEFEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}
	
	public CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}
	
	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}
	
	protected void createPaletteViewer(Composite parent) {
		PaletteViewer viewer = new PaletteViewer();
		setPaletteViewer(viewer);
		viewer.createControl(parent);
		getEditDomain().setPaletteViewer(getPaletteViewer());
	}
	
	private PaletteViewer paletteViewer;
	
	protected void setPaletteViewer(PaletteViewer paletteViewer) {
		this.paletteViewer = paletteViewer;
	}
    
	protected PaletteViewer getPaletteViewer() {
		return paletteViewer;
	}
	
	@Override
	public void stackChanged(CommandStackEvent event) {
		stateChanged();
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		getSite().getWorkbenchWindow().getSelectionService().
			addSelectionListener(this);
		getCommandStack().addCommandStackEventListener(this);
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (equals(getSite().getPage().getActiveEditor()))
			updateActions(getSelectionActions());
	}
	
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().
			removeSelectionListener(this);
		getCommandStack().removeCommandStackEventListener(this);
		super.dispose();
	}
	
	@Override
	public boolean canRedo() {
		return getCommandStack().canRedo();
	}
	
	@Override
	public void redo() {
		getCommandStack().redo();
	}
	
	@Override
	public boolean canUndo() {
		return getCommandStack().canUndo();
	}
	
	@Override
	public void undo() {
		getCommandStack().undo();
	}
	
	@Override
	public boolean canRevert() {
		return getCommandStack().isDirty();
	}
	
	@Override
	public void revert() {
		CommandStack cs = getCommandStack();
		while (cs.canUndo())
			cs.undo();
		cs.flush();
		stateChanged();
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			return getCommandStack();
		} else if (adapter == IPropertySheetPage.class) {
    		PropertySheetPage psp = new PropertySheetPage();
    		psp.setRootEntry(new ChangePropertySheetEntry(getCommandStack()));
    		return psp;
    	} else return super.getAdapter(adapter);
	}
	
	public static <T extends PaletteContainer> T populatePalette(
			T container, PaletteGroup nodeGroup,
			SelectionToolEntry defaultTool) {
    	PaletteGroup selectGroup = new PaletteGroup("Object selection");
		selectGroup.setId("BigraphEditor.palette.selection");
		container.add(selectGroup);
		
		selectGroup.add((defaultTool != null ? defaultTool : new SelectionToolEntry()));
		selectGroup.add(new MarqueeToolEntry());
		
		container.add(new PaletteSeparator());
		
		PaletteGroup creationGroup = new PaletteGroup("Object creation");
		creationGroup.setId("BigraphEditor.palette.creation");
		container.add(creationGroup);
		
		if (nodeGroup == null)
			nodeGroup = new PaletteGroup("Node...");
		nodeGroup.setId("BigraphEditor.palette.node-creation");
		creationGroup.add(nodeGroup);

		ImageDescriptor
			site = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/site.png"),
			root = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/root.png"),
			edge = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/edge.png");
		
		creationGroup.add(new CombinedTemplateCreationEntry("Site", "Add a new site to the bigraph",
				Site.class, new ModelFactory(Site.class), site, site));
		creationGroup.add(new CombinedTemplateCreationEntry("Root", "Add a new root to the bigraph",
				Root.class, new ModelFactory(Root.class), root, root));
		creationGroup.add(new ConnectionCreationToolEntry("Link", "Connect two points with a link",
				new ModelFactory(Edge.class), edge, edge));
		
		ImageDescriptor
			inner = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/inner.png"),
			outer = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/outer.png");
		
		creationGroup.add(new CombinedTemplateCreationEntry("Inner name", "Add a new inner name to the bigraph",
				InnerName.class, new ModelFactory(InnerName.class), inner, inner));
		creationGroup.add(new CombinedTemplateCreationEntry("Outer name", "Add a new outer name to the bigraph",
				OuterName.class, new ModelFactory(OuterName.class), outer, outer));
		
    	return container;
    }
	
	private PaletteGroup nodeGroup;
    
	protected PaletteGroup getNodeGroup() {
		if (nodeGroup == null)
			nodeGroup = new PaletteGroup("Node...");
		return nodeGroup;
	}
	
	protected PaletteRoot createPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		nodeGroup = new PaletteGroup("Node...");
		SelectionToolEntry ste = new SelectionToolEntry();
		
		populatePalette(root, getNodeGroup(), ste);
		
		root.setDefaultEntry(ste);
		return root;
	}
	
	private PaletteRoot paletteRoot;
	
	protected PaletteRoot getPaletteRoot() {
		if (paletteRoot == null)
			paletteRoot = createPaletteRoot();
		return paletteRoot;
	}
	
	private static final class ControlImageDescriptor extends ImageDescriptor {
		private Control c;
		private int width, height;
		
		public ControlImageDescriptor(Control c, int width, int height) {
			this.c = c;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public ImageData getImageData() {
			Display d = UI.getDisplay();
			Image i = new Image(d, width, height);
			try {
				ColorWrapper
					fill = new ColorWrapper(),
					outline = new ColorWrapper();
				
				GC gc = new GC(i);
				try {
					gc.setBackground(
							outline.update(ExtendedDataUtilities.getFill(c)));
					gc.setForeground(
							outline.update(ExtendedDataUtilities.getOutline(c)));
					Object shape = ExtendedDataUtilities.getShape(c);
					if (shape instanceof PointList) {
						PointList modified =
								NodePart.fitPolygon((PointList)shape,
										new Rectangle(0, 0, width, height));
						gc.fillPolygon(modified.toIntArray());
						gc.drawPolygon(modified.toIntArray());
					} else if (shape instanceof Ellipse) {
						gc.fillOval(0, 0, width - 1, height - 1);
						gc.drawOval(0, 0, width - 1, height - 1);
					} else {
						gc.setBackground(ColorConstants.red);
						gc.fillRectangle(0, 0, width, height);
					}
				} finally {
					gc.dispose();
				}
				
				fill.update(null);
				outline.update(null);
				
				return i.getImageData();
			} finally {
				i.dispose();
			}
		}
	}
	
	protected void updateNodePalette(Signature signature) {
    	ArrayList<PaletteEntry> palette = new ArrayList<PaletteEntry>();
    	
		for (Control c : signature.getControls()) {
			palette.add(new CombinedTemplateCreationEntry(c.getName(), "Node",
					Node.class, new NodeFactory(c),
					new ControlImageDescriptor(c, 16, 16),
					new ControlImageDescriptor(c, 48, 48)));
		}
		
		getNodeGroup().setChildren(palette);
	}
}
