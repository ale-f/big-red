package dk.itu.big_red.intro;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;

import dk.itu.big_red.actions.WizardAction;
import dk.itu.big_red.actions.FileNewAction;
import dk.itu.big_red.actions.FileOpenAction;
import dk.itu.big_red.wizards.ControlsWizard;
import dk.itu.big_red.wizards.PortsWizard;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private Object[] actionFile = null;
	private Object[] actionEdit = null;
	private Object[] actionBigraph = null;
	private Object[] actionWindow = null;
	private Object[] actionWindowShowView = null;
    private Object[] actionHelp = null;
    
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}
	
	protected void makeActions(IWorkbenchWindow window) {
		actionFile = new Object[] {
			new FileNewAction(window),
			new FileOpenAction(window),
			new Separator(),
			ActionFactory.SAVE.create(window),
			ActionFactory.SAVE_AS.create(window),
			ActionFactory.REVERT.create(window),
			new Separator(),
			ActionFactory.PRINT.create(window),
			new Separator(),
			ActionFactory.EXPORT.create(window),
			new Separator(),
			ActionFactory.CLOSE.create(window),
			ActionFactory.QUIT.create(window)
		};
		
		actionEdit = new Object[] {
			ActionFactory.UNDO.create(window),
			ActionFactory.REDO.create(window),
			new Separator(),
			ActionFactory.CUT.create(window),
			ActionFactory.COPY.create(window),
			ActionFactory.PASTE.create(window),
			new Separator(),
			ActionFactory.DELETE.create(window),
			new Separator(),
			ActionFactory.SELECT_ALL.create(window)
		};

		actionBigraph = new Object[] {
			new WizardAction(window, "ports", "&Ports...",
					PortsWizard.class),
			new WizardAction(window, "controls", "&Controls...",
					ControlsWizard.class)
		};
		
		actionWindow = new Object[] {
			new MenuManager("Show &View")
		};
		
		actionWindowShowView = new Object[] {
			ContributionItemFactory.VIEWS_SHORTLIST.create(window)
		};
		
		actionHelp = new Object[] {
			ActionFactory.INTRO.create(window),
			new Separator(),
			ActionFactory.ABOUT.create(window)
		};
	}

	protected void populateMenu(MenuManager menu, Object[] items) {
		for (int i = 0; i < items.length; i++) {
			Object item = items[i];
			if (item instanceof IAction) {
				menu.add((IAction)item);
				register((IAction)item);
			} else if (item instanceof IContributionItem) {
				menu.add((IContributionItem)item);
			}
		}
	}
	
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		MenuManager editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
		MenuManager bigraphMenu = new MenuManager("&Bigraph", "bigraph");
		MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		MenuManager windowShowViewMenu = (MenuManager)actionWindow[0];
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(bigraphMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);

		populateMenu(fileMenu, actionFile);
		populateMenu(editMenu, actionEdit);
		populateMenu(bigraphMenu, actionBigraph);
		populateMenu(windowMenu, actionWindow);
		populateMenu(windowShowViewMenu, actionWindowShowView);
		populateMenu(helpMenu, actionHelp);
	}

}
