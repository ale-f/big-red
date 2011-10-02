package dk.itu.big_red.editors.bigraph.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.editors.bigraph.RuleDialog;
import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.util.UI;

public class BigraphCheckpointAction extends SelectionAction {
	public static final String ID =
			"dk.itu.big_red.editors.bigraph.actions.BigraphCheckpointAction";
	
	public BigraphCheckpointAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		setText("Chec&kpoint");
		setToolTipText("Checkpoint");
		
		setId(ID);
		
		ImageDescriptor icon =
			UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
		setEnabled(false);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected boolean calculateEnabled() {
		List l = getSelectedObjects();
		if (l.size() != 1 || !(l.get(0) instanceof BigraphPart))
			return false;
		return true;
	}

	@Override
	public void run() {
		Bigraph b = ((BigraphPart)getSelectedObjects().get(0)).getModel();
		ArrayList<Change> ch = b.checkpoint();
		IStatusLineManager sl = UI.getActiveStatusLine();
		ISharedImages si = UI.getSharedImages();
		if (ch == null) {
			sl.setMessage(si.getImage(ISharedImages.IMG_OBJS_INFO_TSK), "First checkpoint set");
		} else if (ch.size() == 0) {
			sl.setMessage(si.getImage(ISharedImages.IMG_OBJS_WARN_TSK), "No changes since last checkpoint");
		} else {
			String form = "change";
			if (ch.size() > 1)
				form += "s";
			sl.setMessage(si.getImage(ISharedImages.IMG_OBJS_INFO_TSK), ch.size() + " " + form + " since last checkpoint");
			
			CloneMap m = new CloneMap();
			RuleDialog rd = new RuleDialog(UI.getShell());
			rd.setLHS(b.clone(m));
			rd.setChanges(ch);
			rd.open();
		}
	}
}
