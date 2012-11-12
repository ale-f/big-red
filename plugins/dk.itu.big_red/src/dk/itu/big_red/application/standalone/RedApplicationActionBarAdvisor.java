package dk.itu.big_red.application.standalone;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class RedApplicationActionBarAdvisor extends ActionBarAdvisor {
	private Object[] actionFile = null;
	private Object[] actionFileNew = null;
	private Object[] actionEdit = null;
	private Object[] actionWindow = null;
	private Object[] actionWindowShowView = null;
    private Object[] actionHelp = null;
    
	public RedApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}
	
	@Override
	protected void makeActions(IWorkbenchWindow window) {
		actionFile = new Object[] {
			new MenuManager("&New"),
			new Separator(),
			ActionFactory.CLOSE.create(window),
			ActionFactory.CLOSE_ALL.create(window),
			new Separator(),
			ActionFactory.SAVE.create(window),
			ActionFactory.SAVE_AS.create(window),
			ActionFactory.REVERT.create(window),
			new Separator(),
			ActionFactory.PRINT.create(window),
			new Separator(),
			ActionFactory.IMPORT.create(window),
			ActionFactory.EXPORT.create(window),
			new Separator(),
			ActionFactory.QUIT.create(window)
		};
		
		actionFileNew = new Object[] {
			new NewWizardMenu(window)
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
		
		actionWindow = new Object[] {
			new MenuManager("Show &View"),
			ActionFactory.PREFERENCES.create(window),
		};
		
		actionWindowShowView = new Object[] {
			ContributionItemFactory.VIEWS_SHORTLIST.create(window)
		};
		
		actionHelp = new Object[] {
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
	
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		MenuManager fileNewMenu = (MenuManager)actionFile[0];
		MenuManager editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
		MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		MenuManager windowShowViewMenu = (MenuManager)actionWindow[0];
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);

		populateMenu(fileMenu, actionFile);
		populateMenu(fileNewMenu, actionFileNew);
		populateMenu(editMenu, actionEdit);
		populateMenu(windowMenu, actionWindow);
		populateMenu(windowShowViewMenu, actionWindowShowView);
		populateMenu(helpMenu, actionHelp);
	}
}
