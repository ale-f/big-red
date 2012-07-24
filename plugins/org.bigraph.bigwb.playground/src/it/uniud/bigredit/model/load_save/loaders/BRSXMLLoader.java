package it.uniud.bigredit.model.load_save.loaders;


import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SPEC;

import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.resources.IResourceWrapper;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.load_save.loaders.BigraphXMLLoader;
import dk.itu.big_red.model.load_save.loaders.ReactionRuleXMLLoader;
import dk.itu.big_red.model.load_save.loaders.SignatureXMLLoader;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;

import it.uniud.bigredit.Activator;
import it.uniud.bigredit.model.BRS;



	/**
	 * XMLImport reads a XML document and produces a corresponding {@link BRS}.
	 * we use the actual structure for load and save bigraphs, plus we add some information.
	 * 
	 * @author carlo
	 * @see XMLSaver
	 *
	 */
	public class BRSXMLLoader  extends XMLLoader {

		
		public static final String BRS =
				"http://www.itu.dk/research/pls/xmlns/2012/brs";
		
		@Override
		public BRS importObject() throws LoadFailedException {
			try {
				Document d =
						validate(parse(getInputStream()), "resources/schema/brs.xsd");
				System.out.println("not there");
				BRS ss = makeObject(d.getDocumentElement());
				/* XXX: this is a hilariously awful hack */
				ExtendedDataUtilities.setFile(ss,
						((EclipseFileWrapper)getFile()).getResource());
				return ss;
			} catch (Exception e) {
				throw new LoadFailedException(e);
			}
		}
		
		private ModelObject tryLoad(String relPath) throws LoadFailedException {
			IResourceWrapper rw = getFile().getParent().getResource(relPath);
			if (!(rw instanceof IFileWrapper))
				throw new LoadFailedException("The path does not identify a file");
			return ((IFileWrapper)rw).load();
		}
		
		private Signature makeSignature(Element e) throws LoadFailedException {
			String signaturePath = getAttributeNS(e, SPEC, "src");
			if (signaturePath != null && getFile() != null) {
				ModelObject mo = tryLoad(signaturePath);
				if (mo instanceof Signature) {
					return (Signature)mo;
				} else throw new LoadFailedException(
						"The path does not identify a signature file");
			} else {
				return new SignatureXMLLoader().setFile(getFile()).makeObject(e);
			}
		}
		
		private Bigraph makeBigraph(Element e) throws LoadFailedException {
			String bigraphPath = getAttributeNS(e, SPEC, "src");
			if (bigraphPath != null && getFile() != null) {
				ModelObject mo = tryLoad(bigraphPath);
				if (mo instanceof Bigraph) {
					return (Bigraph)mo;
				} else throw new LoadFailedException(
						"The path does not identify a bigraph file");
			} else {
				return new BigraphXMLLoader().setFile(getFile()).makeObject(e);
			}
		}
		
		private ReactionRule makeRule(Element e) throws LoadFailedException {
			String rulePath = getAttributeNS(e, SPEC, "src");
			if (rulePath != null && getFile() != null) {
				ModelObject mo = tryLoad(rulePath);
				if (mo instanceof ReactionRule) {
					return (ReactionRule)mo;
				} else throw new LoadFailedException(
						"The path does not identify a reaction rule file");
			} else {
				return new ReactionRuleXMLLoader().setFile(getFile()).makeObject(e);
			}
		}
		
		@Override
		public BRS makeObject(Element e) throws LoadFailedException {
			BRS ss = new BRS();
			ChangeGroup cg = new ChangeGroup();
			
			Element signatureElement = getNamedChildElement(e, BRS, "signature");
			if (signatureElement != null){
				ss.setSignature(makeSignature(signatureElement));
			}
//				cg.add(ss.setSignature(makeSignature(signatureElement)));
//			
//TODO:RULE			for (Element i : getNamedChildElements(e, BRS, "rule"))
//				cg.add(ss.changeAddRule(makeRule(i)));
//			
			//Element modelElement = getNamedChildElement(e, BRS, "model");
			
			for (Element modelElement : getNamedChildElements(e, BRS, "rule")){
				if (modelElement != null){
					ModelObject created=makeRule(modelElement);
					Element eA = getNamedChildElement(modelElement, BIG_RED, "appearance");
					String width=eA.getAttribute("width");
					String height=eA.getAttribute("height");
					String x=eA.getAttribute("x");
					String y=eA.getAttribute("y");
					Rectangle rect=new Rectangle(Integer.parseInt(x),Integer.parseInt(y), Integer.parseInt(width),Integer.parseInt(height));
					cg.add(ss.changeAddChild(created,""));
					cg.add(ss.changeLayoutChild(created, rect));
					
					}
				}
			
			
			for (Element modelElement : getNamedChildElements(e, BRS, "model")){
			if (modelElement != null){
				ModelObject created=makeBigraph(modelElement);
				Element eA = getNamedChildElement(modelElement, BIG_RED, "appearance");
				String width=eA.getAttribute("width");
				String height=eA.getAttribute("height");
				String x=eA.getAttribute("x");
				String y=eA.getAttribute("y");
				Rectangle rect=new Rectangle(Integer.parseInt(x),Integer.parseInt(y), Integer.parseInt(width),Integer.parseInt(height));
				cg.add(ss.changeAddChild(created,""));
				cg.add(ss.changeLayoutChild(created, rect));
				
				}
			}
			
			try {
				if (cg.size() != 0)
					ss.tryApplyChange(cg);
			} catch (ChangeRejectedException cre) {
				throw new LoadFailedException(cre);
			}
			
			return executeUndecorators(ss, e);
		}
		
		@Override
		public BRSXMLLoader setFile(IFileWrapper f) {
			return (BRSXMLLoader)super.setFile(f);
		}
		
		
		private static SchemaFactory sf = null;
		
		
		public static Document validate(Document d, String schema)
				throws LoadFailedException {
			try {
				InputStream input=Activator.getResource(schema);
				if (sf == null){
					sf =
					SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);}
				sf.newSchema(
						new StreamSource(Activator.getResource(schema))).
					newValidator().validate(new DOMSource(d));
				return d;
			} catch (Exception e) {
				e.printStackTrace();
				throw new LoadFailedException(e);
			}
		}
		


		
		
		
	}

