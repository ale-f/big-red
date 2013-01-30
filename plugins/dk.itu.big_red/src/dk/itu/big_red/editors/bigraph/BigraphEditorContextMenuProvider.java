package dk.itu.big_red.editors.bigraph;

import java.util.Iterator;

import org.bigraph.model.Edge;
import org.bigraph.model.Link;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.utilities.FilteringIterable;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.editors.bigraph.parts.LinkConnectionPart;
import dk.itu.big_red.editors.bigraph.parts.LinkPart;
import dk.itu.big_red.model.LayoutUtilities;
import dk.itu.big_red.model.LinkStyleUtilities;
import dk.itu.big_red.model.LinkStyleUtilities.Style;

public class BigraphEditorContextMenuProvider extends ContextMenuProvider {
	public BigraphEditorContextMenuProvider(
			EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}
	
	private ActionRegistry actionRegistry;
	
	protected ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	protected void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}
	
	protected Iterable<? extends EditPart> getSelection() {
		return new FilteringIterable<EditPart>(
				EditPart.class, getViewer().getSelectedEditParts());
	}
	
	protected EditPart getFirstSelection() {
		Iterator<? extends EditPart> it = getSelection().iterator();
		if (it.hasNext()) {
			return it.next();
		} else return null;
	}
	
	private void populateMenu(
			final PaletteViewer pv, final PaletteContainer pc,
			final IMenuManager menu) {
		for (Object j : pc.getChildren()) {
			if (j instanceof ToolEntry) {
				final ToolEntry k = (ToolEntry)j;
				Action toolAction = new Action(k.getLabel()) {
					@Override
					public void run() {
						pv.setActiveTool(k);
					}
				};
				menu.add(toolAction);
			} else if (j instanceof PaletteContainer) {
				PaletteContainer k = (PaletteContainer)j;
				MenuManager sub = new MenuManager(k.getLabel());
				populateMenu(pv, k, sub);
				menu.add(sub);
			} else if (j instanceof PaletteSeparator) {
				menu.add(new Separator());
			}
		}
	}
	
	private static final String GROUP_VARYING =
			"BigraphEditorContextMenuProvider+Varying";
	
	private final void
			addLinkOptions(final Link l, final IMenuManager menu) {
		MenuManager styleMenu = new MenuManager("Style");
		Style currentStyle = LinkStyleUtilities.getStyle(l);
		for (final Style i : Style.values()) {
			Action a = new Action(i.getDisplayName(), Action.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					if (!isChecked())
						return;
					getViewer().getEditDomain().getCommandStack().execute(
							new ChangeCommand(new BoundDescriptor(
									l.getBigraph(),
									new LinkStyleUtilities.ChangeLinkStyleDescriptor(
											null, l, i)), l.getBigraph()));
				}
			};
			a.setChecked(i.equals(currentStyle));
			styleMenu.add(a);
		}
		menu.appendToGroup(GROUP_VARYING, styleMenu);
		
		if (l instanceof Edge) {
			Action a = new Action("Autolayout", Action.AS_CHECK_BOX) {
				@Override
				public void run() {
					Rectangle r;
					if (isChecked()) {
						r = null;
					} else r = LayoutUtilities.getLayout(l);
					getViewer().getEditDomain().getCommandStack().execute(
							new ChangeCommand(new BoundDescriptor(
									l.getBigraph(),
									new LayoutUtilities.ChangeLayoutDescriptor(
											null, l, r)), l.getBigraph()));
				}
			};
			a.setChecked(LayoutUtilities.getLayoutRaw(l) == null);
			menu.appendToGroup(GROUP_VARYING, a);
		}
	}
	
	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);
		
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO,
				getActionRegistry().getAction(ActionFactory.UNDO.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO,
				getActionRegistry().getAction(ActionFactory.REDO.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_COPY,
				getActionRegistry().getAction(ActionFactory.CUT.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_COPY,
				getActionRegistry().getAction(ActionFactory.COPY.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_COPY,
				getActionRegistry().getAction(ActionFactory.PASTE.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getActionRegistry().getAction(ActionFactory.PROPERTIES.getId()));
		
		menu.add(new GroupMarker(GROUP_VARYING));
		
		EditPart selection = getFirstSelection();
		if (selection instanceof BigraphPart) {
			menu.appendToGroup(GROUP_VARYING,
					getActionRegistry().getAction(BigraphRelayoutAction.ID));
		} else if (selection instanceof LinkPart) {
			addLinkOptions(((LinkPart)selection).getModel(), menu);
		} else if (selection instanceof LinkConnectionPart) {
			addLinkOptions(
					((LinkConnectionPart)selection).getModel().getLink(),
					menu);
		}
		
		menu.appendToGroup(GEFActionConstants.GROUP_REST,
				new Separator());
		
		MenuManager palette = new MenuManager("Palette");
		menu.appendToGroup(GEFActionConstants.GROUP_REST, palette);
		PaletteViewer pv = getViewer().getEditDomain().getPaletteViewer();
		populateMenu(getViewer().getEditDomain().getPaletteViewer(),
				pv.getPaletteRoot(), palette);
	}
}
