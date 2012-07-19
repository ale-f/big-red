package dk.itu.big_red.model.load_save;

public final class LoaderNotice {
	public static enum Type {
		OK,
		INFO,
		WARNING,
		ERROR
	}
	
	private final Type type;
	private final String message;
	
	protected LoaderNotice(Type type, String message) {
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