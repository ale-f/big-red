package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartConstants;
import dk.itu.big_red.editors.AbstractEditor;
import dk.itu.big_red.editors.signature.SignatureEditorPolygonCanvas.SEPCListener;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.savers.SignatureXMLSaver;
import dk.itu.big_red.utilities.Lists;
import dk.itu.big_red.utilities.ui.UI;

public class SignatureEditor extends AbstractEditor
implements PropertyChangeListener {
	public static final String ID = "dk.itu.big_red.SignatureEditor";
	
	public SignatureEditor() {
	}
	
	@Override
	public void doActualSave(OutputStream os) throws SaveFailedException {
    	new SignatureXMLSaver().setModel(getModel()).setOutputStream(os).
    		exportObject();
		setDirty(false);
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

	private Signature model = null;
	
	@Override
	protected Signature getModel() {
		return model;
	}
	
	private dk.itu.big_red.model.Control currentControl;
	
	private ListViewer controls;
	private Button addControl, removeControl;
	
	private Text name, label;
	private SignatureEditorPolygonCanvas appearance;
	private Button ovalMode, polygonMode, resizable;
	private Button activeKind, atomicKind, passiveKind;
	
	private Label
		appearanceDescription, kindLabel, labelLabel,
		outlineLabel, fillLabel, nameLabel, appearanceLabel;
	private ColorSelector outline, fill;
	
	protected void setControl(Control c) {
		if (currentControl != null)
			currentControl.removePropertyChangeListener(this);
		currentControl = c;
		c.addPropertyChangeListener(this);
		
		controlToFields();

		name.setFocus();
	}
	
	private boolean uiUpdateInProgress = false;
	
	private Control getSelectedControl() {
		return
			(Control)
				((IStructuredSelection)controls.getSelection()).
					getFirstElement();
	}
	
	protected void controlToFields() {
		uiUpdateInProgress = true;
		
		boolean polygon = (currentControl.getShape() == Shape.POLYGON);
		
		label.setText(currentControl.getLabel());
		name.setText(currentControl.getName());
		appearance.setMode(polygon ? Shape.POLYGON : Shape.OVAL);
		if (polygon)
			appearance.setPoints(currentControl.getPoints());
		appearance.setPorts(currentControl.getPorts());
		resizable.setSelection(currentControl.isResizable());
		if (getSelectedControl() != currentControl)
			controls.setSelection(new StructuredSelection(currentControl), true);
		
		ovalMode.setSelection(!polygon);
		polygonMode.setSelection(polygon);
		
		outline.setColorValue(currentControl.getOutlineColour().getRGB());
		fill.setColorValue(currentControl.getFillColour().getRGB());
		
		activeKind.setSelection(currentControl.getKind() == Kind.ACTIVE);
		atomicKind.setSelection(currentControl.getKind() == Kind.ATOMIC);
		passiveKind.setSelection(currentControl.getKind() == Kind.PASSIVE);
		
		uiUpdateInProgress = false;
	}
	
	/**
	 * Indicates whether or not changes made to the UI should be propagated
	 * to the current {@link Control}.
	 * @return <code>true</code> if the {@link Control} is valid and is not
	 * itself currently changing the UI, or <code>false</code> otherwise
	 */
	private boolean shouldPropagateUI() {
		return (!uiUpdateInProgress && currentControl != null);
	}
	
	private static Font smiff;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite self =
			setComposite(UI.chain(new Composite(setParent(parent), SWT.NONE)).
			layoutData(new GridData(SWT.FILL, SWT.FILL, true, true)).done());
		
		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		self.setLayout(gl);
		
		Composite left = new Composite(self, 0);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout leftLayout = new GridLayout(1, false);
		left.setLayout(leftLayout);
		
		controls = new ListViewer(left);
		UI.setProviders(controls, new SignatureControlsContentProvider(controls),
			new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((Control)element).getName();
				}
		});
		GridData controlsLayoutData =
			new GridData(SWT.FILL, SWT.FILL, true, true);
		controlsLayoutData.widthHint = 100;
		controls.getList().setLayoutData(controlsLayoutData);
		controls.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Control c = getSelectedControl();
				if (c != null) {
					setControl(c);
					setEnablement(true);
				} else setEnablement(false);
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
				Control c = new Control();
				getModel().addControl(c);
				controls.setSelection(new StructuredSelection(c), true);
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
				Iterator<?> it =
					((IStructuredSelection)controls.getSelection()).iterator();
				while (it.hasNext())
					getModel().removeControl((Control)it.next());
				controls.setSelection(StructuredSelection.EMPTY);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Composite right = new Composite(self, 0);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout rightLayout = new GridLayout(2, false);
		right.setLayout(rightLayout);
		
		abstract class TextListener implements SelectionListener, FocusListener {
			abstract void go();
			
			@Override
			public void focusGained(FocusEvent e) {
				/* nothing */
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (shouldPropagateUI())
					go();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				/* nothing */
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					go();
			}
			
		}
		
		TextListener nameListener = new TextListener() {
			@Override
			void go() {
				if (!currentControl.getName().equals(name.getText()))
					currentControl.setName(name.getText());
			}
		};
		
		nameLabel = UI.chain(new Label(right, SWT.NONE)).text("Name:").done();
		name = new Text(right, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		name.addSelectionListener(nameListener);
		name.addFocusListener(nameListener);
		
		TextListener labelListener = new TextListener() {
			@Override
			void go() {
				if (!currentControl.getLabel().equals(label.getText()))
					currentControl.setLabel(label.getText());
			}
		};
		
		labelLabel = UI.chain(new Label(right, SWT.NONE)).text("Label:").done();
		label = new Text(right, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		label.addSelectionListener(labelListener);
		label.addFocusListener(labelListener);
		
		kindLabel = UI.chain(new Label(right, SWT.NONE)).text("Kind:").done();
		
		Composite kindGroup = new Composite(right, SWT.NONE);
		kindGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		kindGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		atomicKind = UI.chain(new Button(kindGroup, SWT.RADIO)).text("Atomic").done();
		atomicKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					currentControl.setKind(Kind.ATOMIC);
			}
		});
		
		activeKind = UI.chain(new Button(kindGroup, SWT.RADIO)).text("Active").done();
		activeKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					currentControl.setKind(Kind.ACTIVE);
			}
		});
		
		passiveKind = UI.chain(new Button(kindGroup, SWT.RADIO)).text("Passive").done();
		passiveKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					currentControl.setKind(Kind.PASSIVE);
			}
		});
		
		appearanceLabel = UI.chain(new Label(right, SWT.NONE)).text("Appearance:").done();
		GridData appearanceLabelLayoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		appearanceLabel.setLayoutData(appearanceLabelLayoutData);
		
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
		
		ovalMode = UI.chain(new Button(firstLine, SWT.RADIO)).text("Oval").done();
		ovalMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					currentControl.setShape(Shape.OVAL);
			}
		});
		
		polygonMode = UI.chain(new Button(firstLine, SWT.RADIO)).text("Polygon").done();
		polygonMode.setSelection(true);
		polygonMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					currentControl.setShape(Shape.POLYGON);
			}
		});
		
		resizable = UI.chain(new Button(firstLine, SWT.CHECK)).text("Resizable?").done();
		resizable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					currentControl.setResizable(resizable.getSelection());
			}
		});
		
		appearance = new SignatureEditorPolygonCanvas(appearanceGroup, SWT.BORDER);
		GridData appearanceLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		appearanceLayoutData.widthHint = 100;
		appearanceLayoutData.heightHint = 100;
		appearance.setLayoutData(appearanceLayoutData);
		appearance.setBackground(ColorConstants.listBackground);
		appearance.addListener(new SEPCListener() {
			
			@Override
			public void portChange() {
				if (!shouldPropagateUI())
					return;
				ArrayList<PortSpec> toCopy = Lists.copy(appearance.getPorts());
				for (PortSpec p : Lists.copy(currentControl.getPorts()))
					currentControl.removePort(p.getName());
				for (PortSpec p : toCopy)
					currentControl.addPort(new PortSpec(p));
			}
			
			@Override
			public void pointChange() {
				if (!shouldPropagateUI())
					return;
				currentControl.setPoints(appearance.getPoints().getCopy());
			}
		});
		
		if (smiff == null)
			smiff = UI.tweakFont(appearanceLabel.getFont(), 8, SWT.ITALIC);
		
		appearanceDescription = UI.chain(new Label(appearanceGroup, SWT.CENTER | SWT.WRAP)).text("Click to add a new point. Double-click a point to delete it. " +
		"Move elements by clicking and dragging. " +
		"Right-click for more options.").done();
		GridData appearanceDescriptionData = new GridData();
		appearanceDescriptionData.verticalAlignment = SWT.TOP;
		appearanceDescriptionData.horizontalAlignment = SWT.FILL;
		appearanceDescriptionData.widthHint = 0;
		appearanceDescription.setLayoutData(appearanceDescriptionData);
		appearanceDescription.setFont(smiff);
		
		outlineLabel = UI.chain(new Label(right, SWT.NONE)).text("Outline:").done();
		outline = new ColorSelector(right);
		outline.getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		outline.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (!shouldPropagateUI())
					return;
				try {
					getModel().tryApplyChange(
							currentControl.changeOutlineColour(
									new Colour(outline.getColorValue())));
				} catch (ChangeRejectedException cre) {
					cre.printStackTrace();
				}
			}
		});
		
		fillLabel = UI.chain(new Label(right, SWT.NONE)).text("Fill:").done();
		fill = new ColorSelector(right);
		fill.getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fill.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (!shouldPropagateUI())
					return;
				try {
					getModel().tryApplyChange(
							currentControl.changeFillColour(
									new Colour(fill.getColorValue())));
				} catch (ChangeRejectedException cre) {
					cre.printStackTrace();
				}
			}
		});
		
		setEnablement(false);
		initialise();
	}

	private void setEnablement(boolean enabled) {
		UI.setEnabled(enabled,
			name, label, appearance, appearanceDescription, resizable,
			atomicKind, activeKind, passiveKind, outline.getButton(),
			outlineLabel, fill.getButton(), ovalMode, fillLabel, polygonMode,
			kindLabel, nameLabel, appearanceLabel, labelLabel);
	}
	
	@Override
	protected void initialiseActual() throws Throwable {
		model = (Signature)loadInput();
		
		if (getModel() == null) {
			replaceWithError(new Exception("Model is null"));
			return;
		}
		
		getModel().addPropertyChangeListener(this);
		controls.setInput(getModel());
	}

	@Override
	public void setFocus() {
		if (getComposite() == null)
			return;
		controls.getControl().setFocus();
	}

	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (evt.getSource().equals(getModel())) {
			if (evt.getPropertyName().equals(Signature.PROPERTY_CONTROL))
				setDirty(true);
		} else if (evt.getSource().equals(currentControl)) {
			if (uiUpdateInProgress)
				return;
			uiUpdateInProgress = true;
			try {
				String propertyName = evt.getPropertyName();
				Object newValue = evt.getNewValue();
				if (propertyName.equals(Control.PROPERTY_LABEL)) {
					label.setText((String)newValue);
				} else if (propertyName.equals(Control.PROPERTY_NAME)) {
					name.setText((String)newValue);
					controls.refresh(currentControl);
				} else if (propertyName.equals(Control.PROPERTY_SHAPE)) {
					appearance.setMode((Shape)newValue);
					ovalMode.setSelection(Shape.OVAL.equals(newValue));
					polygonMode.setSelection(Shape.POLYGON.equals(newValue));
				} else if (propertyName.equals(Control.PROPERTY_POINTS)) {
					if (appearance.getMode() == Shape.POLYGON)
						appearance.setPoints((PointList)newValue);
				} else if (propertyName.equals(Control.PROPERTY_PORT)) {
					appearance.setPorts(currentControl.getPorts());
				} else if (propertyName.equals(Control.PROPERTY_RESIZABLE)) {
					resizable.setSelection((Boolean)newValue);
				} else if (propertyName.equals(Colourable.PROPERTY_FILL)) {
					fill.setColorValue(((Colour)newValue).getRGB());
				} else if (propertyName.equals(Colourable.PROPERTY_OUTLINE)) {
					outline.setColorValue(((Colour)newValue).getRGB());
				} else if (propertyName.equals(Control.PROPERTY_KIND)) {
					activeKind.setSelection(Kind.ACTIVE.equals(newValue));
					atomicKind.setSelection(Kind.ATOMIC.equals(newValue));
					passiveKind.setSelection(Kind.PASSIVE.equals(newValue));
				}
			} finally {
				uiUpdateInProgress = false;
			}
		}
		setDirty(true);
	}
	
	@Override
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
	}

	@Override
	protected void createActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public void undo() {
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public void redo() {
	}
}
