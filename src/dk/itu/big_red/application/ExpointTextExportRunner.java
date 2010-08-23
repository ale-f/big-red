package dk.itu.big_red.application;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;

import dk.itu.big_red.model.import_export.ModelExport;

public class ExpointTextExportRunner {
	public static void registerExporters() {
		IConfigurationElement exporters[] =
			RegistryFactory.getRegistry().getConfigurationElementsFor("dk.itu.big_red.export.text");
		try {
			for (IConfigurationElement e : exporters) {
				Object ex = e.createExecutableExtension("class");
				if (ex instanceof ModelExport) {
					System.out.println("Hooraj!");
				}
			}
		} catch (CoreException e) {
			return;
		}
	}
}
