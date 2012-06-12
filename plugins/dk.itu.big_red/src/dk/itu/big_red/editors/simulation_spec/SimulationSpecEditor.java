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
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.dialogs.Dialog;
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
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.assistants.IFactory;
import dk.itu.big_red.interaction_managers.IInteractionManager;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.savers.SimulationSpecXMLSaver;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;
import dk.itu.big_red.utilities.ui.StockButton;
import dk.itu.big_red.utilities.ui.jface.ListContentProvider;
import dk.itu.big_red.utilities.ui.UI;

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
	protected void tryApplyChange(Change c) throws ChangeRejectedException {
		getModel().tryApplyChange(c);
	}
	
	@Override
	public void doActualSave(IFile f, OutputStream os)
			throws SaveFailedException {
    	new SimulationSpecXMLSaver().setModel(getModel()).setFile(f).
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
	
	@Override
	protected void initialiseActual() throws Throwable {
		model = (SimulationSpec)loadInput();
		
		if (getModel() == null) {
			replaceWithError(new Exception("Model is null"));
			return;
		}
		
		rules.setInput(model);
		model.addPropertyChangeListener(this);
		modelToControls();
	}
	
	private void modelToControls() {
		uiUpdateInProgress = true;
		
		Signature s = model.getSignature();
		if (s != null) {
			IFile f = ExtendedDataUtilities.getFile(s);
			if (f != null)
				signatureSelector.setResource(f);
		}
		
		Bigraph b = model.getModel();
		if (b != null) {
			IFile f = ExtendedDataUtilities.getFile(b);
			if (f != null)
				modelSelector.setResource(f);
		}
		
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
		     r.getConfigurationElementsFor(Saver.EXTENSION_POINT)) {
			String exports = ce.getAttribute("exports");
			if (exports.equals(SimulationSpec.class.getCanonicalName()))
				factories.add(new ExportInteractionManagerFactory(ce));
		}
		return factories;
	}
	
	private ResourceSelector signatureSelector, modelSelector;
	private ListViewer rules;
	private Button export;
	
	private void recalculateExportEnabled() {
		export.setEnabled(
			signatureSelector.getResource() != null &&
			modelSelector.getResource() != null);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite self =
			setComposite(UI.chain(new Composite(setParent(parent), SWT.NONE)).
			layoutData(new GridData(SWT.FILL, SWT.FILL, true, true)).done());
		
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		self.setLayout(gl);
		
		UI.chain(new Label(self, SWT.RIGHT)).text("Signature:").done().setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		signatureSelector = new ResourceSelector(self,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, Signature.CONTENT_TYPE);
		signatureSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		signatureSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				recalculateExportEnabled();
				try {
					if (uiUpdateInProgress)
						return;
					Signature s = null;
					if (newValue != null)
						s = (Signature)Loader.fromFile((IFile)newValue);
					doChange(getModel().changeSignature(s));
				} catch (LoadFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		UI.chain(new Label(self, SWT.RIGHT)).text("Reaction rules:").done().
			setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		rules = new ListViewer(self);
		UI.setProviders(rules, new SimulationSpecRRContentProvider(rules),
			new LabelProvider() {
				@Override
				public String getText(Object element) {
					IFile f =
						ExtendedDataUtilities.getFile((ModelObject)element);
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
						ReactionRule r = (ReactionRule)Loader.fromFile(f);
						doChange(model.changeAddRule(r));
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
				while (it.hasNext())
					doChange(getModel().
							changeRemoveRule((ReactionRule)it.next()));
			}
		});
		
		UI.chain(new Label(self, SWT.RIGHT)).text("Model:").done().setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		modelSelector = new ResourceSelector(self,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, Bigraph.CONTENT_TYPE);
		modelSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		modelSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				recalculateExportEnabled();
				try {
					if (uiUpdateInProgress)
						return;
					Bigraph b = null;
					if (newValue != null)
						b = (Bigraph)Loader.fromFile((IFile)newValue);
					doChange(getModel().changeModel(b));
				} catch (LoadFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		new Label(self, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		UI.chain(new Label(self, SWT.RIGHT)).text("Tool:").done().setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		final ComboViewer cv = UI.setProviders(new ComboViewer(self),
			new ListContentProvider(), new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((IFactory<?>)element).getName();
				}
			});
		cv.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ArrayList<IFactory<IInteractionManager>> exporters = getIMFactories();
		cv.setInput(exporters);
		cv.setSelection(new StructuredSelection(exporters.get(0)));
		
		export = UI.chain(new Button(self, SWT.NONE)).text("&Export...").done();
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
		
		initialise();
	}
	
	@Override
	protected void createActions() {
	}
	
	@Override
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
		updateActions(getStateActions());
	}
	
	@Override
	public void setFocus() {
		if (getComposite() == null)
			return;
		signatureSelector.getButton().setFocus();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() != getModel() || uiUpdateInProgress)
			return;
		uiUpdateInProgress = true;
		try {
			String propertyName = evt.getPropertyName();
			Object newValue = evt.getNewValue();
			uiUpdateInProgress = true;
			if (propertyName.equals(SimulationSpec.PROPERTY_SIGNATURE)) {
				Signature s = (Signature)newValue;
				signatureSelector.setResource(
						ExtendedDataUtilities.getFile(s));
			} else if (propertyName.equals(SimulationSpec.PROPERTY_MODEL)) {
				Bigraph b = (Bigraph)newValue;
				modelSelector.setResource(
						ExtendedDataUtilities.getFile(b));
			}
		} finally {
			uiUpdateInProgress = false;
		}
	}
}
