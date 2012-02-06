package dk.itu.big_red.utilities.ui;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChainHelper<T extends Control> {
	private T object;
	
	public ChainHelper(T object) {
		this.object = object;
	}
	
	public T done() {
		return object;
	}
	
	public ChainHelper<T> enabled(boolean enabled) {
		done().setEnabled(enabled);
		return this;
	}
	
	public ChainHelper<T> visible(boolean visible) {
		done().setVisible(visible);
		return this;
	}
	
	public ChainHelper<T> layout(Layout layout) {
		T object = done();
		if (object instanceof Composite)
			((Composite)object).setLayout(layout);
		return this;
	}
	
	public ChainHelper<T> size(int width, int height) {
		done().setSize(width, height);
		return this;
	}
	
	public ChainHelper<T> layoutData(Object data) {
		done().setLayoutData(data);
		return this;
	}
	
	public ChainHelper<T> text(String text) {
		T object = done();
		if (object instanceof Button) {
			((Button)object).setText(text);
		} else if (object instanceof Text) {
			((Text)object).setText(text);
		} else if (object instanceof Label) {
			((Label)object).setText(text);
		} else if (object instanceof Shell) {
			((Shell)object).setText(text);
		}
		return this;
	}
}