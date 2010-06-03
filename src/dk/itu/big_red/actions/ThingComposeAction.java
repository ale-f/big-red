package dk.itu.big_red.actions;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;


import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.w3c.dom.Document;

import dk.itu.big_red.GraphicalEditor;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.util.Utility;

public class ThingComposeAction extends Action {
	private IWorkbenchWindow window;
	
	public ThingComposeAction(IWorkbenchWindow window) {
		this.window = window;
		
		setId("net.ybother.big_red.compose");
		setText("&Compose...");
		setAccelerator(SWT.CTRL | 'I');
		
		setImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
		setHoverImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
		setDisabledImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
	}

	@Override
	public void run() {
		if (window != null) {
			Bigraph model =
				((GraphicalEditor)window.getActivePage().getActiveEditor()).getModel();
			
			ArrayList<Thing> sites = model.findAllChildren(Site.class);
			
			if (sites.size() != 0) {
				FileDialog f = Utility.getFileDialog(window.getShell(), SWT.OPEN);
				f.setText("Compose with...");
				
				String filename = f.open();
				
				if (filename != null) {
					File file = new File(filename);
					Document doc;
					try {
						doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					Bigraph b = Bigraph.fromXML(doc);
					
					ArrayList<Thing> roots = b.findAllChildren(Root.class);
					
					if (roots.size() == sites.size()) {
						model.getPortAuthority().registerPortsFrom(b.getPortAuthority());
						model.getControlAuthority().registerControlsFrom(b.getControlAuthority());
						
						for (int i = 0; i < sites.size(); i++) {
							Root root = (Root)roots.get(i);
							Site site = (Site)sites.get(i);
							Thing siteParent = site.getParent();
							Rectangle r = site.getLayout();
							siteParent.removeChild(site);
							siteParent.growUpRecursively(r, root.getLayout());
							for (Thing n : roots.get(i).getChildrenArray()) {
								Rectangle newLayout = new Rectangle(n.getLayout());
								newLayout.x += r.x;
								newLayout.y += r.y;
								n.setLayout(newLayout);
								
								siteParent.addChild(n);
							}
						}
					} else {
						ErrorDialog.openError(
								window.getShell(),
								"Cannot compose",
								"This bigraph cannot be composed.",
								new Status(Status.ERROR, dk.itu.big_red.Activator.PLUGIN_ID, "The two bigraphs have differing numbers of roots and sites (expected " + sites.size() + ", got " + roots.size() + ")."));
					}
				}
			} else {
				ErrorDialog.openError(
						window.getShell(),
						"Cannot compose",
						"This bigraph cannot be composed.",
						new Status(Status.ERROR, dk.itu.big_red.Activator.PLUGIN_ID, "There are no sites on this bigraph."));
			}
		}
	}

}
