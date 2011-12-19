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

public class ProcessDialog extends Dialog implements IAsynchronousInputRecipient {
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
	
	/**
	 * Called (in the main thread) when the worker thread has finished a read
	 * operation.
	 * @param length the number of bytes actually present in <code>buffer</code>
	 * @param buffer a buffer containing a number of bytes
	 */
	@Override
	public void signalData(int length, byte[] buffer) {
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
	
	/**
	 * Called (in the main thread) when there's nothing left for the worker
	 * thread to read.
	 */
	@Override
	public void signalDataComplete() {
	}
	
	/**
	 * Called (in the main thread) when the worker thread encounters an error.
	 * @param e an {@link IOException}
	 */
	@Override
	public void signalError(IOException e) {
	}
	
	@Override
	public int open() {
		try {
			Process process = pb.start();
			
			process.getOutputStream().close();
			
			AsynchronousInputThread t =
				new AsynchronousInputThread(this).
					setInputStream(process.getInputStream());
			t.start();
			
			int r = super.open();
			
			t.kill();
			process.destroy();
			return r;
		} catch (IOException e) {
			return -1;
		}
	}
}
