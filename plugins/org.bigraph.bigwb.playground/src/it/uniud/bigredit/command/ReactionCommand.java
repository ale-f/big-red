package it.uniud.bigredit.command;

import it.uniud.bigredit.model.MatchData;
import it.uniud.bigredit.model.Reaction;

import java.util.HashMap;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.utilities.ui.UI;



public class ReactionCommand extends Command {
	
	//private IWorkbenchPart part = null;
	private Bigraph    target = null;
	private Reaction rule = null;
	private boolean disabled = false;
	private boolean executed = false;
	
	private HashMap< OuterName, OuterName > nameMap = null;
	private HashMap< Site, Site > siteMap = null;
	MatchData matchData = null;
	//GraphMatching.UndoData undoData = null;
	
	public ReactionCommand( Reaction rule, Bigraph target )
	{
		this.target = target;
		this.rule = rule;
		//this.part = part;
	}
	
	public void disable()
	{
		disabled = true;
	}
	
	public boolean wasExecuted()
	{
		return executed;
	}
	
	@Override
	public boolean canExecute()
	{
		return target != null && rule != null && !disabled;
	}
	
	@Override
	public void execute()
	{
		setLabel( "Reaction" );
		ReactionWizard wizard = new ReactionWizard( rule, target );
		WizardDialog dialog = new WizardDialog( UI.getShell(), wizard );
		dialog.create();
		dialog.setTitle( "Reaction rule application" );
		dialog.setMessage( "Applying " + rule.toString() + " to " + target.getName() );
		if ( dialog.open() != WizardDialog.CANCEL ) {
			executed = true;
			//nameMap = wizard.getNameMap();
			//siteMap = wizard.getSiteMap();
			matchData = wizard.getMatchData();
			redo();
		}
	}
	
	@Override
	public void redo()
	{
		Root root = null;
		for ( ModelObject t : rule.getReactum().getChildren() ) {
			if ( t instanceof Root )
				root = ( Root )t;
		}
		if ( root != null ){}
			//undoData = GraphMatching.implementReaction( matchData, root, nameMap, siteMap );
	}
	
	@Override
	public void undo()
	{
//		if ( undoData != null )
//			GraphMatching.undoReaction( undoData );
	}

}
