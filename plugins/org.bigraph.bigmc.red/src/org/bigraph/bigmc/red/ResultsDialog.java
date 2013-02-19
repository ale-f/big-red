package org.bigraph.bigmc.red;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bigraph.bigmc.red.BigMCInteractionManager.State;
import org.bigraph.model.Bigraph;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.wrapper.SaverUtilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;

import dk.itu.big_red.editors.utilities.BigraphCanvas;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.Project.ModificationRunner.Callback;

public class ResultsDialog extends TitleAreaDialog {
	private Timer parseTimer;
	
	public ResultsDialog(Shell parentShell) {
		super(parentShell);
		
		parseTimer = new Timer(true);
	}
	
	private State state;
	
	public State getState() {
		return state;
	}
	
	private SimulationSpec ss;
	
	public ResultsDialog setSimulationSpec(SimulationSpec ss) {
		this.ss = ss;
		return this;
	}
	
	public ResultsDialog setState(State state) {
		this.state = state;
		return this;
	}
	
	private List<String> states;
	
	public List<String> getStates() {
		return states;
	}
	
	public ResultsDialog setStates(List<String> states) {
		this.states = states;
		return this;
	}
	
	private List<String> rules;
	
	public List<String> getRules() {
		return rules;
	}
	
	public ResultsDialog setRules(List<String> rules) {
		this.rules = rules;
		return this;
	}
	
	private int steps;
	
	public ResultsDialog setSteps(int steps) {
		this.steps = steps;
		return this;
	}
	
	private BigraphCanvas canvas;
	
	private TimerTask tt;
	
	private final class ParseTask extends TimerTask {
		private OutputParser op;
		
		public ParseTask(OutputParser op) {
			this.op = op;
		}
		
		@Override
		public void run() {
			final Bigraph b = op.run();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					canvas.setContents(b);
					canvas.setVisible(true);
				}
			});
		}
	}
	
	private String violatedPropertyName, violatedPropertyValue;
	
	public ResultsDialog setViolationDetails(String name, String value) {
		violatedPropertyName = name;
		violatedPropertyValue = value;
		return this;
	}
	
	protected OutputParser parserFor(int i) {
		return new OutputParser(ss.getSignature(), getStates().get(i));
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		
		setTitle((getState() == State.FINISHED ? "Complete" :
					getState() == State.PROPERTY_VIOLATION ?
							"Property violation" : "Not complete"));
		String message = "Unknown.";
		if (getState() == State.FINISHED) {
			message = "Complete";
		} else if (getState() == State.UNFINISHED) {
			message = "Not complete";
		} else if (getState() == State.PROPERTY_VIOLATION) {
			message = "Property \"" + violatedPropertyName +
					"\" (\"" + violatedPropertyValue + "\") was violated";
		}
		setMessage(message + " after " + steps + " steps.");
		
		Composite display = new Composite(c, SWT.BORDER);
		display.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		display.setLayout(new FillLayout(SWT.VERTICAL));
		
		/*Label spinner = new Label(display, SWT.NONE);
		display.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		spinner.setText("Loading...");
		spinner.setVisible(false);*/
		
		canvas = new BigraphCanvas(display, SWT.NONE);
		
		final Link l = new Link(c, SWT.LEAD);
		l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final SelectionAdapter sa;
		
		final Scale s = new Scale(c, SWT.NONE);
		s.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		if (getStates().size() > 1) {
			s.setMinimum(1);
			s.setMaximum(getStates().size());
			s.addSelectionListener((sa = new SelectionAdapter() {
				private int current = -1;
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					int now = s.getSelection() - 1;
					if (current == now)
						return;
					if (tt != null)
						tt.cancel();
					tt = new ParseTask(parserFor(now));
					canvas.setVisible(false);
					parseTimer.schedule(tt, 300);
					l.setText("Now showing bigraph " + s.getSelection() +
							" of " + s.getMaximum() + ".");
					current = now;
				}
			}));
			s.setSelection(1);
			sa.widgetSelected(null);
		} else {
			s.setEnabled(false);
			l.setText("Showing the only bigraph.");
			parseTimer.schedule(new ParseTask(parserFor(0)), 0);
		}
		
		return c;
	}
	
	public static final int SAVE_AS_ID = 1000;
	public static final String SAVE_AS_LABEL = "&Save bigraph as...";
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLOSE_ID) {
			setReturnCode(IDialogConstants.CLOSE_ID);
			close();
		} else if (buttonId == SAVE_AS_ID) {
			Bigraph b = canvas.getContents();
			SaveAsDialog d = new SaveAsDialog(getShell());
			d.setBlockOnOpen(true);
			if (d.open() == Dialog.OK) {
				final IFile f = ResourcesPlugin.getWorkspace().getRoot().
						getFile(d.getResult());
				IOAdapter io = new IOAdapter();
				setErrorMessage(null);
				try {
					Saver r = SaverUtilities.forContentType(Bigraph.CONTENT_TYPE);
					r.setFile(new EclipseFileWrapper(f)).
						setModel(b).setOutputStream(io.getOutputStream()).
						exportObject();
				} catch (Exception e) {
					setErrorMessage(e.getMessage());
					return;
				}
				Project.setContents(f, io.getInputStream(), new Callback() {
					@Override
					public void onSuccess() {
						setErrorMessage(null);
						setMessage("State saved to " + f.getFullPath() + ".",
								IMessageProvider.INFORMATION);
					}
					
					@Override
					public void onError(CoreException e) {
						setErrorMessage(e.getMessage());
						return;
					}
				});
			}
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent,
				IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
		createButton(parent, SAVE_AS_ID, SAVE_AS_LABEL, false);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
}