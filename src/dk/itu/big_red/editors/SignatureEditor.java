package dk.itu.big_red.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.EventObject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.editors.assistants.SignatureEditorPolygonCanvas;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.ResourceWrapper;
import dk.itu.big_red.model.import_export.SignatureXMLExport;
import dk.itu.big_red.model.import_export.SignatureXMLImport;
import dk.itu.big_red.util.Utility;

public class SignatureEditor extends EditorPart implements CommandStackListener, ISelectionListener {
	public static final String ID = "dk.itu.big_red.SignatureEditor";
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
        	SignatureXMLExport ex = new SignatureXMLExport();
        	
        	ex.setModel(model.getModel());
        	ex.setOutputStream(os);
        	ex.exportModel();
        	
    		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    		i.getFile().setContents(is, 0, null);
        	
    		setDirty(false);
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
        }
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

	protected boolean dirty = false;
	
	protected void setDirty(boolean dirty) {
		if (this.dirty != dirty) {
			this.dirty = dirty;
			firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	private ResourceWrapper<Signature> model = new ResourceWrapper<Signature>();
	
	private dk.itu.big_red.model.Control currentControl;
	
	private Tree controls;
	private TreeItem currentControlItem;
	private Button addControl, removeControl;
	
	private Text name, label;
	private Tree ports;
	private Button addPort, removePort;
	private SignatureEditorPolygonCanvas appearance;
	private Button resizable, portsMovable;
	
	private boolean fireModify = true;
	
	protected void controlToFields() {
		fireModify = false;
		
		label.setText(currentControl.getLabel());
		name.setText(currentControl.getLongName());
		appearance.setPoints(currentControl.getPoints());
		resizable.setSelection(currentControl.isResizable());
		currentControlItem.setText(currentControl.getLongName());
		
		fireModify = true;
	}
	
	protected void fieldsToControl() {
		currentControl.setLabel(label.getText());
		currentControl.setLongName(name.getText());
		currentControl.setResizable(resizable.getSelection());
		currentControl.setShape(Shape.SHAPE_POLYGON, appearance.getPoints());
	}
	
	@Override
	public void createPartControl(Composite parent) {
		final class DirtListener implements ModifyListener, SelectionListener {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fireModify) {
					fieldsToControl();
					setDirty(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void modifyText(ModifyEvent e) {
				if (fireModify) {
					fieldsToControl();
					setDirty(true);
				}
			}
			
		}
		
		DirtListener sharedDirtListener = new DirtListener();
		
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		
		Composite left = new Composite(parent, 0);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout leftLayout = new GridLayout(1, false);
		left.setLayout(leftLayout);
		
		controls = new Tree(left, SWT.SINGLE | SWT.BORDER | SWT.VIRTUAL);
		GridData controlsLayoutData =
			new GridData(SWT.FILL, SWT.FILL, true, true);
		controlsLayoutData.widthHint = 100;
		controls.setLayoutData(controlsLayoutData);
		controls.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentControlItem = controls.getSelection()[0];
				currentControl = model.getModel().getControl(currentControlItem.getText());
				controlToFields();
				name.setFocus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Composite controlButtons = new Composite(left, SWT.NONE);
		RowLayout controlButtonsLayout = new RowLayout();
		controlButtons.setLayout(controlButtonsLayout);
		controlButtons.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		
		addControl = new Button(controlButtons, SWT.NONE);
		addControl.setImage(Utility.getImage(ISharedImages.IMG_OBJ_ADD));
		addControl.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentControlItem = new TreeItem(controls, SWT.NONE);
				currentControl = model.getModel().addControl(new dk.itu.big_red.model.Control());
				controlToFields();
				controls.select(currentControlItem);
				name.setFocus();
				
				setDirty(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		
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
		name.addModifyListener(sharedDirtListener);
		name.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String nT = name.getText();
				if (nT.length() > 0)
					label.setText(name.getText().substring(0, 1));
				currentControlItem.setText(name.getText());
			}
		});
		
		Label labelLabel = new Label(right, SWT.NONE);
		labelLabel.setText("Label:");
		
		label = new Text(right, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		label.addModifyListener(sharedDirtListener);
		
		Label portsLabel = new Label(right, SWT.NONE);
		portsLabel.setText("Ports:");
		
		ports = new Tree(right, SWT.SINGLE | SWT.BORDER | SWT.VIRTUAL);
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
		
		appearance = new SignatureEditorPolygonCanvas(right, SWT.BORDER);
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
		appearanceDescription.setText("Click to add a new point. Double-click a point to delete it. Move elements by clicking and dragging. Right-click for more options.");
		appearanceDescription.setLayoutData(appearanceDescriptionData);
		appearanceDescription.setFont(smiff);
		
		new Label(right, SWT.NONE);
		
		resizable = new Button(right, SWT.CHECK);
		resizable.setText("Resizable?");
		resizable.addSelectionListener(sharedDirtListener);
		
		new Label(right, SWT.NONE);
		
		portsMovable = new Button(right, SWT.CHECK);
		portsMovable.setText("Ports movable?");
		portsMovable.addSelectionListener(sharedDirtListener);
		
		initialiseSignatureEditor();
	}

	private void initialiseSignatureEditor() {
		IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput) {
			FileEditorInput fi = (FileEditorInput)input;
			model.setResource(fi.getFile());
			try {
				SignatureXMLImport im = new SignatureXMLImport();
				im.setInputStream(fi.getFile().getContents());
				
				model.setModel(im.importModel());
			} catch (Exception e) {
				e.printStackTrace();
				model.setModel(new Signature());
			}
		}
		
		for (dk.itu.big_red.model.Control c : model.getModel().getControls())
			new TreeItem(controls, 0).setText(c.getLongName());
		setPartName(input.getName());
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
