package dk.itu.big_red.editors.assistants;

import java.io.IOException;
import java.io.PrintStream;

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
import dk.itu.big_red.utilities.io.strategies.TotalReadStrategy;
import dk.itu.big_red.utilities.ui.UI;

public class EditorError {
	private static Font font;
	private static Image error;
	
	private Text text;
	private Composite control;
	
	public EditorError(final Composite parent, Throwable throwable) {
		control = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginBottom = 10;
		gl.horizontalSpacing = 10;
		control.setLayout(gl);
		control.setBackground(ColorConstants.white);
		control.setBackgroundMode(SWT.INHERIT_FORCE);
		
		if (error == null)
			error = new Image(null,
					RedPlugin.getResource("resources/error.png"));
		Label l = new Label(control, SWT.NONE);
		l.setImage(error);
		l.setLayoutData(
				new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		
		Composite rhs = new Composite(control, SWT.NONE);
		GridLayout rhsl = new GridLayout(1, false);
		rhsl.verticalSpacing = 10;
		rhs.setLayout(rhsl);
		rhs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label header = new Label(rhs, SWT.WRAP);
		if (font == null)
			font = UI.tweakFont(header.getFont(), 20, SWT.BOLD);
		header.setFont(font);
		header.setText("Oh no!");
		
		text = new Text(rhs,
				SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI |
				SWT.V_SCROLL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		setThrowable(throwable);
	}
	
	public Composite getControl() {
		return control;
	}
	
	public void dispose() {
		control.dispose();
	}
	
	public void setThrowable(Throwable t) {
		String desc = "(no error)";
		
		if (t != null) {
			desc = t.getLocalizedMessage();
			
			IOAdapter io = new IOAdapter();
			t.printStackTrace(new PrintStream(io.getOutputStream()));
			try {
				io.getOutputStream().close();
				desc += "\n\n" +
						TotalReadStrategy.readString(io.getInputStream());
			} catch (IOException e) {
			}
		}
		
		text.setText(desc);
	}
}
