package dk.itu.big_red.editors.signature;

import java.util.ArrayList;
import java.util.List;
import org.bigraph.model.Control;
import org.bigraph.model.Signature;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.resources.IFileWrapper;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import dk.itu.big_red.editors.assistants.ControlImageDescriptor;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ControlUtilities;
import org.bigraph.extensions.param.ParameterUtilities;

class SignatureControlsLabelProvider
		extends BaseLabelProvider implements ILabelProvider, IColorProvider {
	private List<Image> images = new ArrayList<Image>();
	
	@Override
	public void dispose() {
		for (Image i : images)
			i.dispose();
		super.dispose();
	}
	
	private Image getControlImage(Control e) {
		Image i = new ControlImageDescriptor(e, 16, 16).createImage();
		images.add(i);
		return i;
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Control) {
			return getControlImage((Control)element);
		} else return null;
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		if (element instanceof Control) {
			return (Control.PROPERTY_NAME.equals(property) ||
					ControlUtilities.SHAPE.equals(property) ||
					ColourUtilities.FILL.equals(property) ||
					ColourUtilities.OUTLINE.equals(property) ||
					ParameterUtilities.PARAMETER_POLICY.equals(property));
		} else return false;
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof Signature) {
			Signature s = (Signature)element;
			IFileWrapper f = FileData.getFile(s);
			return (f != null ? f.getPath() : "(embedded)");
		} else if (element instanceof Control) {
			Control c = (Control)element;
			String name = c.getName();
			INamePolicy n =
					ParameterUtilities.getParameterPolicy(c);
			if (n != null)
				name += " (" + n.getClass().getSimpleName() + ")";
			return name;
		} else return null;
	}

	private static boolean isIncluded(Object element) {
		return (element instanceof Signature ||
				(element instanceof Control &&
					((Control)element).getSignature().getParent() != null));
	}
	
	@Override
	public Color getForeground(Object element) {
		return (isIncluded(element) ? ColorConstants.gray : null);
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}
}
