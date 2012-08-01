package it.uniud.bigredit.model.load_save.loaders;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIG_RED;
import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import it.uniud.bigredit.Activator;
import it.uniud.bigredit.model.Reaction;

import org.bigraph.model.Bigraph;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IFileWrapper;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.load_save.loaders.BigraphXMLLoader;

public class ReactionXMLLoader extends XMLLoader{

	
	public static final String BRS =
			"http://www.itu.dk/research/pls/xmlns/2012/brs";
	
	public static final String REACTION =
			"http://www.itu.dk/research/pls/xmlns/2012/reaction";
	
	@Override
	public Reaction makeObject(Element e) throws LoadFailedException {
		Reaction ra= new Reaction();
		
		ChangeGroup cg = new ChangeGroup();
		
		Element redex = getNamedChildElement(e, REACTION, "redex");
		if (redex != null){
			Bigraph created=makeBigraph(redex);
			Element eA = getNamedChildElement(redex, BIG_RED, "appearance");
			String width=eA.getAttribute("width");
			String height=eA.getAttribute("height");
			String x=eA.getAttribute("x");
			String y=eA.getAttribute("y");
			Rectangle rect=new Rectangle(Integer.parseInt(x),Integer.parseInt(y), Integer.parseInt(width),Integer.parseInt(height));
		
			cg.add(ra.changeAddRedex(created));
			cg.add(ra.changeLayoutChild(created, rect));
			
		}else{
			System.out.println("redex=null");
		}
		
		Element reactum = getNamedChildElement(e, REACTION, "reactum");
		
		if (reactum != null){
			Bigraph createdM=makeBigraph(reactum);
			Element eB = getNamedChildElement(reactum, BIG_RED, "appearance");
			String widthb=eB.getAttribute("width");
			String heightb=eB.getAttribute("height");
			String xb=eB.getAttribute("x");
			String yb=eB.getAttribute("y");
			Rectangle rectt=new Rectangle(Integer.parseInt(xb),Integer.parseInt(yb), Integer.parseInt(widthb),Integer.parseInt(heightb));
			cg.add(ra.changeAddReactum(createdM));
			cg.add(ra.changeLayoutChild(createdM, rectt));
		}
		
		try {
			if (cg.size() != 0)
				ra.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
		
		return ra;
	}

	@Override
	public Reaction importObject() throws LoadFailedException {
		try {
			Document d =
					validate(parse(getInputStream()), "resources/schema/reaction.xsd");
			Reaction b = makeObject(d.getDocumentElement());
			FileData.setFile(b, getFile());
			return b;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Bigraph makeBigraph(Element e) throws LoadFailedException {
		return loadEmbedded(e, SPEC, "src", Bigraph.class,
				new BigraphXMLLoader().addNewUndecorators(getUndecorators()));
	}
	
	@Override
	public ReactionXMLLoader setFile(IFileWrapper f) {
		return (ReactionXMLLoader)super.setFile(f);
	}
	
	
	private static SchemaFactory sf = null;
	
	
	public static Document validate(Document d, String schema)
			throws LoadFailedException {
		try {
			if (sf == null)
				sf =
				SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			sf.newSchema(
					new StreamSource(getResource(schema))).
				newValidator().validate(new DOMSource(d));
			return d;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	public static InputStream getResource(String path) {
		try {
			URL u = FileLocator.find(
					Activator.getDefault().getBundle(), new Path(path), null);
			if (u != null)
				return u.openStream();
			else return null;
		} catch (IOException e) {
			return null;
		}
	}
	

}
