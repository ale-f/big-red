package org.bigraph.bigmc.red.interfaces;

public class ModelCheckerFactory {
	private ModelCheckerFactory() {}
	
	private static final class Wrapper {
		private static final ModelCheckerFactory INSTANCE =
				new ModelCheckerFactory();
	}
	
	public static ModelCheckerFactory getInstance() {
		return Wrapper.INSTANCE;
	}
	
	public static IModelChecker getModelChecker(String identifier) {
		return null;
	}
}
