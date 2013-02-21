package org.bigraph.extensions.sortings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bigraph.model.utilities.LexerFactory;
import org.bigraph.model.utilities.LexerFactory.TokenType;
import org.bigraph.model.utilities.LexerFactory.TokenIterator;

import static org.bigraph.extensions.sortings.Parser.Type.*;

final class Parser {
	enum Type implements TokenType {
		WHITESPACE("\\s+", true),
		
		FORALL("forall"),
		PARENT_OF("parent-of"),
		ANCESTOR_OF("ancestor-of"),
		OR("or"),
		AND("and"),
		CTRL("ctrl"),
		COMMA(","),
		COLON(":"),
		LEFT_BR("\\("),
		RIGHT_BR("\\)"),
		AT("@"),
		IMPLIES("=>"),
		NOT_EQUAL("!="),
		EQUAL("="),
		LINKED("--"),
		NOT_LINKED("-/-"),
		IDENT("[a-zA-Z_\\-$][a-zA-Z0-9_\\-$]*"),
		NUM("[0-9]+");
		
		final Pattern pattern;
		final boolean skip;
		
		Type(String pattern) {
			this(pattern, false);
		}
		
		Type(String pattern, boolean skip) {
			this.pattern = Pattern.compile(pattern);
			this.skip = skip;
		}
		
		@Override
		public String getName() {
			return toString();
		}
		
		@Override
		public Pattern getPattern() {
			return pattern;
		}
		
		@Override
		public boolean shouldSkip() {
			return skip;
		}
	}
	
	public enum LOper {
		ParentOf("parent-of"),
		AncestorOf("ancestor-of"),
		NotLinked("-/-"),
		Linked("--"),
		NotEqual("!="),
		Equal("="),
		LAnd("and"),
		LOr("or"),
		CtrlEq("has-control"),
		CtrlNeq("does-not-have-control"),
		Implies("=>");
		
		final String str;
		LOper(String str) {
			this.str = str;
		}
		
		@Override
		public String toString() {
			return str;
		}
	}
	
	abstract static class LTerm {
	}
	
	public static final class LBinOp extends LTerm {
		public final LTerm lhs, rhs;
		public final LOper pred;
		
		public LBinOp(LTerm lhs, LOper pred, LTerm rhs) {
			this.lhs = lhs;
			this.pred = pred;
			this.rhs = rhs;
		}
		
		@Override
		public String toString() {
			return "(" + lhs + " " + pred + " " + rhs + ")";
		}
	}
	
	public static final class LIdent extends LTerm {
		public final String id;
		
		public LIdent(String id) {
			this.id = id;
		}
		
		@Override
		public String toString() {
			return id;
		}
	}
	
	public static final class LPort extends LTerm {
		public final LTerm lhs;
		public final int id;
		
		public LPort(LTerm lhs, int id) {
			this.lhs = lhs;
			this.id = id;
		}
		
		@Override
		public String toString() {
			return "" + lhs + "@" + id;
		}
	}
	
	public static final class LPredicate extends LTerm {
		public final List<String> vars;
		public final LTerm body;
		
		public LPredicate(List<String> vars, LTerm body) {
			this.vars = vars;
			this.body = body;
		}
		
		@Override
		public String toString() {
			return "forall " + vars + " : " + body;
		}
	}
	
	static final LBinOp ctrl(TokenIterator it) {
		if (it.tryNext(CTRL) != null) {
			it.next(LEFT_BR);
			LIdent x = new LIdent(it.next(IDENT).getValue());
			it.next(RIGHT_BR);
			TokenType tt = it.next(EQUAL, NOT_EQUAL).getType();
			LIdent y = new LIdent(it.next(IDENT).getValue());
			return new LBinOp(x,
					(tt == EQUAL ? LOper.CtrlEq : LOper.CtrlNeq), y);
		} else return null;
	}
	
	static final LTerm term(TokenIterator it) {
		LTerm t = ctrl(it);
		if (t == null) {
			t = new LIdent(it.next(IDENT).getValue());
			if (it.tryNext(AT) != null)
				t = new LPort(t, Integer.parseInt(
						it.next(NUM).getValue()));
		}
		return t;
	}
	
	static final LOper binop(TokenIterator it) {
		if (it.tryNext(PARENT_OF) != null) {
			return LOper.ParentOf;
		} else if (it.tryNext(ANCESTOR_OF) != null) {
			return LOper.AncestorOf;
		} else if (it.tryNext(NOT_LINKED) != null) {
			return LOper.NotLinked;
		} else if (it.tryNext(NOT_EQUAL) != null) {
			return LOper.NotEqual;
		} else if (it.tryNext(LINKED) != null) {
			return LOper.Linked;
		} else if (it.tryNext(EQUAL) != null) {
			return LOper.Equal;
		} else return null;
	}
	
	static final LTerm expr(TokenIterator it) {
		LTerm r;
		if (it.tryNext(LEFT_BR) == null) {
			r = term(it);
			LOper op = binop(it);
			if (op != null)
				r = new LBinOp(r, op, expr(it));
		} else {
			r = tlexpr(it);
			it.next(RIGHT_BR);
		}
		return r;
	}
	
	static final LTerm tlexpr(TokenIterator it) {
		LTerm r;
		if (it.tryNext(LEFT_BR) == null) {
			r = expr(it);
			if (it.tryNext(AND) != null) {
				r = new LBinOp(r, LOper.LAnd, tlexpr(it));
			} else if (it.tryNext(OR) != null) {
				r = new LBinOp(r, LOper.LOr, tlexpr(it));
			}
		} else {
			r = tlexpr(it);
			it.next(RIGHT_BR);
		}
		return r;
	}
	
	static final LTerm impl(TokenIterator it) {
		LTerm x = tlexpr(it);
		if (it.tryNext(IMPLIES) == null) {
			return x;
		} else return new LBinOp(x, LOper.Implies, tlexpr(it));
	}
	
	static final List<String> nameList(TokenIterator it) {
		List<String> names = new ArrayList<String>();
		
		do {
			names.add(it.next(IDENT).getValue());
		} while (it.tryNext(COMMA) != null);
		
		return names;
	}
	
	static final LPredicate pred(TokenIterator it) {
		it.next(FORALL);
		List<String> x = nameList(it);
		it.next(COLON);
		LTerm y = impl(it);
		return new LPredicate(x, y);
	}
	
	public static final LPredicate parse(String input) {
		return pred(new LexerFactory(Type.values()).lexer(input).iterator());
	}
	
	public static void main(String[] args) {
		System.out.println(parse(
				"forall a, b, c: " +
				"ctrl(a) = dog and ctrl(b) = owner and ctrl(c) != park and " +
				"c parent-of a and c parent-of b => a@1 -- b@1"));
	}
}
