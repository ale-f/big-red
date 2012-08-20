package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.bigraph.model.Control;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.Kind;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.names.policies.BooleanNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.names.policies.LongNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.PointList;
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
import dk.itu.big_red.model.ParameterUtilities;
import dk.itu.big_red.model.load_save.SaverUtilities;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
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
    	SaverUtilities.installDecorators(r);
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
	private Button addControl, removeControl;
	
	private Text name, label;
	private SignatureEditorPolygonCanvas appearance;
	private Button ovalMode, polygonMode;
	private Button activeKind, atomicKind, passiveKind;
	
	private Label
		appearanceDescription, kindLabel, labelLabel,
		outlineLabel, fillLabel, nameLabel, appearanceLabel;
	private ColorSelector outline, fill;
	
	protected void setControl(Control c) {
		currentControl = c;
		if (setEnablement(c != null))
			controlToFields();
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
	
	private static final IChange changeControlName(Control c, String s) {
		if (c != null && s != null) {
			ChangeGroup cg = new ChangeGroup();
			cg.add(c.changeName(s));
			cg.add(ControlUtilities.changeLabel(c,
					ControlUtilities.labelFor(s)));
			return cg;
		} else return null;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite self =
			UI.chain(new Composite(setParent(parent), SWT.NONE)).
			layoutData(new GridData(SWT.FILL, SWT.FILL, true, true)).done();
		
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
			
			private void doMenuItem(Menu parent, final CNPF p) {
				final MenuItem i = new MenuItem(parent, SWT.RADIO);
				i.setText(p.getName());
				i.setSelection(p.klass.isInstance(currentPolicy));
				i.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (i.getSelection())
							doChange(
								ParameterUtilities.changeParameterPolicy(currentControl, p.newInstance()));
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
							doChange(
								ParameterUtilities.changeParameterPolicy(currentControl, null));
					}
				});
				for (CNPF i : getNamePolicies())
					doMenuItem(paramMenu, i);
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
		
		addControl = StockButton.ADD.create(controlButtons);
		addControl.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control c = new Control();
				doChange(getModel().changeAddControl(c));
				controls.setSelection(new StructuredSelection(c), true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		
		removeControl = StockButton.REMOVE.create(controlButtons);
		removeControl.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Iterator<?> it =
					((IStructuredSelection)controls.getSelection()).iterator();
				ChangeGroup cg = new ChangeGroup();
				while (it.hasNext())
					cg.add(((Control)it.next()).changeRemove());
				doChange(cg);
				
				controls.setSelection(StructuredSelection.EMPTY);
				setControl(null);
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
				String n = currentControl.getName();
				if (!n.equals(name.getText()))
					if (!doChange(changeControlName(
							currentControl, name.getText())))
						lockedTextUpdate(name, n);
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
				String l = ControlUtilities.getLabel(currentControl);
				if (!l.equals(label.getText()))
					if (!doChange(ControlUtilities.changeLabel(currentControl, label.getText())))
						lockedTextUpdate(label, l);
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
				if (shouldPropagateUI() &&
						!currentControl.getKind().equals(Kind.ATOMIC))
					doChange(currentControl.changeKind(Kind.ATOMIC));
			}
		});
		
		activeKind = UI.chain(new Button(kindGroup, SWT.RADIO)).text("Active").done();
		activeKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI() &&
						!currentControl.getKind().equals(Kind.ACTIVE))
					doChange(currentControl.changeKind(Kind.ACTIVE));
			}
		});
		
		passiveKind = UI.chain(new Button(kindGroup, SWT.RADIO)).text("Passive").done();
		passiveKind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI() &&
						!currentControl.getKind().equals(Kind.PASSIVE))
					doChange(currentControl.changeKind(Kind.PASSIVE));
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
					doChange(ControlUtilities.changeShape(
							currentControl, Ellipse.SINGLETON));
			}
		});
		
		polygonMode = UI.chain(new Button(firstLine, SWT.RADIO)).text("Polygon").done();
		polygonMode.setSelection(true);
		polygonMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (shouldPropagateUI())
					doChange(ControlUtilities.changeShape(
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
				Colour newColour = new Colour(outline.getColorValue());
				if (!ColourUtilities.getOutline(currentControl).equals(newColour))
					doChange(ColourUtilities.changeOutline(currentControl, newColour));
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
				Colour newColour = new Colour(fill.getColorValue());
				if (!ColourUtilities.getFill(currentControl).equals(newColour))
					doChange(ColourUtilities.changeFill(currentControl, newColour));
			}
		});
		
		setEnablement(false);
		initialise();
	}

	private boolean setEnablement(boolean enabled) {
		return UI.setEnabled(enabled,
			name, label, appearance, appearanceDescription,
			atomicKind, activeKind, passiveKind, outline.getButton(),
			outlineLabel, fill.getButton(), ovalMode, fillLabel, polygonMode,
			kindLabel, nameLabel, appearanceLabel, labelLabel);
	}
	
	@Override
	protected void initialiseActual() throws Throwable {
		clearUndo();
		
		model = (Signature)loadInput();
		
		getModel().addPropertyChangeListener(this);
		for (Control c : getModel().getControls())
			c.addPropertyChangeListener(this);
		controls.setInput(getModel());
	}

	@Override
	public void dispose() {
		for (Control c : getModel().getControls())
			c.removePropertyChangeListener(this);
		getModel().removePropertyChangeListener(this);
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
		if (evt.getSource() instanceof Control)
			controls.update(evt.getSource(),
					new String[] { evt.getPropertyName() });
		String propertyName = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		if (evt.getSource().equals(currentControl)) {
			if (uiUpdateInProgress)
				return;
			uiUpdateInProgress = true;
			try {
				if (propertyName.equals(ControlUtilities.LABEL)) {
					label.setText((String)newValue);
				} else if (propertyName.equals(Control.PROPERTY_NAME)) {
					name.setText((String)newValue);
				} else if (propertyName.equals(ControlUtilities.SHAPE)) {
					ovalMode.setSelection(newValue instanceof Ellipse);
					polygonMode.setSelection(newValue instanceof PointList);
				} else if (propertyName.equals(ColourUtilities.FILL)) {
					fill.setColorValue(((Colour)newValue).getRGB());
				} else if (propertyName.equals(ColourUtilities.OUTLINE)) {
					outline.setColorValue(((Colour)newValue).getRGB());
				} else if (propertyName.equals(Control.PROPERTY_KIND)) {
					activeKind.setSelection(Kind.ACTIVE.equals(newValue));
					atomicKind.setSelection(Kind.ATOMIC.equals(newValue));
					passiveKind.setSelection(Kind.PASSIVE.equals(newValue));
				}
			} finally {
				uiUpdateInProgress = false;
			}
		} else if (evt.getSource().equals(getModel())) {
			Object oldValue = evt.getOldValue();
			if (Signature.PROPERTY_CONTROL.equals(propertyName)) {
				if (oldValue == null && newValue != null) {
					((Control)newValue).addPropertyChangeListener(this);
				} else if (oldValue != null && newValue == null) {
					((Control)oldValue).removePropertyChangeListener(this);
				}
			}
		}
	}

	@Override
	protected void createActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void tryApplyChange(IChange c) throws ChangeRejectedException {
		getModel().tryApplyChange(c);
	}
	
	@Override
	public boolean doChange(IChange c) {
		return super.doChange(c);
	}
}
