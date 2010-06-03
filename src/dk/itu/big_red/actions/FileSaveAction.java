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

import dk.itu.big_red.GraphicalEditor;
import dk.itu.big_red.model.Bigraph;

public class FileSaveAction extends org.eclipse.gef.ui.actions.SaveAction {

	public FileSaveAction(IEditorPart editor) {
		super(editor);
		setLazyEnablementCalculation(true);
	}
	
	protected GraphicalEditor getEditor() {
		return (GraphicalEditor)getEditorPart();
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
		GraphicalEditor e = getEditor();
		if (e.getAssociatedFile() != null) {
			/*
			 * Saving goes here.
			 */
			try {
				Node d = ((Bigraph)e.getModel()).toXML();
				TransformerFactory f =
					TransformerFactory.newInstance();
				
				Source source = new DOMSource(d);
				File output = new File(e.getAssociatedFile());
				Result result = new StreamResult(output);
			
				Transformer t =
					f.newTransformer();
				t.transform(source, result);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			new FileSaveAsAction(getEditor()).run();
		}
	}
}
