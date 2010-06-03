package dk.itu.big_red.wizards;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

/**
 * A SubwizardLabelProvider is used by {@link TreeViewer}s to display an
 * appropriate icon and caption for {@link SubWizard}s listed in a tree view.
 * @author alec
 *
 */

public class SubWizardLabelProvider extends LabelProvider {
	@Override
	public Image getImage(Object element) {
		return ((SubWizard)element).getIcon();
	}
	
	@Override
	public String getText(Object element) {
		return ((SubWizard)element).getTitle();
	}
}
