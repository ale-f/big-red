package dk.itu.big_red.editors;

import java.util.EventObject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import dk.itu.big_red.editors.assistants.SignatureEditorPolygonCanvas;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.util.Utility;

public class SignatureEditor extends EditorPart implements CommandStackListener, ISelectionListener {
	public static final String ID = "dk.itu.big_red.SignatureEditor";
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		// TODO Auto-generated method stub
		setSite(site);
		setInput(input);
		firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	private Signature signature;
	private dk.itu.big_red.model.Control currentControl;
	
	private List controls;
	private Button addControl, removeControl;
	
	private Text name, label;
	private List ports;
	private Button addPort, removePort;
	private ScrollingGraphicalViewer viewer;
	private Button resizable, portsMovable;
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		
		Composite left = new Composite(parent, 0);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout leftLayout = new GridLayout(1, false);
		left.setLayout(leftLayout);
		
		controls = new List(left, SWT.SINGLE | SWT.BORDER);
		GridData controlsLayoutData =
			new GridData(SWT.FILL, SWT.FILL, true, true);
		controlsLayoutData.widthHint = 100;
		controls.setLayoutData(controlsLayoutData);
		
		Composite controlButtons = new Composite(left, SWT.NONE);
		RowLayout controlButtonsLayout = new RowLayout();
		controlButtons.setLayout(controlButtonsLayout);
		controlButtons.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		
		addControl = new Button(controlButtons, SWT.NONE);
		addControl.setImage(Utility.getImage(ISharedImages.IMG_OBJ_ADD));
		
		removeControl = new Button(controlButtons, SWT.NONE);
		removeControl.setImage(Utility.getImage(ISharedImages.IMG_ELCL_REMOVE));
		
		Composite right = new Composite(parent, 0);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout rightLayout = new GridLayout(2, false);
		right.setLayout(rightLayout);
		
		Label nameLabel = new Label(right, SWT.NONE);
		nameLabel.setText("Name:");
		
		name = new Text(right, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		Label labelLabel = new Label(right, SWT.NONE);
		labelLabel.setText("Label:");
		
		label = new Text(right, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		Label portsLabel = new Label(right, SWT.NONE);
		portsLabel.setText("Ports:");
		
		ports = new List(right, SWT.SINGLE | SWT.BORDER);
		GridData portsLayout = new GridData(SWT.FILL, SWT.NONE, true, false);
		portsLayout.heightHint = 90;
		ports.setLayoutData(portsLayout);
		
		new Label(right, SWT.NONE); /* padding */
		
		Composite portButtons = new Composite(right, SWT.NONE);
		RowLayout portButtonsLayout = new RowLayout();
		portButtons.setLayout(portButtonsLayout);
		portButtons.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		
		addPort = new Button(portButtons, SWT.NONE);
		addPort.setImage(Utility.getImage(ISharedImages.IMG_OBJ_ADD));
		
		removePort = new Button(portButtons, SWT.NONE);
		removePort.setImage(Utility.getImage(ISharedImages.IMG_ELCL_REMOVE));
		
		Label appearanceLabel = new Label(right, SWT.NONE);
		GridData appearanceLabelLayoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		appearanceLabel.setLayoutData(appearanceLabelLayoutData);
		appearanceLabel.setText("Appearance:");
		
		Composite appearance = new SignatureEditorPolygonCanvas(right, SWT.NONE);
		GridData appearanceLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		appearanceLayoutData.widthHint = 100;
		appearanceLayoutData.heightHint = 100;
		appearance.setLayoutData(appearanceLayoutData);
		appearance.setBackground(ColorConstants.listBackground);
		
		new Label(right, SWT.NONE); /* padding */
		
		FontData smif = appearanceLabel.getFont().getFontData()[0];
		smif.setStyle(SWT.ITALIC);
		smif.setHeight(8);
		Font smiff = new Font(null, smif);
		
		Label appearanceDescription = new Label(right, SWT.CENTER | SWT.WRAP);
		GridData appearanceDescriptionData = new GridData();
		appearanceDescriptionData.verticalAlignment = SWT.TOP;
		appearanceDescriptionData.horizontalAlignment = SWT.FILL;
		appearanceDescriptionData.widthHint = 0;
		appearanceDescription.setText("Double-click a line segment to add a new point. Double-click a point to delete it. Move elements by clicking and dragging.");
		appearanceDescription.setLayoutData(appearanceDescriptionData);
		appearanceDescription.setFont(smiff);
		
		new Label(right, SWT.NONE);
		
		resizable = new Button(right, SWT.CHECK);
		resizable.setText("Resizable");
		
		new Label(right, SWT.NONE);
		
		portsMovable = new Button(right, SWT.CHECK);
		portsMovable.setText("Ports movable");
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void commandStackChanged(EventObject event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
