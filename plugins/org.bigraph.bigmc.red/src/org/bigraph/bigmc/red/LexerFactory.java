package org.bigraph.bigmc.red;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexerFactory {
	private final Pattern scanner;
	
	public LexerFactory(String expr) {
		scanner = Pattern.compile(expr);
	}
	
	public Lexer createLexer(String input) {
		return new Lexer(input);
	}
	
	public static class DisappointedException extends Exception {
		private static final long serialVersionUID = -8457674498082916079L;

		public DisappointedException() {
			super();
		}

		public DisappointedException(String message, Throwable cause) {
			super(message, cause);
		}

		public DisappointedException(String message) {
			super(message);
		}

		public DisappointedException(Throwable cause) {
			super(cause);
		}
	}
	
	public class Lexer {
		private final Matcher lexer;
		private String current;
		
		public Lexer(String input) {
			lexer = scanner.matcher(input);
		}
		
		public String getCurrent() {
			if (current == null)
				current = (lexer.find() ? lexer.group() : null);
			return current;
		}
		
		/**
		 * Consumes the next token if it matches {@code p}.
		 * @param p a {@link Pattern}
		 * @return the consumed token, or <code>null</code> if the next token
		 * doesn't match {@code p}
		 */
		public String accept(Pattern p) {
			String current = getCurrent();
			
			if (current != null && p.matcher(current).matches()) {
				this.current = null;
				return current;
			} else return null;
		}
		
		/**
		 * Consumes the next token.
		 * @return the consumed token
		 */
		public String consume() {
			String current = getCurrent();
			this.current = null;
			return current;
		}
		
		/**
		 * Examines the next token without consuming it.
		 * @param p a {@link Pattern}
		 * @return {@code true} if the next token will match {@code p}, or
		 * {@code false} otherwise
		 */
		public boolean lookahead1(Pattern p) {
			String current = getCurrent();
			return (current != null &&
					p.matcher(current).matches());
		}
		
		/**
		 * Consumes the next token if it matches {@code p}.
		 * @param p a {@link Pattern}
		 * @return the consumed token
		 * @throws DisappointedException if the next token doesn't match {@code
		 * p}
		 */
		public String expect(Pattern p) throws DisappointedException {
			String s = accept(p);
			if (s != null) {
				return s;
			} else throw new DisappointedException(
					"Expected " + p + ", but got " + getCurrent());
		}
	}
}
