package org.bigraph.model.loaders.internal;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

class Input implements LSInput {
	Input(InputStream byteStream,
			String publicId, String systemId, String baseURI) {
		setByteStream(byteStream);
		setPublicId(publicId);
		setSystemId(systemId);
		setBaseURI(baseURI);
	}
	
	@Override
	public Reader getCharacterStream() {
		return null;
	}

	@Override
	public void setCharacterStream(Reader characterStream) {
	}

	private InputStream byteStream;
	
	@Override
	public InputStream getByteStream() {
		return byteStream;
	}

	@Override
	public void setByteStream(InputStream byteStream) {
		this.byteStream = byteStream;
	}

	@Override
	public String getStringData() {
		return null;
	}

	@Override
	public void setStringData(String stringData) {
	}

	private String systemId, publicId, baseURI, encoding;
	
	@Override
	public String getSystemId() {
		return systemId;
	}

	@Override
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	@Override
	public String getPublicId() {
		return publicId;
	}

	@Override
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	@Override
	public String getBaseURI() {
		return baseURI;
	}

	@Override
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	@Override
	public boolean getCertifiedText() {
		return false;
	}

	@Override
	public void setCertifiedText(boolean certifiedText) {
	}
}
