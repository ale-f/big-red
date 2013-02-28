package org.bigraph.bigmc.red;

import java.util.regex.Pattern;

import org.bigraph.extensions.param.ParameterUtilities;
import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.PortSpec;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.BigraphOperations;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.utilities.CollectionUtilities;
import org.bigraph.model.utilities.LexerFactory;
import org.bigraph.model.utilities.LexerFactory.Token;
import org.bigraph.model.utilities.LexerFactory.TokenType;
import org.bigraph.model.utilities.LexerFactory.TokenIterator;
import org.bigraph.model.utilities.comparators.IntegerStringComparator;

import static org.bigraph.bigmc.red.BGMParser.Type.*;

public class BGMParser {
	private final TokenIterator it;
	
	public BGMParser(String input) {
		it = lf.lexer(input).iterator();
		System.out.println(CollectionUtilities.collect(it.clone()));
	}
	
	private static void change(Resolver r, IChangeDescriptor ch)
			throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(r, ch);
		System.out.println(ch);
	}
	
	static enum Type implements TokenType {
		WHITESPACE("\\s+", true),
		COMMENT("#(.*)$", true),
		
		NOT("!"),
		NIL("nil"),
		TWOBAR("\\|\\|"),
		ONEBAR("\\|"),
		LEFTSQ("\\["),
		RIGHTSQ("\\]"),
		LEFTPA("\\("),
		RIGHTPA("\\)"),
		DOT("\\."),
		DOLLAR("\\$"),
		ARROW("->"),
		DASH("-"),
		COMMA(","),
		ACTIVE("%active"),
		PASSIVE("%passive"),
		NAME("%(outer|name)"),
		INNER("%inner"),
		CHECK("%check"),
		COLON(":"),
		SEMICOLON(";"),
		PROPERTY("%property"),
		RULE("%rule"),
		IMPORT("%import"),
		IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"),
		INTEGER("[0-9]+");
		
		final Pattern pattern;
		final boolean skip;
		
		Type(String pattern) {
			this(pattern, false);
		}
		
		Type(String pattern, boolean skip) {
			this.pattern = Pattern.compile(pattern, Pattern.MULTILINE);
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
	
	private void property() {
		/* Discard properties for now */
		Token t;
		while ((t = it.peek()) != null && !SEMICOLON.equals(t.getType()))
			System.out.println(it.next());
	}
	
	private void makeRule(Bigraph lhs, Bigraph rhs)
			throws ChangeCreationException {
		ReactionRule rr = new ReactionRule();
		rr.setRedex(lhs);
		
		ChangeDescriptorGroup cdg = rr.getEdit().getDescriptors();
		PropertyScratchpad scratch = new PropertyScratchpad();
		
		Bigraph reactum = rr.getRedex().clone();
		
		for (Root i : reactum.getRoots())
			BigraphOperations.removeObject(cdg, scratch, i);
		
		for (Root i : NamedModelObject.order(
				rhs.getRoots(), IntegerStringComparator.INSTANCE))
			BigraphOperations.copyPlace(cdg, scratch, i, reactum);
		
		change(simulationSpec, new SimulationSpec.ChangeAddRuleDescriptor(
				new SimulationSpec.Identifier(), -1, rr));
	}
	
	private Bigraph makeBigraph() {
		Bigraph b = new Bigraph();
		b.setSignature(signature);
		return b;
	}
	
	private void reaction(String id) throws ChangeCreationException {
		Bigraph
			lhs = makeBigraph(),
			rhs = makeBigraph();
		parseRoots(lhs);
		it.next(ARROW);
		parseRoots(rhs);
		makeRule(lhs, rhs);
	}
	
	private void reaction_or_exp() throws ChangeCreationException {
		Bigraph
			lhs = makeBigraph(),
			rhs;
		parseRoots(lhs);
		if (it.tryNext(ARROW) != null) { /* reaction */
			parseRoots(rhs = makeBigraph());
			makeRule(lhs, rhs);
		} else change(simulationSpec,
				new SimulationSpec.ChangeSetModelDescriptor(
						new SimulationSpec.Identifier(),
						simulationSpec.getModel(), lhs));
	}
	
	private void parseRoots(Bigraph parent) throws ChangeCreationException {
		do {
			Root r = new Root();
			change(null, parent.changeAddChild(r,
					parent.getFirstUnusedName(r)));
			parseChildren(parent, r);
		} while (it.tryNext(TWOBAR) != null);
	}
	
	private void parseChildren(Bigraph b, Container parent)
			throws ChangeCreationException {
		if (it.tryNext(LEFTPA) != null) {
			do {
				parseChild(b, parent);
			} while (it.tryNext(ONEBAR) != null);
			it.next(RIGHTPA);
		} else parseChild(b, parent);
	}
	
	private void parseChild(Bigraph b, Container parent)
			throws ChangeCreationException {
		if (it.tryNext(NIL) != null)
			return;
		Token idT = it.tryNext(IDENTIFIER);
		if (idT != null) {
			String id = idT.getValue();
			Node n = null;
			String[] parts = id.split("_P__", 2);
			if (parts.length == 1) {
				n = new Node(signature.getControl(id));
			} else if (parts.length == 2) {
				n = new Node(signature.getControl(parts[0]));
			} else throw new RuntimeException(
					"Control name couldn't be matched");

			change(null, parent.changeAddChild(n, Integer.toString(x++)));
			
			if (parts.length == 2)
				change(b, new ParameterUtilities.ChangeParameterDescriptor(
						n.getIdentifier(), null, parts[1]));
			
			if (it.tryNext(LEFTSQ) != null) { /* ports */
				int i = 0;
				do {
					Token linkNameT = it.tryNext(IDENTIFIER);
					if (linkNameT == null) {
						it.next(DASH);
						i++;
						continue;
					}
					String linkName = linkNameT.getValue();
					OuterName.Identifier onid =
							new OuterName.Identifier(linkName);
					OuterName l = onid.lookup(null, b);
					if (l == null)
						change(null, b.changeAddChild(
								l = new OuterName(), linkName));
					change(b, new Point.ChangeConnectDescriptor(
							n.getPorts().get(i++).getIdentifier(), onid));
				} while (it.tryNext(COMMA) != null);
				it.next(RIGHTSQ);
			}
			
			if (it.tryNext(DOT) != null)
				parseChildren(b, n);
		} else if (it.tryNext(DOLLAR) != null) {
			String id = it.next(INTEGER).getValue();
			change(null, parent.changeAddChild(new Site(), id));
		}
	}
	
	private void dec() throws ChangeCreationException {
		Token controlTypeT;
		if (it.tryNext(INNER) != null) {
			it.next(IDENTIFIER);
		} else if (it.tryNext(NAME) != null) {
			it.next(IDENTIFIER);
		} else if ((controlTypeT = it.tryNext(ACTIVE, PASSIVE)) != null) {
			String controlType = controlTypeT.getValue();
			Control.Identifier cid =
					new Control.Identifier(it.next(IDENTIFIER).getValue());
			it.next(COLON);
			String arity = it.next(INTEGER).getValue();
			
			change(signature, new Signature.ChangeAddControlDescriptor(
					new Signature.Identifier(), cid));
			change(signature, new Control.ChangeKindDescriptor(
					cid, null,
					"%active".equals(controlType) ?
							Kind.ACTIVE : Kind.PASSIVE));
			for (int i = 0; i < Integer.parseInt(arity); i++)
				change(signature, new Control.ChangeAddPortSpecDescriptor(
						new PortSpec.Identifier("" + i, cid)));
		} else if (it.tryNext(PROPERTY) != null) {
			it.next(IDENTIFIER);
			property();
		} else if (it.tryNext(RULE) != null) {
			reaction(it.next(IDENTIFIER).getValue());
		} else if (it.tryNext(IMPORT) != null) {
			it.next(IDENTIFIER);
		} else reaction_or_exp();
	}
	
	private void model() throws ChangeCreationException {
		while (it.tryNext(CHECK) == null) {
			dec();
			it.next(SEMICOLON);
		}
	}
	
	private int x = 0;
	private Signature signature;
	private SimulationSpec simulationSpec;
	
	public SimulationSpec run() throws ChangeCreationException {
		simulationSpec = new SimulationSpec();
		Signature newSignature = signature = new Signature();
		change(simulationSpec,
				new SimulationSpec.ChangeSetSignatureDescriptor(
						new SimulationSpec.Identifier(),
						simulationSpec.getSignature(), newSignature));
		model();
		return simulationSpec;
	}
	
	private static final String test =
			"%active a : 1; # I'm a comment\n" + 
			"%active b : 2; # Hey, me too!\n" + 
			"%active c : 9;\n" + 
			"%active d : 8;\n" + 
			"%active e : 7;\n\n" + 
			"%property abacus : weasel backgammon ( fred$->());\n" +
			"d.(e.(d | b[-,x].(b[x,-] | c) | a[x]) | b[x,x].(b[-,-] | c));\n\n" + 
			"a -> b;\n\n" + 
			"%check";
	
	public static void main(String[] args) {
		try {
			new BGMParser(test).run();
		} catch (ChangeCreationException e) {
			e.printStackTrace();
		}
	}
}
