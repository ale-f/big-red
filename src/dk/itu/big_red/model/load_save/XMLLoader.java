package dk.itu.big_red.model.load_save;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import dk.itu.big_red.utilities.resources.IFileBackable;

public abstract class XMLLoader extends Loader implements IFileBackable {
	private static SchemaFactory sf = null;
	
	/**
	 * Validates the given {@link Document} with the {@link Schema} constructed
	 * from the given {@link InputStream}.
	 * @param d a Document
	 * @param schema an InputStream
	 * @return <code>d</code>, for convenience
	 * @throws LoadFailedException if the validation (or the validator's
	 *         initialisation and configuration) failed
	 */
	protected static Document validate(Document d, InputStream schema)
			throws LoadFailedException {
		try {
			if (sf == null)
				sf =
				SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			sf.newSchema(new StreamSource(schema)).newValidator().
				validate(new DOMSource(d));
			return d;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private static DocumentBuilderFactory dbf = null;
	
	/**
	 * Attempts to parse the specified {@link InputStream} into a DOM {@link
	 * Document}.
	 * @param is an InputStream, which will be closed &mdash; even in the
	 * event of an exception
	 * @return a Document
	 * @throws SAXException as {@link DocumentBuilder#parse(File)}
	 * @throws CoreException as {@link IFile#getContents()}
	 * @throws IOException as {@link DocumentBuilder#parse(File)} or
	 * {@link InputStream#close}
	 * @throws ParserConfigurationException as {@link
	 * DocumentBuilderFactory#newDocumentBuilder()}
	 */
	protected static Document parse(InputStream is) throws SAXException,
	CoreException, IOException, ParserConfigurationException {
		try {
			if (dbf == null) {
				dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
			}
			return dbf.newDocumentBuilder().parse(is);
		} finally {
			is.close();
		}
	}
	
	private IFile file;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public XMLLoader setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	public abstract Object makeObject(Element e) throws LoadFailedException;
}
