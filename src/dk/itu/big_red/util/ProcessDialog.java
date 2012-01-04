package dk.itu.big_red.util;

import java.io.IOException;
import java.nio.charset.Charset;
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

import dk.itu.big_red.util.io.AsynchronousInputThread;
import dk.itu.big_red.util.io.AsynchronousOutputThread;
import dk.itu.big_red.util.io.IAsynchronousInputRecipient;
import dk.itu.big_red.util.io.IAsynchronousOutputRecipient;

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
	
	byte[] output = null;
	
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
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
		text.setText(new String(output, UTF8));
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
	
	@Override
	public int open() {
		try {
			Process process = pb.start();
			
			AsynchronousOutputThread ot =
				new AsynchronousOutputThread(this).
					setOutputStream(process.getOutputStream());
			ot.start();
			
			ot.enqueue(new byte[] { 'a', 'b', 'c', '\n' });
			ot.enqueue(new byte[] { 'a', 'b', 'c', '\n' });
			ot.enqueue(new byte[] { 'a', 'b', 'c', '\n' });
			ot.enqueue(new byte[] { 'a', 'b', 'c', '\n' });
			ot.enqueue(new byte[] { 'a', 'b', 'c', '\n' });
			ot.enqueue(null);
			
			AsynchronousInputThread it =
				new AsynchronousInputThread(this).
					setInputStream(process.getInputStream());
			it.start();
			
			int r = super.open();
			
			it.kill();
			ot.kill();
			
			process.destroy();
			return r;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
