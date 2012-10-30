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
	
	public class Lexer {
		public class DisappointedException extends Exception {
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
		
		public String accept(Pattern p) {
			String current = getCurrent();
			
			if (current != null && p.matcher(current).matches()) {
				String previous = current;
				this.current = null;
				return previous;
			} else return null;
		}
		
		public boolean lookahead1(Pattern p) {
			String current = getCurrent();
			return (current != null &&
					p.matcher(current).matches());
		}
		
		public String expect(Pattern p) throws DisappointedException {
			String s = accept(p);
			if (s != null) {
				return s;
			} else throw new DisappointedException(
					"Expected " + p + ", but got " + getCurrent());
		}
	}
}
