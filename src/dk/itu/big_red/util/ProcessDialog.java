package dk.itu.big_red.util;

import java.io.IOException;
import java.io.InputStream;
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

public class ProcessDialog extends Dialog {
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
	private void signalData(int length, byte[] buffer) {
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
	private void signalDataComplete() {
	}
	
	/**
	 * Called (in the main thread) when the worker thread encounters an error.
	 * @param e an {@link IOException}
	 */
	private void signalDataError(IOException e) {
	}
	
	private Process process;
	
	@Override
	public int open() {
		try {
			process = pb.start();
			
			process.getOutputStream().close();
			
			Thread runner = new Thread() {
				@Override
				public void run() {
					InputStream is = process.getInputStream();
					try {
						byte[] buffer = new byte[64];
						int length;
						while ((length = is.read(buffer)) != -1) {
							final byte[] tBuffer = buffer;
							final int tLength = length;
							UI.asyncExec(new Runnable() {
								@Override
								public void run() {
									ProcessDialog.this.signalData(tLength, tBuffer);
								}
							});
							buffer = new byte[1024];
						}
						UI.asyncExec(new Runnable() {
							@Override
							public void run() {
								ProcessDialog.this.signalDataComplete();
							}
						});
					} catch (final IOException e) {
						UI.asyncExec(new Runnable() {
							@Override
							public void run() {
								ProcessDialog.this.signalDataError(e);
							}
						});
					}
				}
			};
			runner.start();
			
			int r = super.open();
			
			process.destroy();
			return r;
		} catch (IOException e) {
			return -1;
		}
	}
}
