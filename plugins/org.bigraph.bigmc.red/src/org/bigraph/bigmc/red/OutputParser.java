package org.bigraph.bigmc.red;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.names.HashMapNamespace;
import dk.itu.big_red.model.names.INamespace;


public class OutputParser {
	private String string;
	private static final Pattern scanner = Pattern.compile(
			"(\\(|\\)|\\[|\\]|,|-|\\$[0-9]+|[a-zA-Z_][a-zA-Z0-9_]*|\\.|\\|)");
	
	private Matcher matcher;
	private String current;
	
	private Signature s;
	
	public OutputParser setSignature(Signature s) {
		this.s = s;
		return this;
	}
	
	private void prime() {
		if (matcher == null)
			matcher = scanner.matcher(string);
		if (current == null)
			current = (matcher.find() ? matcher.group() : null);
	}
	
	private String accept(Pattern p) {
		prime();
		
		if (current != null && p.matcher(current).matches()) {
			String previous = current;
			current = null;
			return previous;
		} else return null;
	}
	
	private boolean lookahead(Pattern p) {
		prime();
		return (current != null && p.matcher(current).matches());
	}
	
	private String expect(Pattern p) {
		String s = accept(p);
		if (s != null) {
			return s;
		} else throw new Error("Oh no, expected " + p.toString() + " but current is " + current);
	}
	
	public OutputParser setString(String string) {
		this.string = string;
		matcher = null;
		current = null;
		return this;
	}
	
	public OutputParser() {
	}
	
	private static final Pattern
		P_NIL = Pattern.compile("^nil$"),
		P_DOT = Pattern.compile("^\\.$"),
		P_BAR = Pattern.compile("^\\|$"),
		P_LSQ = Pattern.compile("^\\[$"),
		P_RSQ = Pattern.compile("^\\]$"),
		P_DSH = Pattern.compile("^-$"),
		P_LBR = Pattern.compile("^\\($"),
		P_RBR = Pattern.compile("^\\)$"),
		P_COM = Pattern.compile("^,$"),
		P_NAM = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$"),
		P_SIT = Pattern.compile("^\\$[0-9]+$");
	
	private int x = 1;
	
	private Bigraph workingBigraph;
	private INamespace<Link> ns;
	
	private void parseChild(Container parent, ChangeGroup cg) {
		if (accept(P_NIL) != null)
			return;
		String name = accept(P_NAM);
		if (name != null) { /* name is a control */
			Node n = null;
			String[] parts = name.split("_P__", 2);
			if (parts.length == 1) {
				n = new Node(s.getControl(name));
			} else if (parts.length == 2) {
				n = new Node(s.getControl(parts[0]));
				ExtendedDataUtilities.setParameter(n, parts[1]);
			} else throw new Error("Control name couldn't be matched");
			
			cg.add(parent.changeAddChild(n, Integer.toString(x++)));
			if (accept(P_LSQ) != null) { /* ports */
				int i = 0;
				do {
					String linkName = accept(P_NAM);
					if (linkName == null) {
						expect(P_DSH);
						i++;
						continue;
					}
					Link l = ns.get(linkName);
					if (l == null) {
						ns.put(linkName, (l = new OuterName()));
						cg.add(workingBigraph.changeAddChild(l, linkName));
					}
					cg.add(n.getPorts().get(i++).changeConnect(l));
				} while (accept(P_COM) != null);
				expect(P_RSQ);
			}
			/* Strictly speaking, this doesn't need to be a conditional -- on
			 * the other hand, it means that the input language can be parsed
			 * as well */
			if (accept(P_DOT) != null) { /* children */
				if (lookahead(P_LBR)) {
					parseChildren(n, cg);
				} else parseChild(n, cg);
			}
		} else if ((name = accept(P_SIT)) != null) { /* name is a site id */
			Site s = new Site();
			cg.add(parent.changeAddChild(s, name.substring(1)));
		} else throw new Error("What(child-context): " + current);
	}
	
	private void parseChildren(Container parent, ChangeGroup cg) {
		expect(P_LBR);
		do {
			parseChild(parent, cg);
		} while (accept(P_BAR) != null);
		expect(P_RBR);
	}
	
	public Bigraph run() {
		try {
			ns = new HashMapNamespace<Link>();
			ChangeGroup cg = new ChangeGroup();
			workingBigraph = new Bigraph();
			workingBigraph.setSignature(s);
			Root r = new Root();
			cg.add(workingBigraph.changeAddChild(r, "1"));
			
			parseChildren(r, cg);
			
			workingBigraph.tryApplyChange(cg);
			workingBigraph.tryApplyChange(
					ExtendedDataUtilities.relayout(workingBigraph));
			
			return workingBigraph;
		} catch (ChangeRejectedException cre) {
			cre.printStackTrace();
			return null;
		}
	}
}
