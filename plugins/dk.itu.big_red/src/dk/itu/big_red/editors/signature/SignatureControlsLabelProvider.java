package dk.itu.big_red.editors.signature;

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

import dk.itu.big_red.model.ParameterUtilities;

class SignatureControlsLabelProvider
		extends BaseLabelProvider implements ILabelProvider, IColorProvider {
	@Override
	public Image getImage(Object element) {
		return null;
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

	private boolean isIncluded(Object element) {
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
