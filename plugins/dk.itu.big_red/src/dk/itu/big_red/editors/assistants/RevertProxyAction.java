package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

public class RevertProxyAction extends Action implements UpdateAction {
	public static interface IRevertImplementor {
		public boolean canRevert();
		public void revert();
	}
	
	private IRevertImplementor revertImplementor;
	
	public RevertProxyAction(IRevertImplementor revertImplementor) {
		this.revertImplementor = revertImplementor;
		
		setText("Rever&t");
		setId(ActionFactory.REVERT.getId());
	}
	
	protected boolean calculateEnabled() {
		return revertImplementor.canRevert();
	}

	@Override
	public void run() {
		if (revertImplementor.canRevert())
			revertImplementor.revert();
	}

	@Override
	public void update() {
		setEnabled(calculateEnabled());
	}
}
