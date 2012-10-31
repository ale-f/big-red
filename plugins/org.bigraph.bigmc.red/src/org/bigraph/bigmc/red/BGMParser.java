package org.bigraph.bigmc.red;

import java.util.regex.Pattern;

import org.bigraph.bigmc.red.LexerFactory.Lexer;
import org.bigraph.bigmc.red.LexerFactory.DisappointedException;
import org.bigraph.extensions.param.ParameterUtilities;
import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.PortSpec;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class BGMParser {
	private static final LexerFactoryFactory lff = new LexerFactoryFactory();
	private static final LexerFactory lf;
	
	private Lexer lexer;
	
	public BGMParser setString(String s) {
		lexer = lf.createLexer(s);
		return this;
	}
	
	private void change(IChange ch) throws ChangeRejectedException {
		ExecutorManager.getInstance().tryApplyChange(ch);
		System.out.println(ch);
	}
	
	private static final Pattern
		P_NIL = lff.addTokenType("nil"),
		P_TWOBAR = lff.addTokenType("\\|\\|"),
		P_ONEBAR = lff.addTokenType("\\|"),
		P_LEFTSQ = lff.addTokenType("\\["),
		P_RIGHTSQ = lff.addTokenType("\\]"),
		P_LEFTPA = lff.addTokenType("\\("),
		P_RIGHTPA = lff.addTokenType("\\)"),
		P_DOT = lff.addTokenType("\\."),
		P_DOLLAR = lff.addTokenType("$"),
		P_ARROW = lff.addTokenType("->"),
		P_DASH = lff.addTokenType("-"),
		P_COMMA = lff.addTokenType(","),
		P_ACTIVE = lff.addTokenType("%active"),
		P_PASSIVE = lff.addTokenType("%passive"),
		P_NAME = lff.addTokenType("%(outer|name)"),
		P_INNER = lff.addTokenType("%inner"),
		P_CHECK = lff.addTokenType("%check"),
		P_COLON = lff.addTokenType(":"),
		P_SEMICOLON = lff.addTokenType(";"),
		P_PROPERTY = lff.addTokenType("%property"),
		P_RULE = lff.addTokenType("%rule"),
		P_IMPORT = lff.addTokenType("%import"),
		P_IDENTIFIER = lff.addTokenType("[a-zA-Z_][a-zA-Z0-9_]*"),
		P_INTEGER = lff.addTokenType("[0-9]+");
	
	private static final String test =
			"%active a : 1;\n" + 
			"%active b : 2;\n" + 
			"%active c : 9;\n" + 
			"%active d : 8;\n" + 
			"%active e : 7;\n\n" + 
			"%property abacus : weasel backgammon ( fred$->());\n" +
			"d.(e.(d | b[-,x].(b[x,-] | c) | a[x]) | b[x,x].(b[-,-] | c));\n\n" + 
			"a -> b;\n\n" + 
			"%check";
	
	static {
		lf = lff.createLexerFactory();
	}
	
	private void property() {
		/* Discard properties for now */
		while (!lexer.lookahead1(P_SEMICOLON))
			System.out.println(lexer.consume());
	}
	
	private static ReactionRule makeRule(Bigraph lhs, Bigraph rhs) {
		return null;
	}
	
	private Bigraph makeBigraph() {
		Bigraph b = new Bigraph();
		b.setSignature(signature);
		return b;
	}
	
	private void reaction(String id)
			throws DisappointedException, ChangeRejectedException {
		Bigraph
			lhs = makeBigraph(),
			rhs = makeBigraph();
		parseRoots(lhs);
		lexer.expect(P_ARROW);
		parseRoots(rhs);
		change(simulationSpec.changeAddRule(makeRule(lhs, rhs)));
	}
	
	private void reaction_or_exp()
			throws DisappointedException, ChangeRejectedException {
		Bigraph
			lhs = makeBigraph(),
			rhs;
		parseRoots(lhs);
		if (lexer.accept(P_ARROW) != null) { /* reaction */
			parseRoots(rhs = makeBigraph());
			change(simulationSpec.changeAddRule(makeRule(lhs, rhs)));
		} else change(simulationSpec.changeModel(lhs));
	}
	
	private void parseRoots(Bigraph parent)
			throws DisappointedException, ChangeRejectedException {
		do {
			Root r = new Root();
			change(parent.changeAddChild(r,
					parent.getFirstUnusedName(r)));
			parseChildren(parent, r);
		} while (lexer.accept(P_TWOBAR) != null);
	}
	
	private void parseChildren(Bigraph b, Container parent)
			throws DisappointedException, ChangeRejectedException {
		if (lexer.accept(P_LEFTPA) != null) {
			do {
				parseChild(b, parent);
			} while (lexer.accept(P_ONEBAR) != null);
			lexer.expect(P_RIGHTPA);
		} else parseChild(b, parent);
	}
	
	private void parseChild(Bigraph b, Container parent)
			throws DisappointedException, ChangeRejectedException {
		if (lexer.accept(P_NIL) != null)
			return;
		String id = lexer.accept(P_IDENTIFIER);
		if (id != null) {
			Node n = null;
			String[] parts = id.split("_P__", 2);
			if (parts.length == 1) {
				n = new Node(signature.getControl(id));
			} else if (parts.length == 2) {
				n = new Node(signature.getControl(parts[0]));
				ParameterUtilities.setParameter(n, parts[1]);
			} else throw new Error("Control name couldn't be matched");

			change(parent.changeAddChild(n, Integer.toString(x++)));
			if (lexer.accept(P_LEFTSQ) != null) { /* ports */
				int i = 0;
				do {
					String linkName = lexer.accept(P_IDENTIFIER);
					if (linkName == null) {
						lexer.expect(P_DASH);
						i++;
						continue;
					}
					OuterName l =
							new OuterName.Identifier(linkName).lookup(null, b);
					if (l == null)
						change(b.changeAddChild(l = new OuterName(), linkName));
					change(n.getPorts().get(i++).changeConnect(l));
				} while (lexer.accept(P_COMMA) != null);
				lexer.expect(P_RIGHTSQ);
			}
			
			if (lexer.accept(P_DOT) != null)
				parseChildren(b, n);
		} else if (lexer.accept(P_DOLLAR) != null) {
			id = lexer.accept(P_INTEGER);
			change(parent.changeAddChild(new Site(), id));
		}
	}
	
	private void dec()
			throws DisappointedException, ChangeRejectedException {
		String controlType;
		if (lexer.accept(P_INNER) != null) {
			lexer.expect(P_IDENTIFIER);
		} else if (lexer.accept(P_NAME) != null) {
			lexer.expect(P_IDENTIFIER);
		} else if ((controlType = lexer.accept(P_ACTIVE)) != null ||
				(controlType = lexer.accept(P_PASSIVE)) != null) {
			String id = lexer.expect(P_IDENTIFIER);
			lexer.expect(P_COLON);
			String arity = lexer.expect(P_INTEGER);
			
			Control c = new Control();
			ChangeGroup cg = new ChangeGroup();
			cg.add(signature.changeAddControl(c, id));
			cg.add(c.changeKind("%active".equals(controlType) ?
					Kind.ACTIVE : Kind.PASSIVE));
			for (int i = 0; i < Integer.parseInt(arity); i++)
				cg.add(c.changeAddPort(new PortSpec(), "" + i));
			change(cg);
		} else if (lexer.accept(P_PROPERTY) != null) {
			lexer.expect(P_IDENTIFIER);
			property();
		} else if (lexer.accept(P_RULE) != null) {
			reaction(lexer.expect(P_IDENTIFIER));
		} else if (lexer.accept(P_IMPORT) != null) {
			String id = lexer.expect(P_IDENTIFIER);
		} else reaction_or_exp();
	}
	
	private void model()
			throws DisappointedException, ChangeRejectedException {
		while (lexer.lookahead1(P_CHECK) == false) {
			dec();
			lexer.expect(P_SEMICOLON);
		}
	}
	
	private int x = 0;
	private Signature signature;
	private SimulationSpec simulationSpec;
	
	public SimulationSpec run() throws DisappointedException {
		setString(test);
		simulationSpec = new SimulationSpec();
		try {
			change(simulationSpec.changeSignature(
					signature = new Signature()));
			model();
			return simulationSpec;
		} catch (ChangeRejectedException cre) {
			return null;
		}
	}
}
