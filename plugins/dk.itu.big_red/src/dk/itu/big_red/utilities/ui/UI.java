package dk.itu.big_red.utilities.ui;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
 */
public final class UI {
	private UI() {}
	
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
	 * Gets the active workbench's {@link Display}.
	 * @return a Display
	 */
	public static Display getDisplay() {
		return getWorkbench().getDisplay();
	}
	
	/**
	 * Performs a single iteration of the SWT event loop. This method may
	 * block.
	 * @see Display#sleep()
	 */
	public static void tick() {
		Display d = getDisplay();
		if (!d.readAndDispatch())
			d.sleep();
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
	 * @return <code>enabled</code>, for convenience
	 */
	public static boolean setEnabled(boolean enabled, Control... controls) {
		for (Control c : controls)
			if (c != null && !c.isDisposed())
				c.setEnabled(enabled);
		return enabled;
	}
	
	/**
	 * Shows, or hides, a group of {@link Control}s.
	 * @param visible the new visibility state
	 * @param controls the controls to show or hide
	 * @return <code>visible</code>, for convenience
	 */
	public static boolean setVisible(boolean visible, Control... controls) {
		for (Control c : controls)
			if (c != null && !c.isDisposed())
				c.setVisible(visible);
		return visible;
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

	/**
	 * Prompts the user for a string.
	 * @param title the title to be given to the {@link InputDialog}
	 * @param caption the <i>caption</i> to be given to the {@link InputDialog}
	 * @param initialValue the initial string value
	 * @param validator an {@link IInputValidator} to validate the string 
	 * @return a string, or <code>null</code> if the user cancelled the dialog
	 */
	public static String promptFor(String title, String caption, String initialValue, IInputValidator validator) {
		InputDialog id =
			new InputDialog(getShell(), title,
				caption, initialValue, validator);
		if (id.open() == InputDialog.OK) {
			return id.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Gets a shared image from the workbench's shared image registry.
	 * <p>The usual caveats for {@link ISharedImages#getImage(String)} apply
	 * here &mdash; in particular, don't dispose of the returned {@link Image}.
	 * @param symbolicName the symbolic name of the image to retrieve
	 * @return a shared {@link Image}
	 */
	public static Image getImage(String symbolicName) {
		return getWorkbench().getSharedImages().getImage(symbolicName);
	}

	/**
	 * Gets an image descriptor from the workbench's shared image registry.
	 * @param symbolicName the symbolic name of the image whose descriptor
	 * should be retrieved
	 * @return an {@link ImageDescriptor}
	 */
	public static ImageDescriptor getImageDescriptor(String symbolicName) {
		return getWorkbench().getSharedImages().getImageDescriptor(symbolicName);
	}
	
	/**
	 * Schedules a {@link Runnable} to be run as part of the workbench's
	 * event loop.
	 * @param r a {@link Runnable}
	 */
	public static void asyncExec(Runnable r) {
		getDisplay().asyncExec(r);
	}
}
