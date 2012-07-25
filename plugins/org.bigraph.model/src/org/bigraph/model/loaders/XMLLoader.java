package org.bigraph.model.loaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.IChangeExecutor;
import org.w3c.dom.Element;

public abstract class XMLLoader extends ChangeLoader implements IXMLLoader {
	public static String getAttributeNS(Element d, String nsURI, String n) {
		String r = d.getAttributeNS(nsURI, n);
		if (r.length() == 0 && d.getNamespaceURI().equals(nsURI))
			r = d.getAttributeNS(null, n);
		return (r.length() != 0 ? r : null);
	}

	public static int getIntAttribute(Element d, String nsURI, String n) {
		try {
			return Integer.parseInt(getAttributeNS(d, nsURI, n));
		} catch (Exception e) {
			return 0;
		}
	}

	public static double getDoubleAttribute(
			Element d, String nsURI, String n) {
		try {
			return Double.parseDouble(getAttributeNS(d, nsURI, n));
		} catch (Exception e) {
			return 0;
		}
	}

	private List<IXMLUndecorator> undecorators = null;

	protected List<IXMLUndecorator> getUndecorators() {
		return (undecorators != null ? undecorators :
				Collections.<IXMLUndecorator>emptyList());
	}

	protected void addUndecorator(IXMLUndecorator d) {
		if (d == null)
			return;
		if (undecorators == null)
			undecorators = new ArrayList<IXMLUndecorator>();
		undecorators.add(d);
		d.setLoader(this);
	}

	protected <T extends ModelObject> T executeUndecorators(T mo, Element el) {
		if (mo != null && el != null)
			for (IXMLUndecorator d : getUndecorators())
				d.undecorate(mo, el);
		return mo;
	}

	@Override
	protected void executeChanges(IChangeExecutor ex)
			throws LoadFailedException {
		for (IXMLUndecorator d : getUndecorators())
			d.finish(ex);
		super.executeChanges(ex);
	}
}
