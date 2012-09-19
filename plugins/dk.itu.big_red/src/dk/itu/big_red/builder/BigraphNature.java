package dk.itu.big_red.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class BigraphNature implements IProjectNature {
	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID =
			"dk.itu.big_red.utilities.resources.builder.BigraphNature";

	private static final ICommand[] EMPTY = new ICommand[0];
	
	@Override
	public void configure() throws CoreException {
		IProjectDescription desc = getDescription();
		List<ICommand> commands = Arrays.asList(desc.getBuildSpec());

		for (ICommand i : commands)
			if (BigraphBuilder.BUILDER_ID.equals(i.getBuilderName()))
				return;

		ICommand newCommand = desc.newCommand();
		newCommand.setBuilderName(BigraphBuilder.BUILDER_ID);
		(commands = new ArrayList<ICommand>(commands)).add(newCommand);
		desc.setBuildSpec(commands.toArray(EMPTY));
		project.setDescription(desc, null);
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription desc = getDescription();
		List<ICommand> commands = Arrays.asList(desc.getBuildSpec());
		for (ICommand i : commands) {
			if (!BigraphBuilder.BUILDER_ID.equals(i))
				continue;
			(commands = new ArrayList<ICommand>(commands)).remove(i);
			desc.setBuildSpec(commands.toArray(EMPTY));
			project.setDescription(desc, null);			
			return;
		}
	}

	private IProject project;
	
	@Override
	public IProject getProject() {
		return project;
	}

	protected IProjectDescription getDescription() throws CoreException {
		return getProject().getDescription();
	}
	
	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
}
