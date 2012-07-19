package dk.itu.big_red.model.load_save;

public interface ILoader {
	void addNotice(LoaderNotice notice);
	void addNotice(LoaderNotice.Type type, String message);
}
