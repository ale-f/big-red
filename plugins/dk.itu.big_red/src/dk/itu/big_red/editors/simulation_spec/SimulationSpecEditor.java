package dk.itu.big_red.editors.simulation_spec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.savers.SimulationSpecXMLSaver;
import org.bigraph.model.wrapper.SaverUtilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.editors.AbstractNonGEFEditor;
import dk.itu.big_red.editors.assistants.IFactory;
import dk.itu.big_red.interaction_managers.IInteractionManager;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;
import dk.itu.big_red.utilities.ui.StockButton;

public class SimulationSpecEditor extends AbstractNonGEFEditor
		implements PropertyChangeListener {
	private static class ExportInteractionManagerFactory
			extends ConfigurationElementInteractionManagerFactory {
		public ExportInteractionManagerFactory(IConfigurationElement ice) {
			super(ice);
		}
		
		@Override
		public IInteractionManager newInstance() {
			Saver s;
			try {
				s = (Saver)getCE().createExecutableExtension("class");
			} catch (CoreException e) {
				return null;
			}
			return new BasicCommandLineInteractionManager(s);
		}
	}
	
	@Override
	protected Resolver getResolver() {
		return getModel();
	}
	
	@Override
	public void doActualSave(IFile f, OutputStream os)
			throws SaveFailedException {
    	SimulationSpecXMLSaver r = new SimulationSpecXMLSaver().setModel(getModel());
		r.setFile(new EclipseFileWrapper(f)).
    		setOutputStream(os).exportObject();
    	setSavePoint();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		firePropertyChange(PROP_INPUT);
	}
	
	private SimulationSpec model = null;
	
	@Override
	protected SimulationSpec getModel() {
		return model;
	}
	
	private boolean uiUpdateInProgress = false;
	
	private static final IFile getFileFrom(ModelObject m) {
		IFileWrapper fw = FileData.getFile(m);
		return (fw instanceof EclipseFileWrapper ?
				((EclipseFileWrapper)fw).getResource() : null);
	}
	
	@Override
	protected void loadModel() throws LoadFailedException {
		model = (SimulationSpec)loadInput();
	}
	
	@Override
	protected void updateEditorControl() {
		if (getError() != null)
			return;
		clearUndo();
		rules.setInput(model);
		model.addPropertyChangeListener(this);
		modelToControls();
	}
	
	private void modelToControls() {
		uiUpdateInProgress = true;
		
		modelSelector.setResource(getFileFrom(model.getModel()));
		signatureSelector.setResource(getFileFrom(model.getSignature()));
		
		recalculateExportEnabled();
		uiUpdateInProgress = false;
	}
	
	private static ArrayList<IFactory<IInteractionManager>> getIMFactories() {
		ArrayList<IFactory<IInteractionManager>> factories =
				new ArrayList<IFactory<IInteractionManager>>();
		
		IExtensionRegistry r = RegistryFactory.getRegistry();
		for (IConfigurationElement ce :
				r.getConfigurationElementsFor(
						IInteractionManager.EXTENSION_POINT))
			factories.add(new ConfigurationElementInteractionManagerFactory(ce));
		
		for (IConfigurationElement ce :
				r.getConfigurationElementsFor(
						SaverUtilities.EXTENSION_POINT)) {
			String exports = ce.getAttribute("exports");
			if (SimulationSpec.class.getCanonicalName().equals(exports))
				factories.add(new ExportInteractionManagerFactory(ce));
		}
		return factories;
	}
	
	private ResourceSelector signatureSelector, modelSelector;
	private ListViewer rules;
	private Button export;
	
	private void recalculateExportEnabled() {
		export.setEnabled(
			getModel().getModel() != null &&
			getModel().getSignature() != null);
	}
	
	@Override
	public void createEditorControl(Composite parent) {
		Composite self = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		self.setLayout(gl);
		
		Label l;
		(l = new Label(self, SWT.RIGHT)).setText("Signature:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		signatureSelector = new ResourceSelector(self,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, Signature.CONTENT_TYPE);
		signatureSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		signatureSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				if (uiUpdateInProgress)
					return;
				try {
					Signature s = (newValue != null ?
						(Signature)new EclipseFileWrapper((IFile)newValue).load() : null);
					doChange(new BoundDescriptor(getModel(),
							new SimulationSpec.ChangeSetSignatureDescriptor(
									new SimulationSpec.Identifier(),
									getModel().getSignature(), s)));
					recalculateExportEnabled();
				} catch (LoadFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		(l = new Label(self, SWT.RIGHT)).setText("Reaction rules:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		rules = new ListViewer(self);
		rules.setContentProvider(new SimulationSpecRRContentProvider(rules));
		rules.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IFile f = getFileFrom((ModelObject)element);
				if (f != null) {
					return f.getProjectRelativePath().toString();
				} else return "(embedded rule)";
			}
		});
		rules.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite br = new Composite(self, SWT.NONE);
		br.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, false));
		RowLayout brl = new RowLayout(SWT.VERTICAL);
		brl.marginBottom = brl.marginLeft = brl.marginRight =
				brl.marginTop = 0;
		brl.pack = false;
		br.setLayout(brl);
		
		StockButton.ADD.create(br, SWT.NONE, true).addSelectionListener(
				new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceTreeSelectionDialog rtsd =
					new ResourceTreeSelectionDialog(
						getSite().getShell(),
						((FileEditorInput)getEditorInput()).getFile().getProject(),
						Mode.FILE, ReactionRule.CONTENT_TYPE);
				rtsd.setBlockOnOpen(true);
				if (rtsd.open() == Dialog.OK) {
					IFile f = (IFile)rtsd.getFirstResult();
					try {
						ReactionRule r = (ReactionRule)new EclipseFileWrapper(f).load();
						doChange(new BoundDescriptor(model,
								new SimulationSpec.ChangeAddRuleDescriptor(
										new SimulationSpec.Identifier(),
										-1, r)));
					} catch (LoadFailedException ife) {
						ife.printStackTrace();
					}
				}
			}
		});
		
		StockButton.REMOVE.create(br).addSelectionListener(
				new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Iterator<?> it =
					((IStructuredSelection)rules.getSelection()).iterator();
				ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
				PropertyScratchpad scratch = new PropertyScratchpad();
				while (it.hasNext()) {
					ReactionRule rr = (ReactionRule)it.next();
					IChangeDescriptor ch =
							new SimulationSpec.ChangeRemoveRuleDescriptor(
									new SimulationSpec.Identifier(),
									getModel().getRules(scratch).indexOf(rr),
									rr);
					ch.simulate(scratch, getModel());
					cg.add(ch);
				}
				if (!cg.isEmpty())
					doChange(new BoundDescriptor(getModel(), cg));
			}
		});
		
		(l = new Label(self, SWT.RIGHT)).setText("Model:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		modelSelector = new ResourceSelector(self,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, Bigraph.CONTENT_TYPE);
		modelSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		modelSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				if (uiUpdateInProgress)
					return;
				try {
					Bigraph b = (newValue != null ?
						(Bigraph)new EclipseFileWrapper((IFile)newValue).load() : null);
					doChange(new BoundDescriptor(getModel(),
							new SimulationSpec.ChangeSetModelDescriptor(
									new SimulationSpec.Identifier(),
									getModel().getModel(), b)));
					recalculateExportEnabled();
				} catch (LoadFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		new Label(self, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		(l = new Label(self, SWT.RIGHT)).setText("Tool:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		final ComboViewer cv = new ComboViewer(self);
		cv.setContentProvider(new ArrayContentProvider());
		cv.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IFactory<?>)element).getName();
			}
		});
		cv.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ArrayList<IFactory<IInteractionManager>> exporters = getIMFactories();
		cv.setInput(exporters);
		cv.setSelection(new StructuredSelection(exporters.get(0)));
		
		(export = new Button(self, SWT.NONE)).setText("&Export...");
		export.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IInteractionManager im =
					(IInteractionManager)((IFactory<?>)
						((IStructuredSelection)cv.getSelection()).
							getFirstElement()).newInstance();
				im.setSimulationSpec(getModel());
				im.run(getEditorSite().getShell());
			}
		});
		export.setEnabled(false);
	}
	
	@Override
	protected void createActions() {
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		if (signatureSelector != null) {
			Button b = signatureSelector.getButton();
			if (b != null && !b.isDisposed() && b.isVisible())
				b.setFocus();
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() != getModel() || uiUpdateInProgress)
			return;
		uiUpdateInProgress = true;
		try {
			String propertyName = evt.getPropertyName();
			Object newValue = evt.getNewValue();
			if (SimulationSpec.PROPERTY_SIGNATURE.equals(propertyName)) {
				Signature s = (Signature)newValue;
				signatureSelector.setResource(getFileFrom(s));
			} else if (SimulationSpec.PROPERTY_MODEL.equals(propertyName)) {
				Bigraph b = (Bigraph)newValue;
				modelSelector.setResource(getFileFrom(b));
			}
		} finally {
			uiUpdateInProgress = false;
			recalculateExportEnabled();
		}
	}
}
