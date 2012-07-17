package dk.itu.big_red.editors.actions;

import org.eclipse.ui.actions.ActionFactory;

public class RevertProxyAction extends ProxyAction {
	public RevertProxyAction(IActionImplementor revertImplementor) {
		super(ActionFactory.REVERT.getId());
		setImplementor(revertImplementor);
		
		setText("Rever&t");
	}
}
