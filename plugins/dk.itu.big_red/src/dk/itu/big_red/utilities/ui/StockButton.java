package dk.itu.big_red.utilities.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;

import static dk.itu.big_red.utilities.ui.UI.getImage;

public final class StockButton {
	private Image image;
	private String caption;
	
	private StockButton(Image image, String caption) {
		this.image = image;
		this.caption = caption;
	}
	
	public Button create(Composite parent) {
		return create(parent, SWT.NONE);
	}
	
	public Button create(Composite parent, int flags) {
		return create(parent, flags, false);
	}
	
	public Button create(Composite parent, int flags, boolean modal) {
		Button b = new Button(parent, flags);
		b.setText(caption + (modal ? "..." : ""));
		b.setImage(image);
		return b;
	}
	
	public static final StockButton
		ADD = new StockButton(
				getImage(ISharedImages.IMG_OBJ_ADD), "&Add"),
		REMOVE = new StockButton(
				getImage(ISharedImages.IMG_ELCL_REMOVE), "&Remove"),
		OPEN = new StockButton(
				getImage(ISharedImages.IMG_OBJ_FOLDER), "&Open");
}
