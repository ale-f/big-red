package dk.itu.big_red.editors;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.utilities.comparators.LexicographicStringComparator;
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
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.assistants.ControlImageDescriptor;
import dk.itu.big_red.editors.bigraph.ModelFactory;
import dk.itu.big_red.editors.bigraph.NodeFactory;
import dk.itu.big_red.editors.utilities.RedPropertySheetEntry;

public abstract class AbstractGEFEditor extends AbstractEditor
		implements CommandStackEventListener, ISelectionListener,
		INullSelectionListener {
	public static final String PROPERTY_DISPLAY_GUIDES =
			"dk.itu.big_red.editors.AbstractGEFEditor.propertyDisplayGuides";
	
	public static final String PROPERTY_DISPLAY_EDGES =
			"dk.itu.big_red.editors.AbstractGEFEditor.propertyDisplayEdges";
	
	private DefaultEditDomain editDomain;
	
	private List<String> selectionActions;
	
	/**
	 * Returns the list of <i>selection actions</i>, those actions which want
	 * to be updated when the editor's selection changes.
	 * @return a list of action IDs
	 * @see AbstractEditor#updateActions(List)
	 */
	protected List<String> getSelectionActions() {
		if (selectionActions == null)
			selectionActions = new ArrayList<String>();
		return selectionActions;
	}
	
	/**
	 * Returns the {@link ScalableRootEditPart} of a given {@link
	 * GraphicalViewer}, if it has one.
	 * @param v a {@link GraphicalViewer}; must not be <code>null</code>
	 * @return a {@link ScalableRootEditPart}; can be <code>null</code>
	 */
	protected static ScalableRootEditPart getScalableRoot(GraphicalViewer v) {
		if (v != null) {
			RootEditPart r = v.getRootEditPart();
			return (r instanceof ScalableRootEditPart ?
					(ScalableRootEditPart)r : null);
		} else return null;
	}
	
	/**
	 * Assigns a {@link DefaultEditDomain} to this editor, and sets the
	 * editor's palette root into it.
	 * @param editDomain a {@link DefaultEditDomain}
	 */
	protected void setEditDomain(DefaultEditDomain editDomain) {
		this.editDomain = editDomain;
		getEditDomain().setPaletteRoot(getPaletteRoot());
	}
	
	/**
	 * Returns this editor's {@link DefaultEditDomain}.
	 * @return a {@link DefaultEditDomain}
	 */
	protected DefaultEditDomain getEditDomain() {
		return editDomain;
	}
	
	public AbstractGEFEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}
	
	/**
	 * Returns the {@link CommandStack} associated with this editor's edit
	 * domain.
	 * @return a {@link CommandStack}
	 */
	public CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}
	
	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}
	
	/**
	 * Creates the {@link PaletteViewer} for this editor, creating its control
	 * and setting it into the edit domain.
	 * @param parent the {@link Composite} that should contain the {@link
	 * PaletteViewer}'s control
	 */
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
    		psp.setRootEntry(new RedPropertySheetEntry(getCommandStack()));
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
		
		creationGroup.add(new PaletteSeparator());
		
		if (nodeGroup == null)
			nodeGroup = new PaletteGroup("Node...");
		nodeGroup.setId("BigraphEditor.palette.node-creation");
		creationGroup.add(nodeGroup);
		
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
	
	private static PaletteContainer sigPop(
			List<String> names, PaletteContainer pc, Signature signature) {
		pc.setLabel(signature.toString());
		for (Control c : NamedModelObject.order(signature.getControls(),
				LexicographicStringComparator.INSTANCE)) {
			if (names.contains(c.getName()))
				continue;
			names.add(c.getName());
			pc.add(new CombinedTemplateCreationEntry(c.getName(),"Node",
					Node.class, new NodeFactory(c),
					new ControlImageDescriptor(c, 16, 16),
					new ControlImageDescriptor(c, 48, 48)));
		}
		
		for (Signature s : signature.getSignatures()) {
			PaletteContainer cpc = new PaletteGroup(null);
			sigPop(names, cpc, s);
			if (cpc.getChildren().size() != 0)
				pc.add(cpc);
		}
		return pc;
	}
	
	protected void updateNodePalette(Signature signature) {
		getNodeGroup().getChildren().clear();
		sigPop(new ArrayList<String>(), getNodeGroup(), signature);
	}
}
