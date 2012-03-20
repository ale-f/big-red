package dk.itu.big_red.application.plugin;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.20f,
				editorArea);
		
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
		topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout topRight = layout.createFolder(
			"topRight", IPageLayout.RIGHT, 0.75f, editorArea);
		topRight.addPlaceholder(IPageLayout.ID_OUTLINE);
		topRight.addPlaceholder(IPageLayout.ID_PROP_SHEET);
		
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}
}
