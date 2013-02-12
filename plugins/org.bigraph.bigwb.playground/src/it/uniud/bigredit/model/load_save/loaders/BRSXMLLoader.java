package it.uniud.bigredit.model.load_save.loaders;


import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;

import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.loaders.BigraphXMLLoader;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.ReactionRuleXMLLoader;
import org.bigraph.model.loaders.SignatureXMLLoader;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IFileWrapper;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.BigRedNamespaceConstants;
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
		
		public static final
		String SIGNATURE = "http://www.itu.dk/research/pls/xmlns/2010/signature";
		
		
		@Override
		public BRS importObject() throws LoadFailedException {
			try {
				Document d =
						validate(parse(getInputStream()), "resources/schema/brs.xsd");
				System.out.println("not there");
				BRS ss = makeObject(d.getDocumentElement());
				FileData.setFile(ss, getFile());
				return ss;
			} catch (Exception e) {
				throw new LoadFailedException(e);
			}
		}
		
		private Signature makeSignature(Element e) throws LoadFailedException {
			return loadSub(e, SPEC, Signature.class, new SignatureXMLLoader(this));
		}
		
		private Bigraph makeBigraph(Element e) throws LoadFailedException {
			return loadSub(e, SPEC, Bigraph.class, new BigraphXMLLoader(this));
		}
		
		private ReactionRule makeRule(Element e) throws LoadFailedException {
			return loadSub(e, SPEC, ReactionRule.class, new ReactionRuleXMLLoader(this));
		}
		
		@Override
		public BRS makeObject(Element e) throws LoadFailedException {
			String replacement = getAttributeNS(e, BRS, "src");
			if (replacement != null)
				return loadRelative(replacement, BRS.class,
						new BRSXMLLoader());
			BRS ss = new BRS();
			
			ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
			
			SignatureXMLLoader si = new SignatureXMLLoader(this);
			Element signatureElement = getNamedChildElement(e, SIGNATURE, "signature");
			if (signatureElement != null){
				ss.setSignature(si.makeObject(signatureElement));
				
				//ss.setSignature(makeSignature(signatureElement));
			}else{
				System.out.println("Signature ==  null");
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
					Element eA = getNamedChildElement(modelElement, BigRedNamespaceConstants.BIG_RED, "appearance");
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
				Element eA = getNamedChildElement(modelElement, BigRedNamespaceConstants.BIG_RED, "appearance");
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
					DescriptorExecutorManager.getInstance().tryApplyChange(null, cg);
			} catch (ChangeCreationException cre) {
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

