package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import org.bigraph.model.Control;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.Store;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.names.policies.BooleanNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.LongNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import dk.itu.big_red.editors.AbstractNonGEFEditor;
import dk.itu.big_red.editors.assistants.IFactory;
import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ControlUtilities;
import dk.itu.big_red.model.Ellipse;
import org.bigraph.extensions.param.ParameterUtilities;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.StockButton;
import dk.itu.big_red.utilities.ui.UI;

public class SignatureEditor extends AbstractNonGEFEditor
implements PropertyChangeListener {
	public static final String ID = "dk.itu.big_red.SignatureEditor";
	
	public SignatureEditor() {
	}
	
	@Override
	public void doActualSave(IFile f, OutputStream os)
			throws SaveFailedException {
    	SignatureXMLSaver r = new SignatureXMLSaver().setModel(getModel());
		r.setFile(new EclipseFileWrapper(f)).
    		setOutputStream(os).exportObject();
		setSavePoint();
	}

	private Signature model = null;
	
	@Override
	protected Signature getModel() {
		return model;
	}
	
	private org.bigraph.model.Control currentControl;
	
	private TreeViewer controls;
	private Button embedSignature, addControl, remove;
	
	private Text name, label;
	private SignatureEditorPolygonCanvas appearance;
	private Button ovalMode, polygonMode;
	private Button activeKind, atomicKind, passiveKind;
	
	private Label
		appearanceDescription, kindLabel, labelLabel,
		outlineLabel, fillLabel, nameLabel, appearanceLabel;
	private ColorSelector outline, fill;
	
	protected void setControl(Control c) {
		if (currentControl != null)
			currentControl.removePropertyChangeListener(this);
		currentControl = c;
		if (setEnablement(c != null)) {
			currentControl.addPropertyChangeListener(this);
			controlToFields();
		}
	}
	
	private boolean uiUpdateInProgress = false;
	
	private Control getSelectedControl() {
		Object o = ((IStructuredSelection)controls.getSelection()).
				getFirstElement();
		return (o instanceof Control ? (Control)o : null);
	}
	
	protected void controlToFields() {
		uiUpdateInProgress = true;
		
		try {
			Object shape = ControlUtilities.getShape(currentControl);
			boolean polygon = (shape instanceof PointList);
			
			label.setText(ControlUtilities.getLabel(currentControl));
			name.setText(currentControl.getName());
			
			appearance.setModel(currentControl);
			
			if (getSelectedControl() != currentControl)
				controls.setSelection(
						new StructuredSelection(currentControl), true);
			
			ovalMode.setSelection(!polygon);
			polygonMode.setSelection(polygon);
			
			outline.setColorValue(
					ColourUtilities.getOutline(currentControl).getRGB());
			fill.setColorValue(
					ColourUtilities.getFill(currentControl).getRGB());
			
			activeKind.setSelection(currentControl.getKind() == Kind.ACTIVE);
			atomicKind.setSelection(currentControl.getKind() == Kind.ATOMIC);
			passiveKind.setSelection(currentControl.getKind() == Kind.PASSIVE);
			
			/* Don't allow controls from nested signatures to be edited */
			if (setEnablement(
					currentControl.getSignature().equals(getModel()))) {
				name.setFocus();
				name.selectAll();
			}
			
		} finally {
			uiUpdateInProgress = false;
		}
	}
	
	private void lockedTextUpdate(Text t, String newValue) {
		boolean oldUI = uiUpdateInProgress;
		uiUpdateInProgress = true;
		try {
			t.setText(newValue);
		} finally {
			uiUpdateInProgress = oldUI;
		}
	}
	
	private static class CNPF implements IFactory<INamePolicy> {
		public Class<? extends INamePolicy> klass;
		public CNPF(Class<? extends INamePolicy> klass) {
			this.klass = klass;
		}
		
		@Override
		public String getName() {
			return klass.getSimpleName();
		}
		
		@Override
		public INamePolicy newInstance() {
			try {
				return klass.newInstance();
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	private static ArrayList<CNPF> getNamePolicies() {
		ArrayList<CNPF> r = new ArrayList<CNPF>();
		r.add(new CNPF(LongNamePolicy.class));
		r.add(new CNPF(StringNamePolicy.class));
		r.add(new CNPF(BooleanNamePolicy.class));
		return r;
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
	
	private static final PointList POINTS_QUAD = new PointList(new int[] {
		0, 0,
		0, 40,
		-40, 40,
		-40, 0
	});
	
	private static final IChangeDescriptor changeControlName(
			Control c, String s) {
		if (c != null && s != null) {
			ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
			cdg.add(new NamedModelObject.ChangeNameDescriptor(
					c.getIdentifier(), s));
			cdg.add(new ControlUtilities.ChangeLabelDescriptor(
					c.getIdentifier().getRenamed(s),
					ControlUtilities.getLabel(c),
					ControlUtilities.labelFor(s)));
			return cdg;
		} else return null;
	}
	
	@Override
	protected Resolver getResolver() {
		return getModel();
	}
	
	private static final IChangeDescriptor changeDeleteControl(Control c) {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		Control.Identifier cid = c.getIdentifier();
		
		for (PortSpec i : c.getPorts()) {
			PortSpec.Identifier pid = i.getIdentifier();
			cdg.add(new Store.ToStoreDescriptor(
					pid, Store.getInstance().createID()));
			cdg.add(new Control.ChangeRemovePortSpecDescriptor(pid));
		}
		cdg.add(new Store.ToStoreDescriptor(
				cid, Store.getInstance().createID()));
		cdg.add(new Signature.ChangeRemoveControlDescriptor(
				new Signature.Identifier(), c.getIdentifier()));
		
		return cdg;
	}
	
	@Override
	public void createEditorControl(Composite parent) {
		Composite self = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		self.setLayout(gl);
		
		Composite left = new Composite(self, 0);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout leftLayout = new GridLayout(1, false);
		left.setLayout(leftLayout);
		
		controls = new TreeViewer(left);
		controls.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				return (element instanceof Signature ? 1 :
						element instanceof Control ? 2 : 0);
			}
			
			@Override
			public boolean isSorterProperty(Object element, String property) {
				return (element instanceof Control &&
						Control.PROPERTY_NAME.equals(property));
			}
		});
		controls.setContentProvider(
				new SignatureControlsContentProvider(controls));
		controls.setLabelProvider(new SignatureControlsLabelProvider());
		GridData controlsLayoutData =
			new GridData(SWT.FILL, SWT.FILL, true, true);
		controlsLayoutData.widthHint = 100;
		controls.getTree().setLayoutData(controlsLayoutData);
		controls.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setControl(getSelectedControl());
			}
		});
		final Menu menu = new Menu(controls.getTree());
		menu.addMenuListener(new MenuListener() {
			private Control currentControl;
			private INamePolicy currentPolicy;
			
			private void createPolicyMenuItem(Menu parent, final CNPF p) {
				final MenuItem i = new MenuItem(parent, SWT.RADIO);
				i.setText(p.getName());
				i.setSelection(p.klass.isInstance(currentPolicy));
				i.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (i.getSelection())
							doChange(new ParameterUtilities.ChangeParameterPolicyDescriptor(
									currentControl.getIdentifier(),
									ParameterUtilities.getParameterPolicy(currentControl),
									p.newInstance()));
					}
				});
			}
			
			@Override
			public void menuShown(MenuEvent e) {
				for (MenuItem i : menu.getItems())
					i.dispose();
				
				currentControl = getSelectedControl();
				if (currentControl == null) {
					menu.setVisible(false);
					return;
				}
				
				boolean nested =
						(currentControl.getSignature().getParent() != null);
				
				currentPolicy =
					ParameterUtilities.getParameterPolicy(currentControl);
				
				MenuItem paramItem = new MenuItem(menu, SWT.CASCADE);
				paramItem.setText("&Parameter");
				
				Menu paramMenu = new Menu(paramItem);
				final MenuItem n = new MenuItem(paramMenu, SWT.RADIO);
				n.setText("(none)");
				n.setSelection(currentPolicy == null);
				n.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (n.getSelection())
							doChange(new ParameterUtilities.ChangeParameterPolicyDescriptor(
									currentControl.getIdentifier(),
									ParameterUtilities.getParameterPolicy(currentControl),
									null));
					}
				});
				if (!nested) {
					for (CNPF i : getNamePolicies())
						createPolicyMenuItem(paramMenu, i);
				} else paramItem.setEnabled(false);
				paramItem.setMenu(paramMenu);
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
			}
		});
		controls.getTree().setMenu(menu);
		
		Composite controlButtons = new Composite(left, SWT.NONE);
		RowLayout controlButtonsLayout = new RowLayout();
		controlButtons.setLayout(controlButtonsLayout);
		controlButtons.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		
		embedSignature = StockButton.OPEN.create(controlButtons, SWT.NONE);
		embedSignature.setText("&Import...");
		embedSignature.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceTreeSelectionDialog rtsd =
						new ResourceTreeSelectionDialog(
								getSite().getShell(),
								getFile().getProject(),
								Mode.FILE, Signature.CONTENT_TYPE);
				if (rtsd.open() == Dialog.OK) {
					try {
						IFile f = (IFile)rtsd.getFirstResult();
						doChange(new Signature.ChangeAddSignatureDescriptor(
								new Signature.Identifier(), -1,
								(Signature)new EclipseFileWrapper(f).load()));
					} catch (LoadFailedException ex) {
						return;
					}
				}
			}
		});
		
		addControl = StockButton.ADD.create(controlButtons);
		addControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control.Identifier cid = new Control.Identifier(
						getModel().getNamespace().getNextName());
				doChange(new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(), cid));
				controls.setSelection(
						new StructuredSelection(
								cid.lookup(null, getModel())), true);
			}
		});
		
		remove = StockButton.REMOVE.create(controlButtons);
		remove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Iterator<?> it =
					((IStructuredSelection)controls.getSelection()).iterator();
				ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
				PropertyScratchpad context = new PropertyScratchpad();
				while (it.hasNext()) {
					Object i = it.next();
					IChangeDescriptor ch = null;
					if (i instanceof Control) {
						Control c = (Control)i;
						if (c.getSignature().equals(getModel()))
							ch = changeDeleteControl(c);
					} else if (i instanceof Signature) {
						Signature s = (Signature)i;
						if (s.getParent().equals(getModel()))
							ch = new Signature.ChangeRemoveSignatureDescriptor(
									new Signature.Identifier(),
									getModel().getSignatures(context).indexOf(s),
									s);
					}
					if (ch != null) {
						ch.simulate(context, getResolver());
						cg.add(ch);
					}
				}
				
				if (cg.size() > 0 && doChange(cg)) {
					controls.setSelection(StructuredSelection.EMPTY);
					setControl(null);
				}
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
				String n = currentControl.getName();
				if (!n.equals(name.getText()))
					if (!doChange(changeControlName(
							currentControl, name.getText())))
						lockedTextUpdate(name, n);
			}
		};
		
		(nameLabel = new Label(right, SWT.NONE)).setText("Name:");
		name = new Text(right, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		name.addSelectionListener(nameListener);
		name.addFocusListener(nameListener);
		
		TextListener labelListener = new TextListener() {
			@Override
			void go() {
				String l = ControlUtilities.getLabel(currentControl);
				String n = label.getText();
				if (!l.equals(n))
					if (!doChange(new ControlUtilities.ChangeLabelDescriptor(
							null, currentControl, n)))
						lockedTextUpdate(label, l);
			}
		};
		
		(labelLabel = new Label(right, SWT.NONE)).setText("Label:");
		label = new Text(right, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		label.addSelectionListener(labelListener);
		label.addFocusListener(labelListener);
		
		(kindLabel = new Label(right, SWT.NONE)).setText("Kind:");
		
		Composite kindGroup = new Composite(right, SWT.NONE);
		kindGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		kindGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		(atomicKind = new Button(kindGroup, SWT.RADIO)).setText("Atomic");
		atomicKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI() &&
						!currentControl.getKind().equals(Kind.ATOMIC))
					doChange(new Control.ChangeKindDescriptor(
							null, currentControl, Kind.ATOMIC));
			}
		});
		
		(activeKind = new Button(kindGroup, SWT.RADIO)).setText("Active");
		activeKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI() &&
						!currentControl.getKind().equals(Kind.ACTIVE))
					doChange(new Control.ChangeKindDescriptor(
							null, currentControl, Kind.ACTIVE));
			}
		});
		
		(passiveKind = new Button(kindGroup, SWT.RADIO)).setText("Passive");
		passiveKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI() &&
						!currentControl.getKind().equals(Kind.PASSIVE))
					doChange(new Control.ChangeKindDescriptor(
							null, currentControl, Kind.PASSIVE));
			}
		});
		
		(appearanceLabel = new Label(right, SWT.NONE)).setText("Appearance:");
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
		
		(ovalMode = new Button(firstLine, SWT.RADIO)).setText("Oval");
		ovalMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					doChange(new ControlUtilities.ChangeShapeDescriptor(
							(PropertyScratchpad)null, currentControl,
							Ellipse.SINGLETON));
			}
		});
		
		(polygonMode = new Button(firstLine, SWT.RADIO)).setText("Polygon");
		polygonMode.setSelection(true);
		polygonMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					doChange(new ControlUtilities.ChangeShapeDescriptor(
							(PropertyScratchpad)null,
							currentControl, POINTS_QUAD));
			}
		});
		
		appearance = new SignatureEditorPolygonCanvas(this,
				appearanceGroup, SWT.BORDER);
		GridData appearanceLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		appearanceLayoutData.widthHint = 100;
		appearanceLayoutData.heightHint = 100;
		appearance.setLayoutData(appearanceLayoutData);
		appearance.setBackground(ColorConstants.listBackground);
		
		if (smiff == null)
			smiff = UI.tweakFont(appearanceLabel.getFont(), 8, SWT.ITALIC);
		
		appearanceDescription = new Label(appearanceGroup, SWT.CENTER | SWT.WRAP);
		appearanceDescription.setText(
				"Click to add a new point. Double-click a point to delete " +
				"it. Move elements by clicking and dragging. Right-click for" +
				"more options.");
		GridData appearanceDescriptionData = new GridData();
		appearanceDescriptionData.verticalAlignment = SWT.TOP;
		appearanceDescriptionData.horizontalAlignment = SWT.FILL;
		appearanceDescriptionData.widthHint = 0;
		appearanceDescription.setLayoutData(appearanceDescriptionData);
		appearanceDescription.setFont(smiff);
		
		(outlineLabel = new Label(right, SWT.NONE)).setText("Outline:");
		outline = new ColorSelector(right);
		outline.getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		outline.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (!shouldPropagateUI())
					return;
				Colour newColour = new Colour(outline.getColorValue());
				if (!ColourUtilities.getOutline(currentControl).equals(newColour))
					doChange(new ColourUtilities.ChangeOutlineDescriptor(
							currentControl.getIdentifier(),
							ColourUtilities.getOutlineRaw(currentControl),
							newColour));
			}
		});
		
		(fillLabel = new Label(right, SWT.NONE)).setText("Fill:");
		fill = new ColorSelector(right);
		fill.getButton().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fill.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (!shouldPropagateUI())
					return;
				Colour newColour = new Colour(fill.getColorValue());
				if (!ColourUtilities.getFill(currentControl).equals(newColour))
					doChange(new ColourUtilities.ChangeFillDescriptor(
							currentControl.getIdentifier(),
							ColourUtilities.getFillRaw(currentControl),
							newColour));
			}
		});
		
		setEnablement(false);
	}

	private boolean setEnablement(boolean enabled) {
		return UI.setEnabled(enabled,
			name, label, appearance, appearanceDescription,
			atomicKind, activeKind, passiveKind, outline.getButton(),
			outlineLabel, fill.getButton(), ovalMode, fillLabel, polygonMode,
			kindLabel, nameLabel, appearanceLabel, labelLabel);
	}
	
	@Override
	protected void loadModel() throws LoadFailedException {
		model = (Signature)loadInput();
	}
	
	@Override
	protected void updateEditorControl() {
		if (getError() != null)
			return;
		
		clearUndo();
		if (currentControl != null)
			currentControl.removePropertyChangeListener(this);
		
		controls.setInput(getModel());
		setControl(null);
	}

	@Override
	public void dispose() {
		if (currentControl != null)
			currentControl.removePropertyChangeListener(this);
		model = null;
		
		appearance.dispose();
		appearance = null;
		
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		if (controls != null) {
			org.eclipse.swt.widgets.Control c = controls.getControl();
			if (!c.isDisposed())
				c.setFocus();
		}
	}

	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		if (evt.getSource().equals(currentControl)) {
			if (uiUpdateInProgress)
				return;
			uiUpdateInProgress = true;
			try {
				if (ControlUtilities.LABEL.equals(propertyName)) {
					label.setText((String)newValue);
				} else if (Control.PROPERTY_NAME.equals(propertyName)) {
					name.setText((String)newValue);
				} else if (ControlUtilities.SHAPE.equals(propertyName)) {
					ovalMode.setSelection(newValue instanceof Ellipse);
					polygonMode.setSelection(newValue instanceof PointList);
				} else if (ColourUtilities.FILL.equals(propertyName)) {
					Colour c = (Colour)newValue;
					if (c == null)
						c = ColourUtilities.getDefaultFill(currentControl);
					fill.setColorValue(c.getRGB());
				} else if (ColourUtilities.OUTLINE.equals(propertyName)) {
					Colour c = (Colour)newValue;
					if (c == null)
						c = ColourUtilities.getDefaultOutline(currentControl);
					outline.setColorValue(c.getRGB());
				} else if (Control.PROPERTY_KIND.equals(propertyName)) {
					activeKind.setSelection(Kind.ACTIVE.equals(newValue));
					atomicKind.setSelection(Kind.ATOMIC.equals(newValue));
					passiveKind.setSelection(Kind.PASSIVE.equals(newValue));
				}
			} finally {
				uiUpdateInProgress = false;
			}
		}
	}

	@Override
	protected void createActions() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean doChange(IChangeDescriptor c) {
		return super.doChange(c);
	}
}
