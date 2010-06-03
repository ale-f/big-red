package dk.itu.big_red.intro;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	private static String ID_TABS_FOLDER = "big_red.intro.tabs";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		IFolderLayout tabs = layout.createFolder(
			ID_TABS_FOLDER, IPageLayout.LEFT, 0.3f, editorArea);
		tabs.addView(IPageLayout.ID_OUTLINE);
		tabs.addPlaceholder(IPageLayout.ID_PROP_SHEET);
		
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}
}
