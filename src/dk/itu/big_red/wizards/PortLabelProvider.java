package dk.itu.big_red.wizards;

import dk.itu.big_red.util.Utility;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class PortLabelProvider extends LabelProvider {
	@Override
	public Image getImage(Object element) {
		return Utility.getBigRedImage("/resource/icons/bigraph/port.png");
	}
	
	@Override
	public String getText(Object element) {
		return (String)element;
	}
}
