package org.bigraph.bigmc.red;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigraph.model.savers.SaveFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import dk.itu.big_red.interaction_managers.InteractionManager;
import dk.itu.big_red.utilities.io.AsynchronousOutputThread;
import dk.itu.big_red.utilities.io.IAsynchronousOutputRecipient;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.io.strategies.LineReadStrategy;
import dk.itu.big_red.utilities.io.strategies.TotalReadStrategy;

public class BigMCInteractionManager extends InteractionManager {
	private Text stepText;
	private int stepCount;
	
	private class StepCountListener extends SelectionAdapter
	implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			update();
		}

		@Override
		public void focusLost(FocusEvent e) {
			update();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			update();
		}
		
		private void update() {
			try {
				stepCount = Integer.parseInt(stepText.getText());
			} catch (NumberFormatException e) {
				stepCount = 1000;
			}
			stepText.setText(Integer.toString(stepCount));
		}
	}
	
	private class OptionsDialog extends Dialog {
		protected OptionsDialog(Shell parentShell) {
			super(parentShell);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			new Label(c, SWT.NONE).setText("Number of steps:");
			
			StepCountListener scl = new StepCountListener();
			stepText = new Text(c, SWT.BORDER);
			stepText.addFocusListener(scl);
			stepText.addSelectionListener(scl);
			
			return c;
		}
		
		@Override
		protected void buttonPressed(int buttonId) {
			try {
				stepCount = Integer.parseInt(stepText.getText());
			} catch (NumberFormatException nfe) {
				stepCount = 1000;
			}
			super.buttonPressed(buttonId);
		}
	}
	
	String rstr;
	
	private byte[] getInput(/* TEMPORARY */Shell parent) {
		try {
			final IOAdapter io = new IOAdapter();
			new SimulationSpecBigMCSaver().
				setOutputStream(io.getOutputStream()).
					setModel(getSimulationSpec()).exportObject();
			
			Dialog d = new Dialog(parent) {
				private Text t;
				@Override
				protected Control createDialogArea(Composite parent) {
					Composite c = (Composite)super.createDialogArea(parent);
					t = new Text(parent,
							SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
					GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
					gd.widthHint = 400;
					gd.heightHint = 500;
					t.setLayoutData(gd);
					try {
						t.setText(new String(
							new TotalReadStrategy().read(io.getInputStream())));
					} catch (IOException e) {
						e.printStackTrace();
					}
					return c;
				}
				
				@Override
				protected void okPressed() {
					rstr = t.getText();
					super.okPressed();
				}
				
				@Override
				protected void createButtonsForButtonBar(Composite parent) {
					createButton(parent,
						IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
						true);
				}
			};
			
			d.open();
			
			return rstr.getBytes();
		} catch (SaveFailedException sfe) {
			return new byte[0];
		}
	}
	
	private static final Pattern
		state = Pattern.compile("^(\\d+): (.*)$"),
		violation = Pattern.compile("^\\*\\*\\* Found violation of property: .*$"),
		violationProp = Pattern.compile("^\\*\\*\\* (.*): (.*)$"),
		violationFirst = Pattern.compile("^#(\\d+)  (.*)$"),
		violationSecond = Pattern.compile("^ >> (.*)$"),
		
		violationEnd =
			Pattern.compile("^\\[mc::step\\] Counter-example found.$"),
		unfinished = Pattern.compile("^\\[mc::step\\] Interrupted!  Reached maximum steps: \\d+$"),
		finished = Pattern.compile("^\\[mc::step\\] Complete!$");
	
	static enum State {
		UNFINISHED,
		PROPERTY_VIOLATION,
		FINISHED
	}
	
	private ArrayList<String>
		states = new ArrayList<String>(),
		rules = new ArrayList<String>();
	private String violatedProperty, violatedPropertyValue;
	private int steps = 0;
	
	private final class InputCollector implements IRunnableWithProgress {
		private InputStream is;
		
		private InputCollector(InputStream is) {
			this.is = is;
		}
		
		private final LineReadStrategy rs = new LineReadStrategy();
		
		private final String getLine() {
			byte[] b;
			try {
				b = rs.read(is);
				return (b != null ?
						new String(b).trim() : null);
			} catch (IOException e) {
				return null;
			}
		}
		
		private State st = State.UNFINISHED;
		
		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Running BigMC", stepCount);
			monitor.subTask("Exploring state space");
			String s;
			Matcher m;
			boolean completed = false;
			while (!completed &&
					!monitor.isCanceled() && (s = getLine()) != null) {
				if (s.length() == 0)
					continue;
				switch (st) {
				case UNFINISHED:
					if ((m = state.matcher(s)).matches()) {
						steps = Integer.parseInt(m.group(1));
						states.add(m.group(2));
						monitor.worked(1);
					} else if ((m = violation.matcher(s)).matches()) {
						monitor.subTask("Processing property violation");
						states.clear();
						st = State.PROPERTY_VIOLATION;
					} else if ((m = finished.matcher(s)).matches()) {
						completed = true;
						st = State.FINISHED;
					} else if ((m = unfinished.matcher(s)).matches()) {
						completed = true;
					}
					break;
				case PROPERTY_VIOLATION:
					if ((m = violationProp.matcher(s)).matches()) {
						if (violatedProperty == null &&
						    violatedPropertyValue == null) {
							violatedProperty = m.group(1);
							violatedPropertyValue = m.group(2);
						}
					} else if ((m = violationFirst.matcher(s)).matches()) {
						states.add(0, m.group(2));
					} else if ((m = violationSecond.matcher(s)).matches()) {
						rules.add(0, m.group(2));
					} else if ((m = violationEnd.matcher(s)).matches()) {
						completed = true;
					}
					break;
				case FINISHED:
					break;
				}
			}
			monitor.subTask("Finishing");
			monitor.done();
		}
	}
	
	private static final IAsynchronousOutputRecipient aor =
			new IAsynchronousOutputRecipient() {
		@Override
		public void signalOutputError(IOException e) {
			e.printStackTrace();
		}
	};
	
	@Override
	public void run(Shell parent) {
		if (new OptionsDialog(parent).open() == Dialog.OK) {
			byte[] b = getInput(parent);
			
			ProcessBuilder pb = new ProcessBuilder(
					Preferences.getBigMCPath(),
					"-m", Integer.toString(stepCount),
					"-p",
					"-");
			Process process;
			try {
				process = pb.start();
			} catch (IOException e) {
				ErrorDialog.openError(parent, "Error", e.getLocalizedMessage(),
					new Status(
						Status.ERROR, Activator.PLUGIN_ID,
						e.getLocalizedMessage(), e));
				return;
			}
		
			AsynchronousOutputThread ot =
				new AsynchronousOutputThread(aor).
					setOutputStream(process.getOutputStream());
			ot.start();
			ot.add(b);
			ot.done();
			
			ProgressMonitorDialog d =
					new ProgressMonitorDialog(parent);
			
			InputCollector ic =
					new InputCollector(process.getInputStream());
			try {
				d.run(true, true, ic);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			
			process.destroy();
			ResultsDialog rd = new ResultsDialog(parent);
			rd.setSimulationSpec(getSimulationSpec()).setState(ic.st).
				setStates(states).setRules(rules).setSteps(steps).
				setViolationDetails(violatedProperty,
						violatedPropertyValue).open();
		}
	}

}