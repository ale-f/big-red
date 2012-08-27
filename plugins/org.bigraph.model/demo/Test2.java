import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

import org.bigraph.model.Bigraph;

import org.bigraph.model.savers.BigraphXMLSaver;
import org.bigraph.model.savers.SaveFailedException;

import org.bigraph.model.loaders.BigraphXMLLoader;
import org.bigraph.model.loaders.LoadFailedException;

import org.bigraph.model.resources.JavaFileWrapper;

public class Test2 {
	public static void main(String[] args) {
		File f = new File("../../../doc/examples/airport/agents/" +
			"airport.bigraph-agent");
		JavaFileWrapper fw = new JavaFileWrapper(f);

		Bigraph b = null;
		BigraphXMLLoader l = new BigraphXMLLoader();
		try {
			l.setFile(fw);
			l.setInputStream(new FileInputStream(f));
			b = l.importObject();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (LoadFailedException lfe) {
			lfe.printStackTrace();
			System.exit(-1);
		}

		BigraphXMLSaver s = new BigraphXMLSaver();
		s.setFile(fw).setModel(b).setOutputStream(System.out);
		try {
			s.exportObject();
		} catch (SaveFailedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
