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
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.utilities.LexerFactory;
import org.bigraph.model.utilities.LexerFactory.Token;
import org.bigraph.model.utilities.LexerFactory.TokenType;
import org.bigraph.model.utilities.LexerFactory.TokenIterator;

import dk.itu.big_red.model.LayoutUtilities;

import org.bigraph.extensions.param.ParameterUtilities;

import static org.bigraph.bigmc.red.OutputParser.Type.*;

public class OutputParser {
	static enum Type implements TokenType {
		WHITESPACE("\\s+", true),
		
		NIL("nil"),
		DOT("\\."),
		BAR("\\|"),
		LSQ("\\["),
		RSQ("\\]"),
		DSH("-"),
		LBR("\\("),
		RBR("\\)"),
		COM(","),
		NAM("[a-zA-Z_][a-zA-Z0-9_]*"),
		SIT("\\$[0-9]+");
		
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
	
	private static final LexerFactory lf = new LexerFactory(Type.values());
	
	private TokenIterator it;
	
	private Signature signature;
	
	public OutputParser(Signature signature, String input) {
		this.signature = signature;
		it = lf.lexer(input).iterator();
	}
	
	private int x = 1;
	
	private Bigraph bigraph;
	
	private void change(IChangeDescriptor cd) throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(bigraph, cd);
	}
	
	private void parseChild(Container parent) throws ChangeCreationException {
		if (it.tryNext(NIL) != null)
			return;
		Token nameT = it.tryNext(NAM);
		if (nameT != null) { /* name is a control */
			String name = nameT.getValue();
			Node n = null;
			String cn = null;
			String[] parts = name.split("_P__", 2);
			if (parts.length < 3) {
				n = new Node(signature.getControl(cn = parts[0]));
			} else throw new RuntimeException(
					"Control name couldn't be matched");

			String nn = Integer.toString(x++);
			change(parent.changeAddChild(n, nn));
			
			if (parts.length == 2)
				change(new ParameterUtilities.ChangeParameterDescriptor(
						new Node.Identifier(nn,
								new Control.Identifier(cn)),
						null, parts[1]));
			
			if (it.tryNext(LSQ) != null) { /* ports */
				int i = 0;
				do {
					Token linkNameT = it.tryNext(NAM);
					if (linkNameT == null) {
						it.next(DSH);
						i++;
						continue;
					}
					String linkName = linkNameT.getValue();
					Link l = (Link)bigraph.getNamespace(Link.class).get(
							linkName);
					if (l == null)
						change(bigraph.changeAddChild(
								l = new OuterName(), linkName));
					change(new Point.ChangeConnectDescriptor(
							n.getPorts().get(i++).getIdentifier(),
							l.getIdentifier().getRenamed(linkName)));
				} while (it.tryNext(COM) != null);
				it.next(RSQ);
			}
			/* Strictly speaking, this doesn't need to be a conditional -- on
			 * the other hand, it means that the input language can be parsed
			 * as well */
			if (it.tryNext(DOT) != null)
				parseChildren(n);
		} else if ((nameT = it.tryNext(SIT)) != null) { /* name is a site id */
			Site s = new Site();
			change(parent.changeAddChild(s, nameT.getValue().substring(1)));
		}
	}
	
	private void parseChildren(Container parent)
			throws ChangeCreationException {
		if (it.tryNext(LBR) != null) {
			do {
				parseChild(parent);
			} while (it.tryNext(BAR) != null);
			it.next(RBR);
		} else parseChild(parent);
	}
	
	public Bigraph run() {
		try {
			bigraph = new Bigraph();
			bigraph.setSignature(signature);
			
			Root r = new Root();
			change(bigraph.changeAddChild(r, "1"));
			
			parseChildren(r);
			
			DescriptorExecutorManager.getInstance().tryApplyChange(
					bigraph, LayoutUtilities.relayout(bigraph));
			
			return bigraph;
		} catch (ChangeCreationException cre) {
			cre.printStackTrace();
			return null;
		}
	}
}
