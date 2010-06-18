package dk.itu.big_red.actions;

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.eclipse.ui.IEditorPart;
import org.w3c.dom.Node;

import dk.itu.big_red.editors.BigraphEditor;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.util.DOM;

public class FileSaveAction extends org.eclipse.gef.ui.actions.SaveAction {

	public FileSaveAction(IEditorPart editor) {
		super(editor);
		setLazyEnablementCalculation(true);
	}
	
	protected BigraphEditor getEditor() {
		return (BigraphEditor)getEditorPart();
	}
	
	@Override
	public boolean calculateEnabled() {
		/*
		 * FIXME: this should work. Why won't it work? I hate you, isEnabled,
		 * in all your various guises
		 */
		return true;
	}
	
	public void run() {
		BigraphEditor e = getEditor();
		if (e.getAssociatedFile() != null) {
			/*
			 * Saving goes here.
			 */
			try {
				DOM.write(e.getAssociatedFile(), e.getModel().toXML());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			new FileSaveAsAction(getEditor()).run();
		}
	}
}
