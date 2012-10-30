package org.bigraph.bigmc.red;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LexerFactoryFactory {
	private ArrayList<String> subexprs = new ArrayList<String>();
	
	public Pattern addTokenType(String expr) {
		try {
			Pattern p = Pattern.compile("^" + expr + "$");
			subexprs.add(expr);
			return p;
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public LexerFactory createLexerFactory() {
		String expr = "";
		Iterator<String> it = subexprs.iterator();
		while (it.hasNext()) {
			expr += it.next();
			if (it.hasNext())
				expr += "|";
		}
		return new LexerFactory(expr);
	}
}
