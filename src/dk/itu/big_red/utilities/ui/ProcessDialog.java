package dk.itu.big_red.utilities.ui;

import java.io.IOException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import dk.itu.big_red.utilities.io.AsynchronousInputThread;
import dk.itu.big_red.utilities.io.AsynchronousOutputThread;
import dk.itu.big_red.utilities.io.BlockReadStrategy;
import dk.itu.big_red.utilities.io.IAsynchronousInputRecipient;
import dk.itu.big_red.utilities.io.IAsynchronousOutputRecipient;

public class ProcessDialog extends Dialog implements IAsynchronousInputRecipient, IAsynchronousOutputRecipient {
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
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return c;
	}
	
	private byte[] output = null;
	
	@Override
	public void signalInput(int length, byte[] buffer) {
		if (text.isDisposed())
			return;
		if (output == null) {
			output = new byte[length];
			System.arraycopy(buffer, 0, output, 0, length);
		} else if (length != 0) {
			byte[] newOutput = new byte[output.length + length];
			System.arraycopy(output, 0, newOutput, 0, output.length);
			System.arraycopy(buffer, 0, newOutput, output.length, length);
			output = newOutput;
		}
		text.setText(new String(output));
		text.setTopIndex(Integer.MAX_VALUE);
	}
	
	@Override
	public void signalInputComplete() {
	}
	
	@Override
	public void signalInputError(IOException e) {
	}
	
	@Override
	public void signalOutputError(IOException e) {
	}
	
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
		try {
			Process process = pb.start();
			
			AsynchronousOutputThread ot =
				new AsynchronousOutputThread(this).
					setOutputStream(process.getOutputStream());
			ot.start();
			
			ot.add(getInput());
			ot.done();
			
			AsynchronousInputThread it =
				new AsynchronousInputThread(this).
					setInputStream(process.getInputStream()).
						setReadStrategy(new BlockReadStrategy());
			it.start();
			
			int r = super.open();
			
			it.kill();
			
			process.destroy();
			return r;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
