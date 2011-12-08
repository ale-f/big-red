package dk.itu.big_red.editors.signature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.EventObject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.swt.widgets.Combo;
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

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.import_export.SignatureXMLExport;
import dk.itu.big_red.model.import_export.SignatureXMLImport;
import dk.itu.big_red.util.Colour;
import dk.itu.big_red.util.UI;

public class SignatureEditor extends EditorPart implements CommandStackListener, ISelectionListener {
	public static final String ID = "dk.itu.big_red.SignatureEditor";
	
	private static final String[] kinds = { "active", "atomic", "passive" };
	
	public SignatureEditor() {
		 /*
		  * Don't fire any modifications until the first control has been
		  * loaded.
		  */
		fireModify = false;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
        	SignatureXMLExport ex = new SignatureXMLExport();
        	
        	ex.setModel(model);
        	ex.setOutputStream(os);
        	ex.exportObject();
        	
    		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    		i.getFile().setContents(is, 0, null);
        	
    		setDirty(false);
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
        	ErrorDialog.openError(getSite().getShell(), null, "Unable to save the document.",
        			RedPlugin.getThrowableStatus(ex));
        }
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
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

	private Signature model = null;
	
	private dk.itu.big_red.model.Control currentControl;
	
	private Tree controls;
	private TreeItem currentControlItem;
	private Button addControl, removeControl;
	
	private Text name, label;
	private SignatureEditorPolygonCanvas appearance;
	private Button ovalMode, polygonMode, resizable;
	private Combo kind;
	
	private boolean fireModify = true;

	private Label
		appearanceDescription, kindLabel, labelLabel,
		outlineLabel, fillLabel, nameLabel, appearanceLabel;
	private ColorSelector outline, fill;
	
	protected void controlToFields() {
		fireModify = false;
		
		boolean polygon = (currentControl.getShape() == Shape.POLYGON);
		
		label.setText(currentControl.getLabel());
		name.setText(currentControl.getLongName());
		appearance.setMode(polygon ? Shape.POLYGON : Shape.OVAL);
		if (polygon)
			appearance.setPoints(currentControl.getPoints());
		appearance.setPorts(currentControl.getPorts());
		resizable.setSelection(currentControl.isResizable());
		currentControlItem.setText(currentControl.getLongName());
		
		ovalMode.setSelection(!polygon);
		polygonMode.setSelection(polygon);
		
		outline.setColorValue(currentControl.getOutlineColour().getRGB());
		fill.setColorValue(currentControl.getFillColour().getRGB());
		
		kind.setText(currentControl.getKind().toString());
		
		fireModify = true;
	}
	
