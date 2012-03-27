package it.uniud.bigredit.editparts;

import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.ui.IWorkbenchPart;

public class BigreditRootEditPart extends ScalableRootEditPart {

	private IWorkbenchPart part;
	
	public BigreditRootEditPart( IWorkbenchPart part )
	{
		this.part = part;
	}
	public IWorkbenchPart getWorkbenchPart()
	{
		return part;
	}
	
}
