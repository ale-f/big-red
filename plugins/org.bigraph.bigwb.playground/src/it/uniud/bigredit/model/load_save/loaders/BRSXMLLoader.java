package it.uniud.bigredit.model.load_save.loaders;


import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.loaders.BigraphXMLLoader;
import dk.itu.big_red.model.load_save.loaders.ReactionRuleXMLLoader;
import dk.itu.big_red.model.load_save.loaders.SignatureXMLLoader;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;

import it.uniud.bigredit.Activator;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;



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
						validate(parse(source), "resources/schema/brs.xsd");
				System.out.println("not there");
				BRS ss = makeObject(d.getDocumentElement());
				ExtendedDataUtilities.setFile(ss, getFile());
				return ss;
			} catch (Exception e) {
				throw new LoadFailedException(e);
			}
		}
		
		private Signature makeSignature(Element e) throws LoadFailedException {
			String signaturePath = getAttributeNS(e, BRS, "src");
			SignatureXMLLoader l = newLoader(SignatureXMLLoader.class);
			if (signaturePath != null && getFile() != null) {
				IFile f = Project.findFileByPath(
						getFile().getParent(), new Path(signaturePath));
				try {
					l.setFile(f).setInputStream(f.getContents());
				} catch (CoreException ex) {
					throw new LoadFailedException(ex);
				}
				return l.importObject();
			} else {
				return l.setFile(getFile()).makeObject(e);
			}
		}
		
		private Bigraph makeBigraph(Element e) throws LoadFailedException {
			String bigraphPath = getAttributeNS(e, BRS, "src");
			BigraphXMLLoader l = newLoader(BigraphXMLLoader.class);
			if (bigraphPath != null && getFile() != null) {
				
				
				IFile f =  Project.findFileByPath(getFile().getParent(), new Path(bigraphPath));    
				try {
					l.setFile(f).setInputStream(f.getContents());
				} catch (CoreException ex) {
					throw new LoadFailedException(ex);
				}
				return l.importObject();
			} else {
				return l.setFile(getFile()).makeObject(e);
			}
		}
		
		private Reaction makeRule(Element e) throws LoadFailedException {
			String rulePath = getAttributeNS(e, BRS, "src");
			ReactionXMLLoader l = newLoader(ReactionXMLLoader.class);
			if (rulePath != null && getFile() != null) {
				IFile f = Project.findFileByPath(
						getFile().getParent(), new Path(rulePath));
				try {
					l.setFile(f).setInputStream(f.getContents());
				} catch (CoreException ex) {
					throw new LoadFailedException(ex);
				}
				return l.importObject();
			} else {
				return l.setFile(getFile()).makeObject(e);
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
					ModelObject created=(ModelObject)makeRule(modelElement);
					Element eA = getNamedChildElement(modelElement, BIG_RED, "appearance");
					String width=eA.getAttribute("width");
					String height=eA.getAttribute("height");
					String x=eA.getAttribute("x");
					String y=eA.getAttribute("y");
					Rectangle rect=new Rectangle(Integer.parseInt(x),Integer.parseInt(y), Integer.parseInt(width),Integer.parseInt(height));
					cg.add(ss.changeAddChild(created,""), ss.changeLayoutChild(created, rect));
					
					}
				}
			
			
			for (Element modelElement : getNamedChildElements(e, BRS, "model")){
			if (modelElement != null){
				ModelObject created=(ModelObject)makeBigraph(modelElement);
				Element eA = getNamedChildElement(modelElement, BIG_RED, "appearance");
				String width=eA.getAttribute("width");
				String height=eA.getAttribute("height");
				String x=eA.getAttribute("x");
				String y=eA.getAttribute("y");
				Rectangle rect=new Rectangle(Integer.parseInt(x),Integer.parseInt(y), Integer.parseInt(width),Integer.parseInt(height));
				cg.add(ss.changeAddChild(created,""), ss.changeLayoutChild(created, rect));
				
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
		public BRSXMLLoader setFile(IFile f) {
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

