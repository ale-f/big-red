package org.bigraph.model.utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
	
	/**
	 * A <strong>Token</strong> is a string of characters identified by a
	 * {@link Lexer} as conforming to a {@link TokenType}.
	 * @author alec
	 */
	public static final class Token {
		private final TokenType type;
		private final String value;
		
		public Token(TokenType type, String value) {
			this.type = type;
			this.value = value;
		}
		
		/**
		 * Returns the {@link TokenType} of this token.
		 * @return a {@link TokenType}; will not be {@code null}
		 */
		public TokenType getType() {
			return type;
		}
		
		/**
		 * Returns the value of this token.
		 * @return a {@link String}; will not be {@code null}
		 */
		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "<" + type.getName() + ":" + value + ">";
		}
	}
	
	/**
	 * Classes extending <strong>TokenIterator</strong> are iterators over
	 * collections of {@link Token}s, with a few additional methods to help
	 * parsers.
	 * @author alec
	 */
	public static abstract class TokenIterator implements Iterator<Token> {
		private Token current;
		
		protected abstract void prime();
		
		protected Token getCurrent() {
			return current;
		}
		
		protected Token setCurrent(Token current) {
			Token oldCurrent = this.current;
			this.current = current;
			return oldCurrent;
		}
		
		@Override
		public boolean hasNext() {
			prime();
			return (getCurrent() != null);
		}
		
		/**
		 * Returns the next token without consuming it.
		 * @return a {@link Token}, or {@code null} if no token is
		 * available
		 */
		public Token peek() {
			return (hasNext() ? getCurrent() : null);
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
				return setCurrent(null);
			} else throw new NoSuchElementException();
		}
		
		/**
		 * Consumes the next token, whose type must be one of the arguments
		 * to this method.
		 * @param ts a vararg list of {@link TokenType}s
		 * @return a {@link} Token; will not be {@code null}
		 * @throws NoSuchElementException if the next token is not
		 * appropriately typed
		 */
		public Token next(TokenType... ts) {
			Token token = tryNext(ts);
			if (token != null) {
				return token;
			} else throw new NoSuchElementException(
					"Expected " + Arrays.asList(ts) +
					", but got " + getCurrent());
		}
		
		/**
		 * Throws an {@link UnsupportedOperationException}.
		 * @throws UnsupportedOperationException always and forever
		 */
		@Override
		public final void remove() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public abstract TokenIterator clone();
	}
	
	/**
	 * Classes implementing <strong>Lexer</strong> are lexical analysers; they
	 * produce a stream of {@link Token}s from some input.
	 * @author alec
	 */
	public interface Lexer extends Iterable<Token> {
		@Override
		TokenIterator iterator();
	}
	
	public final class FlyLexer implements Lexer {
		private final String input;
		
		public FlyLexer(String input) {
			this.input = input;
		}
		
		public String getInput() {
			return input;
		}
		
		protected final class FlyTokenIterator extends TokenIterator {
			private int position;
			
			@Override
			protected void prime() {
				if (getCurrent() != null || position == getInput().length())
					return;

				boolean again;
				do {
					again = false;
					for (TokenType i : getTokenTypes()) {
						Matcher m = i.getPattern().matcher(getInput());
						if (m.find(position) && m.start() == position) {
							position = m.end();
							if (!i.shouldSkip()) {
								setCurrent(new Token(i, m.group()));
							} else again = true;
							break;
						}
					}
				} while (again);

				if (getCurrent() == null && position != getInput().length())
					throw new IllegalStateException(
							"Couldn't lex " + getInput().substring(position));
			}
			
			@Override
			public TokenIterator clone() {
				FlyTokenIterator ti = new FlyTokenIterator();
				ti.position = position;
				ti.setCurrent(getCurrent());
				return ti;
			}
		}
		
		@Override
		public TokenIterator iterator() {
			return new FlyTokenIterator();
		}
	}
	
	/**
	 * Returns an <i>unsafe lexer</i>, which opportunistically returns tokens
	 * without first checking that the input can be completely lexed.
	 * @see #safeLexer(String)
	 * @param input an input string
	 * @return a new, unsafe {@link Lexer}
	 */
	public Lexer lexer(String input) {
		return new FlyLexer(input);
	}
	
	public final class CollectingLexer implements Lexer {
		private final List<Token> tokens;
		
		public CollectingLexer(Lexer l) {
			tokens = CollectionUtilities.collect(l);
		}
		
		protected final class CollectedIterator extends TokenIterator {
			private int position;
			
			@Override
			protected void prime() {
				if (getCurrent() != null || position == tokens.size())
					return;
				setCurrent(tokens.get(position++));
			}
			
			@Override
			public TokenIterator clone() {
				CollectedIterator ci = new CollectedIterator();
				ci.position = position;
				ci.setCurrent(getCurrent());
				return ci;
			}
		}
		
		@Override
		public TokenIterator iterator() {
			return new CollectedIterator();
		}
	}
	
	/**
	 * Returns a <i>safe lexer</i>, which ensures that the input can be lexed
	 * completely before it returns any tokens.
	 * @see #lexer(String)
	 * @param input an input string
	 * @return a new, safe {@link Lexer}
	 */
	public Lexer safeLexer(String input) {
		return new CollectingLexer(lexer(input));
	}
}
