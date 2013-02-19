package org.bigraph.model.utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexerFactory {
	public interface TokenType {
		String getName();
		Pattern getPattern();
		
		boolean shouldSkip();
	}
	
	private final Collection<? extends TokenType> tokenTypes;
	
	public LexerFactory(TokenType... tokenTypes) {
		this(Arrays.asList(tokenTypes));
	}
	
	public LexerFactory(Collection<? extends TokenType> tokenTypes) {
		this.tokenTypes = tokenTypes;
	}
	
	public Collection<? extends TokenType> getTokenTypes() {
		return tokenTypes;
	}
	
	public static final class Token {
		private final TokenType type;
		private final String value;
		
		public Token(TokenType type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public TokenType getType() {
			return type;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "<" + type.getName() + ":" + value + ">";
		}
	}
	
	public class Lexer implements Iterable<Token> {
		private final String input;
		
		public Lexer(String input) {
			this.input = input;
		}
		
		public String getInput() {
			return input;
		}
		
		public final class TokenIterator implements Iterator<Token> {
			private int position;
			
			private Token current;
			
			private void prime() {
				if (current != null || position == getInput().length())
					return;
				for (TokenType i : getTokenTypes()) {
					Matcher m = i.getPattern().matcher(getInput());
					if (m.find(position) && m.start() == position) {
						position = m.end();
						if (!i.shouldSkip()) {
							current = new Token(i, m.group());
							break;
						}
					}
				}
				if (current == null && position != getInput().length())
					throw new IllegalStateException(
							"Couldn't lex " + getInput().substring(position));
			}
			
			@Override
			public boolean hasNext() {
				prime();
				return (current != null);
			}
			
			/**
			 * Returns the next token without consuming it.
			 * @return a {@link Token}, or {@code null} if no token is
			 * available
			 */
			public Token peek() {
				return (hasNext() ? current : null);
			}
			
			/**
			 * Consumes the next token if its type is one of the arguments to
			 * this method.
			 * @param ts a vararg list of {@link TokenType}s
			 * @return a {@link Token}, or {@code null} if a call to {@link
			 * #peek()} didn't produce an appropriately-typed token
			 */
			public Token tryNext(TokenType... ts) {
				Token token = peek();
				if (token != null && ts != null)
					for (TokenType t : ts)
						if (token.type.equals(t))
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
			
			/**
			 * Consumes the next token, whose type must be one of the arguments
			 * to this method.
			 * @param ts a vararg list of {@link TokenType}s
			 * @return a {@link} Token (will not be {@code null})
			 * @throws NoSuchElementException if the next token is not
			 * appropriately typed
			 */
			public Token next(TokenType... ts) {
				Token token = tryNext(ts);
				if (token != null) {
					return token;
				} else throw new NoSuchElementException();
			}
			
			/**
			 * Throws an {@link UnsupportedOperationException}.
			 * @throws UnsupportedOperationException always and forever
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			/**
			 * Returns a copy of this {@link TokenIterator}.
			 */
			@Override
			public TokenIterator clone() {
				TokenIterator ti = new TokenIterator();
				ti.current = current;
				ti.position = position;
				return ti;
			}
		}
		
		@Override
		public TokenIterator iterator() {
			return new TokenIterator();
		}
	}
	
	public Lexer lexer(String input) {
		return new Lexer(input);
	}
}
