package dk.itu.big_red.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Utility methods for the SWT user interface.
 * @author alec
 *
 */
public class UI {
	/**
	 * Gets the active workbench.
	 * @return an {@link IWorkbench}
	 */
	public static IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();		
	}
	
	/**
	 * Gets the active workbench page.
	 * @return an {@link IWorkbenchPage}
	 */
	public static IWorkbenchPage getWorkbenchPage() {
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();		
	}
	
	/**
	 * Opens the given {@link IFile} in the appropriate editor.
	 * @param f an IFile
	 * @throws PartInitException 
	 */
	public static void openInEditor(IFile f) throws PartInitException {
		IDE.openEditor(getWorkbenchPage(), f);
	}
	
	/**
	 * Gets the active workbench window's {@link Shell}.
	 * @return a Shell
	 */
	public static Shell getShell() {
		return getWorkbench().getActiveWorkbenchWindow().getShell();		
	}
	
	/**
	 * Gets the active workbench's shared image registry.
	 * @return an {@link ISharedImages}
	 */
	public static ISharedImages getSharedImages() {
		return getWorkbench().getSharedImages();
	}
	
	/**
	 * Gets the status line manager for the currently active editor.
	 * @return an {@link IStatusLineManager}
	 */
	public static IStatusLineManager getActiveStatusLine() {
		return getWorkbenchPage().getActiveEditor().getEditorSite().
				getActionBars().getStatusLineManager();
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
	
	/**
	 * Enables, or disables, a group of {@link Control}s.
	 * @param enabled the new enabled state
	 * @param controls the controls to enable or disable
	 */
	public static void setEnabled(boolean enabled, Control... controls) {
		for (Control c : controls)
			c.setEnabled(enabled);
	}
	
	private static Clipboard cb = null;
	
	/**
	 * Returns the shared {@link Clipboard} for this application.
	 * @return a Clipboard
	 */
	public static Clipboard getClipboard() {
		if (cb == null)
			cb = new Clipboard(null);
		return cb;
	}
	
	/**
	 * Sets the clipboard's contents to the given string.
	 * @param s a string
	 */
	public static void setClipboardText(String s) {
		TextTransfer tt = TextTransfer.getInstance();
		getClipboard().setContents(new Object[]{ s }, new Transfer[]{ tt });
	}
	
	/**
	 * Returns the clipboard's contents as a string.
	 * @return the clipboard's contents, or <code>null</code> if the clipboard
	 *         didn't contain a string
	 */
	public static String getClipboardText() {
		TextTransfer tt = TextTransfer.getInstance();
		return (String)getClipboard().getContents(tt);
	}

	/**
	 * Returns a copy of the given Font with the formatting properties changed.
	 * @param original the font to adjust
	 * @param pt the size of the new font, in points
	 * @param properties a combination of {@link SWT#BOLD},
	 * {@link SWT#ITALIC}, and {@link SWT#NORMAL}
	 * @return a new Font (make sure to {@link Font#dispose() dispose} it!)
	 */
	public static Font tweakFont(Font original, int pt, int properties) {
		FontData[] orig = original.getFontData();
		if (orig.length < 1)
			return null;
		if (pt < 1)
			pt = orig[0].getHeight();
		FontData f = new FontData(orig[0].getName(), pt, properties);
		return new Font(null, f);
	}

	/**
	 * Returns a copy of the given Font with the formatting properties changed.
	 * @param original the font to adjust
	 * @param properties a combination of {@link SWT#BOLD},
	 * {@link SWT#ITALIC}, and {@link SWT#NORMAL}
	 * @return a new Font (make sure to {@link Font#dispose() dispose} it!)
	 */
	public static Font tweakFont(Font original, int properties) {
		return tweakFont(original, 0, properties);
	}

	public static String promptFor(String title, String caption, String initialValue, IInputValidator validator) {
		InputDialog id =
			new InputDialog(getShell(), title,
				caption, initialValue, validator);
		id.setBlockOnOpen(true);
		if (id.open() == InputDialog.OK) {
			return id.getValue();
		} else {
			return null;
		}
	}
	
	public static Image getImage(String symbolicName) {
		return getWorkbench().getSharedImages().getImage(symbolicName);
	}

	public static ImageDescriptor getImageDescriptor(String symbolicName) {
		return getWorkbench().getSharedImages().getImageDescriptor(symbolicName);
	}
}
