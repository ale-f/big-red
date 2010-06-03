package dk.itu.big_red.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * SubWizards are second-level wizards: a {@link Wizard} presents a list of
 * SubWizards to the user, and then the relevant one is called.
 * 
 * <p>To support this list model, SubWizards need to have icons, titles, and
 * summaries, so that the user can decide which one to select.
 * @author alec
 *
 */
public abstract class SubWizard extends Wizard implements IWizardNode {
	public abstract void init();
	
	public Image getIcon() {
		return null;
	}
	
	public abstract String getSummary();
	
	public String getTitle() {
		return null;
	}
	
	@Override
	public IWizard getWizard() {
		return this;
	}
	
	@Override
	public boolean isContentCreated() {
		return true;
	}
	
	@Override
	public Point getExtent() {
		return new Point(-1, -1);
	}
}
