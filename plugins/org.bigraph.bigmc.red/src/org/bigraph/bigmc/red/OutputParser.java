package org.bigraph.bigmc.red;

import java.util.regex.Pattern;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.names.HashMapNamespace;
import org.bigraph.model.names.INamespace;

import dk.itu.big_red.model.LayoutUtilities;

import org.bigraph.bigmc.red.LexerFactoryFactory;
import org.bigraph.bigmc.red.LexerFactory.Lexer;
import org.bigraph.bigmc.red.LexerFactory.DisappointedException;
import org.bigraph.extensions.param.ParameterUtilities;

public class OutputParser {
	private static final LexerFactoryFactory lff = new LexerFactoryFactory();
	private static final LexerFactory lf;
	
	private Lexer lexer;
	
	private Signature s;
	
	public OutputParser setSignature(Signature s) {
		this.s = s;
		return this;
	}
	
	public OutputParser setString(String string) {
		lexer = lf.createLexer(string);
		return this;
	}
	
	public OutputParser() {
	}
	
	private static final Pattern
		P_NIL = lff.addTokenType("nil"),
		P_DOT = lff.addTokenType("\\."),
		P_BAR = lff.addTokenType("\\|"),
		P_LSQ = lff.addTokenType("\\["),
		P_RSQ = lff.addTokenType("\\]"),
		P_DSH = lff.addTokenType("-"),
		P_LBR = lff.addTokenType("\\("),
		P_RBR = lff.addTokenType("\\)"),
		P_COM = lff.addTokenType(","),
		P_NAM = lff.addTokenType("[a-zA-Z_][a-zA-Z0-9_]*"),
		P_SIT = lff.addTokenType("\\$[0-9]+");
	static {
		lf = lff.createLexerFactory();
	}
	
	private int x = 1;
	
	private Bigraph workingBigraph;
	private INamespace<Link> ns;
	
	private void parseChild(Container parent, ChangeDescriptorGroup cg)
			throws DisappointedException {
		if (lexer.accept(P_NIL) != null)
			return;
		String name = lexer.accept(P_NAM);
		if (name != null) { /* name is a control */
			Node n = null;
			String cn = null;
			String[] parts = name.split("_P__", 2);
			if (parts.length == 1) {
				n = new Node(s.getControl(name));
			} else if (parts.length == 2) {
				n = new Node(s.getControl(cn = parts[0]));
			} else throw new Error("Control name couldn't be matched");

			String nn = Integer.toString(x++);
			cg.add(parent.changeAddChild(n, nn));
			
			if (parts.length == 2)
				cg.add(new BoundDescriptor(workingBigraph,
						new ParameterUtilities.ChangeParameterDescriptor(
								new Node.Identifier(nn,
										new Control.Identifier(cn)),
								null, parts[1])));
			
			if (lexer.accept(P_LSQ) != null) { /* ports */
				int i = 0;
				do {
					String linkName = lexer.accept(P_NAM);
					if (linkName == null) {
						lexer.expect(P_DSH);
						i++;
						continue;
					}
					Link l = ns.get(linkName);
					if (l == null) {
						ns.put(linkName, (l = new OuterName()));
						cg.add(workingBigraph.changeAddChild(l, linkName));
					}
					cg.add(new BoundDescriptor(workingBigraph,
							new Point.ChangeConnectDescriptor(
									n.getPorts().get(i++).getIdentifier(),
									l.getIdentifier().getRenamed(linkName))));
				} while (lexer.accept(P_COM) != null);
				lexer.expect(P_RSQ);
			}
			/* Strictly speaking, this doesn't need to be a conditional -- on
			 * the other hand, it means that the input language can be parsed
			 * as well */
			if (lexer.accept(P_DOT) != null)
				parseChildren(n, cg);
		} else if ((name = lexer.accept(P_SIT)) != null) { /* name is a site id */
			Site s = new Site();
			cg.add(parent.changeAddChild(s, name.substring(1)));
		} else throw new Error("What(child-context): " + lexer.getCurrent());
	}
	
	private void parseChildren(Container parent, ChangeDescriptorGroup cg)
			throws DisappointedException {
		if (lexer.lookahead1(P_LBR)) {
			lexer.expect(P_LBR);
			do {
				parseChild(parent, cg);
			} while (lexer.accept(P_BAR) != null);
			lexer.expect(P_RBR);
		} else parseChild(parent, cg);
	}
	
	public Bigraph run() {
		try {
			ns = new HashMapNamespace<Link>();
			ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
			workingBigraph = new Bigraph();
			workingBigraph.setSignature(s);
			Root r = new Root();
			cg.add(workingBigraph.changeAddChild(r, "1"));
			
			parseChildren(r, cg);
			
			DescriptorExecutorManager.getInstance().tryApplyChange(cg);
			DescriptorExecutorManager.getInstance().tryApplyChange(
					LayoutUtilities.relayout(workingBigraph));
			
			return workingBigraph;
		} catch (ChangeCreationException cre) {
			cre.printStackTrace();
			return null;
		} catch (DisappointedException de) {
			de.printStackTrace();
			return null;
		}
	}
}
