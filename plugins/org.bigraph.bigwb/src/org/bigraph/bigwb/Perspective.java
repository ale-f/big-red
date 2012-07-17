package org.bigraph.bigwb;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		
		  String editorArea = layout.getEditorArea();
			
			 IFolderLayout topRight = layout.createFolder("topLeft", IPageLayout.LEFT, 0.30f, editorArea);
			 topRight.addView(IPageLayout.ID_PROJECT_EXPLORER);
			 
			 IFolderLayout bottomleft=layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.70f, "topLeft");
		     bottomleft.addView(IPageLayout.ID_OUTLINE);
		         
		
	}
}
