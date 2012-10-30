package org.bigraph.bigmc.red;

import java.util.regex.Pattern;

import org.bigraph.bigmc.red.LexerFactory.Lexer;
import org.bigraph.bigmc.red.LexerFactory.Lexer.DisappointedException;
import org.bigraph.model.SimulationSpec;

public class BGMParser {
	private static final LexerFactoryFactory lff = new LexerFactoryFactory();
	private static final LexerFactory lf;
	
	private Lexer lexer;
	
	public BGMParser setString(String s) {
		lexer = lf.createLexer(s);
		return this;
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
			"%active a : 0;\n" + 
			"%active b : 0;\n" + 
			"%active c : 0;\n" + 
			"%active d : 0;\n" + 
			"%active e : 0;\n\n" + 
			"d.(e.(d | b.(b | c) | a) | b.(b | c));\n\n" + 
			"a -> b;\n\n" + 
			"%check";
	
	static {
		lf = lff.createLexerFactory();
	}
	
	private void property() throws DisappointedException {
	}
	
	private boolean reaction() throws DisappointedException {
		if (exp()) {
			lexer.expect(P_ARROW);
			exp();
		} else return false;
		return true;
	}
	
	private boolean reaction_or_exp() throws DisappointedException {
		if (exp()) {
			if (lexer.accept(P_ARROW) != null)
				exp();
		} else return false;
		return true;
	}
	
	private boolean exp() throws DisappointedException {
		if (expel()) {
			if (lexer.accept(P_TWOBAR) != null ||
					lexer.accept(P_ONEBAR) != null)
				exp();
		} else return false;
		return true;
	}
	
	private boolean expel() throws DisappointedException {
		if (prefix()) {
			if (lexer.accept(P_DOT) != null)
				expel();
		} else if (lexer.accept(P_DOLLAR) != null) {
			String id = lexer.expect(P_INTEGER);
		} else if (lexer.accept(P_LEFTPA) != null) {
			exp();
			lexer.expect(P_RIGHTPA);
		} else return false;
		return true;
	}
	
	private boolean prefix() throws DisappointedException {
		String id;
		if ((id = lexer.accept(P_IDENTIFIER)) != null) {
			if (lexer.accept(P_LEFTSQ) != null) { /* ports */
				do {
					String linkName = lexer.accept(P_IDENTIFIER);
					if (linkName == null)
						lexer.expect(P_DASH);
				} while (lexer.accept(P_COMMA) != null);
				lexer.expect(P_RIGHTSQ);
			}
		} else if ((id = lexer.accept(P_INTEGER)) != null) {
			;
		} else return false;
		return true;
	}
	
	private void dec() throws DisappointedException {
		String controlType;
		if (lexer.accept(P_INNER) != null) {
			String id = lexer.expect(P_IDENTIFIER);
			System.out.println("inner(" + id + ")");
		} else if (lexer.accept(P_NAME) != null) {
			String id = lexer.expect(P_IDENTIFIER);
			System.out.println("outer(" + id + ")");
		} else if ((controlType = lexer.accept(P_ACTIVE)) != null ||
				(controlType = lexer.accept(P_PASSIVE)) != null) {
			String id = lexer.expect(P_IDENTIFIER);
			lexer.expect(P_COLON);
			String arity = lexer.expect(P_INTEGER);
			System.out.println(
					"control(" + controlType + ", " + id + ", " + arity + ")");
		} else if (lexer.accept(P_PROPERTY) != null) {
			String id = lexer.expect(P_IDENTIFIER);
			property();
		} else if (lexer.accept(P_RULE) != null) {
			String id = lexer.expect(P_IDENTIFIER);
			reaction();
		} else if (lexer.accept(P_IMPORT) != null) {
			String id = lexer.expect(P_IDENTIFIER);
		} else reaction_or_exp();
	}
	
	private void model() throws DisappointedException {
		while (lexer.lookahead1(P_CHECK) == false) {
			dec();
			lexer.expect(P_SEMICOLON);
		}
	}
	
	public SimulationSpec run() throws DisappointedException {
		setString(test);
		model();
		return null;
	}
}
