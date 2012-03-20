package dk.itu.big_red.model.names;

import java.util.ArrayList;
import java.util.List;

public class OperatorNamePolicy implements INamePolicy {
	private static final List<String> operators;
	static {
		operators = new ArrayList<String>();
		
		operators.add("+");
		operators.add("*");
		operators.add("-");
		operators.add("/");
		
		operators.add("<");
		operators.add("<=");
		operators.add("=");
		operators.add(">=");
		operators.add(">");
	}
	
	@Override
	public String normalise(String name) {
		return (operators.contains(name = name.trim()) ? name : null);
	}

	@Override
	public String get(int value) {
		return operators.get(value % operators.size());
	}

}
