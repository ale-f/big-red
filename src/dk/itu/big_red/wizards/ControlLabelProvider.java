package dk.itu.big_red.wizards;

import dk.itu.big_red.util.Utility;
import dk.itu.big_red.model.Control;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

/**
 * A ControlLabelProvider is used by {@link TreeViewer}s to display an
 * appropriate icon and caption for {@link Control}s listed in the tree view.
 * @author alec
 *
 */
public class ControlLabelProvider extends LabelProvider {
	@Override
	public Image getImage(Object element) {
		Image i = Utility.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		Control m = (Control)element;
		switch (m.getShape()) {
		case SHAPE_OVAL:
			i = Utility.getBigRedImage("/resource/icons/bigraph/circle.png");
			break;
/*		case SHAPE_RECTANGLE:
			i = Utility.getBigRedImage("/resource/icons/bigraph/square.png");
			break;
		case SHAPE_TRIANGLE:
			i = Utility.getBigRedImage("/resource/icons/bigraph/triangle.png");
			break;*/
		default:
			break;
		}
		return i;
	}
	
	@Override
	public String getText(Object element) {
		return ((Control)element).getLongName();
	}
}
