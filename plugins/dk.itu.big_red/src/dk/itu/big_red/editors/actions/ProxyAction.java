package dk.itu.big_red.editors.actions;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;

public abstract class ProxyAction extends Action implements UpdateAction {
	public interface IActionImplementor {
		void performAction(String actionID);
		boolean canPerformAction(String actionID);
	}
	
	protected ProxyAction(String action) {
		setId(action);
	}
	
	private IActionImplementor implementor;
	
	protected void setImplementor(IActionImplementor implementor) {
		this.implementor = implementor;
	}
	
	protected IActionImplementor getImplementor() {
		return implementor;
	}
	
	@Override
	public final boolean isEnabled() {
		update();
		return super.isEnabled();
	}
	
	@Override
	public final void run() {
		getImplementor().performAction(getId());
	}
	
	protected final boolean calculateEnabled() {
		return getImplementor().canPerformAction(getId());
	}
	
	@Override
	public final void update() {
		setEnabled(calculateEnabled());
	}
}
