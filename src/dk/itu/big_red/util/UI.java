package dk.itu.big_red.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility methods for the SWT user interface.
 * @author alec
 *
 */
public class UI {
	/**
	 * Gets the active workbench window's {@link Shell}.
	 * @return a Shell
	 */
	public static Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();		
	}
	
	/**
	 * Gets a {@link FileDialog} for the current workbench shell.
	 * @param style the style of dialog to construct
	 * @return a new file dialog whose parent is the current workbench shell
	 */
	public static FileDialog getFileDialog(int style) {
		return UI.getFileDialog(getShell(), style);
	}

	/**
	 * Gets a {@link FileDialog} for the specified window.
	 * @param parent a window
	 * @param style the style of dialog to construct
	 * @return a new file dialog whose parent is the specified window
	 */
	public static FileDialog getFileDialog(Shell parent, int style) {
		return new FileDialog(parent, style | SWT.DIALOG_TRIM);
	}

	/**
	 * Displays a {@link MessageBox} for the current workbench shell.
	 * @param style the style of dialog to construct
	 * @param caption the title to give the message box window
	 * @param message the message to display in the message box
	 * @return the ID of the button used to dismiss the message box
	 */
	public static int showMessageBox(int style, String caption, String message) {
		return UI.showMessageBox(getShell(), style, caption, message);
	}
	
	/**
	 * Displays a {@link MessageBox} for the specified window.
	 * @param parent a window
	 * @param style the style of dialog to construct
	 * @param caption the title to give the message box window
	 * @param message the message to display in the message box
	 * @return the ID of the button used to dismiss the message box
	 */
	public static int showMessageBox(Shell parent, int style, String caption, String message) {
		MessageBox mb = new MessageBox(parent, style);
		mb.setMessage(message);
		mb.setText(caption);
		return mb.open();
	}
	
	/**
	 * Creates a new {@link MenuItem} with the given properties.
	 * @param parent the parent {@link Menu}
	 * @param style the style of MenuItem to construct
	 * @param text the text of the MenuItem
	 * @param listener the {@link SelectionListener} to be notified when the
	 *        MenuItem is selected
	 * @return the new MenuItem
	 */
	public static MenuItem createMenuItem(Menu parent, int style, String text, SelectionListener listener) {
		MenuItem i = new MenuItem(parent, style);
		i.setText(text);
		if (listener != null)
			i.addSelectionListener(listener);
		return i;
	}
}
