package org.bigraph.extensions.sortings;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

nameList ::= x ',' nameList
         ::= x

l ::= 'forall' nameList ':' u

u ::= x 'parent-of' y
  ::= x 'child-of' y
  ::= x 'ancestor-of' y
  ::= x 'descendant-of' y
  ::= x '[' n ']' '-/-' y '[' n ']'
  ::= x '[' n ']' '--' y '[' n ']'
  ::= u 'or' u
  ::= u 'and' u
  ::= 'ctrl' '(' x ')' '=' y
  ::= 'ctrl' '(' x ')' '!=' y
  ::= '(' u ')'
  ::= u '=>' u

*/

public class Lexer implements Iterable<Lexer.Token> {
	public static class Token {
		enum Type {
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
		}
		
		private final Type type;
		private final String value;
		
		protected Token(Type type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public Type getType() {
			return type;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return getType() + ":<" + getValue() + ">";
		}
	}
	
	private final String input;
	
	public Lexer(String input) {
		this.input = input;
	}
	
	public final class TokenIterator implements Iterator<Token> {
		private int position;
		
		private Token current;
		
		private void prime() {
			if (current != null || position == input.length())
				return;
			for (Token.Type i : Token.Type.values()) {
				Matcher m = i.pattern.matcher(input);
				if (m.find(position) && m.start() == position) {
					position = m.end();
					if (!i.skip) {
						current = new Token(i, m.group());
						break;
					}
				}
			}
			if (current == null && position != input.length())
				throw new IllegalStateException(
						"Couldn't lex " + input.substring(position));
		}
		
		@Override
		public boolean hasNext() {
			prime();
			return (current != null);
		}
		
		/**
		 * Returns the next token without consuming it.
		 * @return a {@link Token}, or {@code null} if
		 */
		public Token peek() {
			return (hasNext() ? current : null);
		}
		
		public Token tryNext(Token.Type... ts) {
			Token token = peek();
			if (token != null && ts != null)
				for (Token.Type t : ts)
					if (token.type == t)
						return next();
			return null;
		}
		
		@Override
		public Token next() {
			if (hasNext()) {
				Token r = current;
				current = null;
				return r;
			} else throw new NoSuchElementException();
		}
		
		public Token next(Token.Type... ts) {
			Token token = tryNext(ts);
			if (token != null) {
				return token;
			} else throw new NoSuchElementException();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public TokenIterator iterator() {
		return new TokenIterator();
	}
}
