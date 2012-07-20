package org.bigraph.model.loaders;

public final class LoaderNotice {
	public static enum Type {
		OK,
		INFO,
		WARNING,
		ERROR
	}
	
	private final Type type;
	private final String message;
	
	public LoaderNotice(Type type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "Notice[type=" + type + ", message=" + message + "]";
	}
}