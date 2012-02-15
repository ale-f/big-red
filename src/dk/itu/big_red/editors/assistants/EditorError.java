package dk.itu.big_red.editors.assistants;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.ui.UI;

public class EditorError {
	private static Font font;
	private static Image error;
	
	public EditorError(final Composite parent, IStatus reason) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginBottom = 10;
		gl.horizontalSpacing = 10;
		c.setLayout(gl);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setBackground(ColorConstants.white);
		c.setBackgroundMode(SWT.INHERIT_FORCE);
		
		if (error == null)
			error = new Image(null, RedPlugin.getResource("content/error.png"));
		Label l = new Label(c, SWT.NONE);
		l.setImage(error);
		l.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		
		Composite rhs = new Composite(c, SWT.NONE);
		GridLayout rhsl = new GridLayout(1, false);
		rhsl.verticalSpacing = 10;
		rhs.setLayout(rhsl);
		rhs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label header = new Label(rhs, SWT.WRAP);
		if (font == null)
			font = UI.tweakFont(header.getFont(), 20, SWT.BOLD);
		header.setFont(font);
		header.setText("Oh no!");
		
		Text t = new Text(rhs, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
		t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		String errorText = reason.toString();
		
		Throwable ex;
		if ((ex = reason.getException()) != null) {
			IOAdapter io = new IOAdapter();
			ex.printStackTrace(new PrintStream(io.getOutputStream()));
			try {
				io.getOutputStream().close();
				errorText += "\n\n" + IOAdapter.readString(io.getInputStream());
			} catch (IOException e) {
			}
		}
		
		t.setText(errorText);
	}
}