	protected void fieldsToControl() {
		if (currentControl != null) {
			currentControl.setLabel(label.getText());
			currentControl.setLongName(name.getText());
			currentControl.clearPorts();
			for (PortSpec p : appearance.getPorts())
				currentControl.addPort(p);
			currentControl.setResizable(resizable.getSelection());
			if (polygonMode.getSelection()) {
				currentControl.setShape(Shape.POLYGON, appearance.getPoints().getCopy());
			} else currentControl.setShape(Shape.OVAL, null);
			
			ChangeGroup cg = new ChangeGroup();
			cg.add(currentControl.changeOutlineColour(
					new Colour(outline.getColorValue())));
			cg.add(currentControl.changeFillColour(
					new Colour(fill.getColorValue())));
			model.applyChange(cg);
			
			currentControl.setKind(
				kind.getText().equals("active") ? Kind.ACTIVE :
				kind.getText().equals("passive") ? Kind.PASSIVE : Kind.ATOMIC);
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		final class DirtListener implements ModifyListener, SelectionListener, PointListener, PortListener, IPropertyChangeListener {
			private boolean canvasActive = true;
			
			private void go() {
				if (fireModify) {
					fieldsToControl();
					
					canvasActive = false;
					boolean polygon = polygonMode.getSelection();
					if (polygon && appearance.getMode() != Shape.POLYGON) {
						appearance.setMode(Shape.POLYGON);
					} else if (!polygon && appearance.getMode() == Shape.POLYGON) {
						appearance.setMode(Shape.OVAL);
					}
					canvasActive = true;
					
					setDirty(true);
				}
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				go();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void modifyText(ModifyEvent e) {
				go();
			}

			@Override
			public void pointChange(PointEvent e) {
				if (canvasActive)
					go();
			}
			
			@Override
			public void portChange(PortEvent e) {
				if (canvasActive)
					go();
			}

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				/* only from colour selectors */
				go();
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
				currentControl = model.getControl(currentControlItem.getText());
				controlToFields();
				name.setFocus();
				setEnablement(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Composite controlButtons = new Composite(left, SWT.NONE);
		RowLayout controlButtonsLayout = new RowLayout();
		controlButtons.setLayout(controlButtonsLayout);
		controlButtons.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		
		addControl = new Button(controlButtons, SWT.NONE);
		addControl.setImage(UI.getImage(ISharedImages.IMG_OBJ_ADD));
		addControl.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentControlItem = new TreeItem(controls, SWT.NONE);
				currentControl = model.addControl(new dk.itu.big_red.model.Control());
				controlToFields();
				controls.select(currentControlItem);
				name.setFocus();
				setEnablement(true);
				
				setDirty(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		
		removeControl = new Button(controlButtons, SWT.NONE);
		removeControl.setImage(UI.getImage(ISharedImages.IMG_ELCL_REMOVE));
		removeControl.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.removeControl(currentControl);
				currentControlItem.dispose();
				controls.update();
				
				if (controls.getItemCount() > 0) {
					controls.select(controls.getItem(0));
					currentControlItem = controls.getItem(0);
					currentControl = model.getControl(currentControlItem.getText());
					controlToFields();
					name.setFocus();
				} else setEnablement(false);
				
				setDirty(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Composite right = new Composite(parent, 0);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout rightLayout = new GridLayout(2, false);
		right.setLayout(rightLayout);
		
		nameLabel = new Label(right, SWT.NONE);
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
		
		labelLabel = new Label(right, SWT.NONE);
		labelLabel.setText("Label:");
		
		label = new Text(right, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		label.addModifyListener(sharedDirtListener);
		
		kindLabel = new Label(right, SWT.NONE);
		kindLabel.setText("Kind:");
		
		kind = new Combo(right, SWT.DROP_DOWN | SWT.READ_ONLY);
		kind.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		kind.setItems(kinds);
		kind.addModifyListener(sharedDirtListener);
		
		appearanceLabel = new Label(right, SWT.NONE);
		GridData appearanceLabelLayoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		appearanceLabel.setLayoutData(appearanceLabelLayoutData);
		appearanceLabel.setText("Appearance:");
		
		Composite appearanceGroup = new Composite(right, SWT.NONE);
		GridData appearanceGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		appearanceGroup.setLayoutData(appearanceGroupLayoutData);
		GridLayout appearanceGroupLayout = new GridLayout(1, false);
		appearanceGroup.setLayout(appearanceGroupLayout);		
		
		/* XXX: the addition of this row leads to really weird oversizing of
		 * the polygon canvas! */
		Composite firstLine = new Composite(appearanceGroup, SWT.NONE);
		firstLine.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		firstLine.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		ovalMode = new Button(firstLine, SWT.RADIO);
		ovalMode.setText("Oval");
		ovalMode.addSelectionListener(sharedDirtListener);
		
		polygonMode = new Button(firstLine, SWT.RADIO);
		polygonMode.setText("Polygon");
		polygonMode.setSelection(true);
		polygonMode.addSelectionListener(sharedDirtListener);
		
		resizable = new Button(firstLine, SWT.CHECK);
		resizable.setText("Resizable?");
		resizable.addSelectionListener(sharedDirtListener);
		
		appearance = new SignatureEditorPolygonCanvas(appearanceGroup, SWT.BORDER);
		GridData appearanceLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		appearanceLayoutData.widthHint = 100;
		appearanceLayoutData.heightHint = 100;
		appearance.setLayoutData(appearanceLayoutData);
		appearance.setBackground(ColorConstants.listBackground);
		appearance.addPortListener(sharedDirtListener);
		appearance.addPointListener(sharedDirtListener);
		
		FontData smif = appearanceLabel.getFont().getFontData()[0];
		smif.setStyle(SWT.ITALIC);
		smif.setHeight(8);
		Font smiff = new Font(null, smif);
		
		appearanceDescription = new Label(appearanceGroup, SWT.CENTER | SWT.WRAP);
		GridData appearanceDescriptionData = new GridData();
		appearanceDescriptionData.verticalAlignment = SWT.TOP;
		appearanceDescriptionData.horizontalAlignment = SWT.FILL;
		appearanceDescriptionData.widthHint = 0;
		appearanceDescription.setText("Click to add a new point. Double-click a point to delete it. Move elements by clicking and dragging. Right-click for more options.");
		appearanceDescription.setLayoutData(appearanceDescriptionData);
		appearanceDescription.setFont(smiff);
		
		outlineLabel = new Label(right, SWT.NONE);
		outlineLabel.setText("Outline:");
		
		outline = new ColorSelector(right);
		outline.getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		outline.addListener(sharedDirtListener);
		
		fillLabel = new Label(right, SWT.NONE);
		fillLabel.setText("Fill:");
		
		fill = new ColorSelector(right);
		fill.getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fill.addListener(sharedDirtListener);
		
		setEnablement(false);
		initialiseSignatureEditor();
	}

	private void setEnablement(boolean enabled) {
		UI.setEnabled(enabled,
			name, label, appearance, appearanceDescription, resizable, kind,
			outline.getButton(), outlineLabel, fill.getButton(),
			ovalMode, fillLabel, polygonMode, kindLabel, nameLabel,
			appearanceLabel, labelLabel);
	}
	
	private void initialiseSignatureEditor() {
		IEditorInput input = getEditorInput();
		setPartName(input.getName());
		
		if (input instanceof FileEditorInput) {
			FileEditorInput fi = (FileEditorInput)input;
			try {
				model = SignatureXMLImport.importFile(fi.getFile());
			} catch (Exception e) {
				e.printStackTrace();
				model = new Signature();
			}
		}
		
		for (dk.itu.big_red.model.Control c : model.getControls())
			new TreeItem(controls, 0).setText(c.getLongName());
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
