package dk.itu.big_red.editors.simulation_spec;

import java.io.IOException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import dk.itu.big_red.utilities.io.AsynchronousAdapter;
import dk.itu.big_red.utilities.io.AsynchronousInputThread;
import dk.itu.big_red.utilities.io.AsynchronousOutputThread;
import dk.itu.big_red.utilities.io.strategies.LineReadStrategy;

class ProcessDialog extends Dialog {
	private ProcessBuilder pb;
	
	public ProcessDialog(Shell parentShell, ProcessBuilder pb) {
		super(parentShell);
		this.pb = pb;
	}

	protected ProcessDialog(IShellProvider parentShell, ProcessBuilder pb) {
		super(parentShell);
		this.pb = pb;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent,
			IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	private StyledText text;
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		text = new StyledText(c, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY |
				SWT.V_SCROLL | SWT.WRAP);
		text.setFont(JFaceResources.getTextFont());
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return c;
	}
	
	private String result = "";
	
	private String input;
	
	public String getInput() {
		return input;
	}
	
	public ProcessDialog setInput(String input) {
		this.input = input;
		return this;
	}
	
	@Override
	public int open() {
		boolean oldRedirect = pb.redirectErrorStream();
		pb.redirectErrorStream(true);
		try {
			Process process = pb.start();
			
			AsynchronousOutputThread ot =
				new AsynchronousOutputThread(new AsynchronousAdapter()).
					setOutputStream(process.getOutputStream());
			ot.start();
			
			ot.add(getInput());
			ot.done();
			
			AsynchronousInputThread it =
				new AsynchronousInputThread(new AsynchronousAdapter() {
					@Override
					public void signalInput(int length, byte[] buffer) {
						if (text != null && text.isDisposed())
							return;
						result += new String(buffer);
						if (text != null) {
							text.setText(result);
							text.setTopIndex(Integer.MAX_VALUE);
						}
					}
				}).setInputStream(process.getInputStream()).
					setReadStrategy(new LineReadStrategy());
			it.start();
			
			int r = super.open();
			
			it.kill();
			
			process.destroy();
			return r;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			pb.redirectErrorStream(oldRedirect);
		}
	}
}
