package dk.itu.big_red.wizards;

import dk.itu.big_red.GraphicalEditor;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Control.Shape;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ControlEditorWizardPage extends WizardPage {
	private String[] portNames = null;
	
	protected Bigraph getModel() {
		return ((GraphicalEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getModel();
	}
	
	protected Control getWizardControl() {
		return ((IControlSelector)getWizard()).getSelectedControl();
	}
	
	protected ControlEditorWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		setTitle("Edit");
		setMessage("Change control properties.");
	}
	
	protected Text nameInput = null;
	protected Combo appearanceChoice = null;
	protected Combo topPortChoice = null;
	protected Combo leftPortChoice = null;
	protected Combo bottomPortChoice = null;
	protected Combo rightPortChoice = null;
	protected Spinner widthSpinner = null;
	protected Spinner heightSpinner = null;
	protected Button resizeButton = null;
	
	@Override
	public void createControl(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		
		GridLayout l = new GridLayout();
		l.numColumns = 2;
		form.setLayout(l);

		Label name = new Label(form, SWT.NONE);
		name.setText("&Name:");
		
		int nameInputFlags = SWT.BORDER;
		nameInput = new Text(form, nameInputFlags);
		nameInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		if (getWizard().getClass() != ControlAddWizard.class) {
			nameInput.setEnabled(false);
		}
		
		nameInput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(((Text)e.widget).getText().length() > 0);
			}
		});
		
		Label appearance = new Label(form, SWT.NONE);
		appearance.setText("&Appearance:");
		
		appearanceChoice = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		appearanceChoice.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		appearanceChoice.setItems(new String[] {
			"Rectangle",
			"Oval",
			"Triangle",
//			"Complex polygon"
		});
		appearanceChoice.select(0);
		
		portNames = new String[]{};
		
		Label topPort = new Label(form, SWT.NONE);
		topPort.setText("&Top port:");
		
		topPortChoice = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		topPortChoice.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		topPortChoice.setItems(portNames);
		topPortChoice.add("(none)", 0);
		topPortChoice.select(0);
		
		Label leftPort = new Label(form, SWT.NONE);
		leftPort.setText("&Left port:");
		
		leftPortChoice = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		leftPortChoice.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		leftPortChoice.setItems(portNames);
		leftPortChoice.add("(none)", 0);
		leftPortChoice.select(0);
		
		Label bottomPort = new Label(form, SWT.NONE);
		bottomPort.setText("&Bottom port:");
		
		bottomPortChoice = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		bottomPortChoice.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		bottomPortChoice.setItems(portNames);
		bottomPortChoice.add("(none)", 0);
		bottomPortChoice.select(0);
		
		Label rightPort = new Label(form, SWT.NONE);
		rightPort.setText("&Right port:");
		
		rightPortChoice = new Combo(form, SWT.DROP_DOWN | SWT.READ_ONLY);
		rightPortChoice.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		rightPortChoice.setItems(portNames);
		rightPortChoice.add("(none)", 0);
		rightPortChoice.select(0);
		
		if (getWizard().getClass() != ControlAddWizard.class) {
			topPortChoice.setEnabled(false);
			leftPortChoice.setEnabled(false);
			bottomPortChoice.setEnabled(false);
			rightPortChoice.setEnabled(false);
		}
		
		Label sizeLabel = new Label(form, SWT.NONE);
		sizeLabel.setText("&Default size:");
		
		Composite c = new Composite(form, SWT.NONE);
		RowLayout r = new RowLayout();
		r.center = true;
		c.setLayout(r);
		
		widthSpinner = new Spinner(c, SWT.BORDER);
		widthSpinner.setSelection(50); widthSpinner.setMaximum(1000);
		
		Label p = new Label(c, SWT.NONE); p.setText("px by ");
		
		heightSpinner = new Spinner(c, SWT.BORDER);
		heightSpinner.setSelection(50); heightSpinner.setMaximum(1000);
		p = new Label(c, SWT.NONE); p.setText("px");
		
		new Label(form, SWT.NONE); /* padding */
		
		resizeButton = new Button(form, SWT.CHECK);
		resizeButton.setText("&Allow resizing");
		
		setControl(form);
	}

	public void updateFromControl() {
		Control m = getWizardControl();
		if (m != null) {
			nameInput.setText(m.getLongName());
			appearanceChoice.select(m.getShape().ordinal());
			topPortChoice.select(0);
			leftPortChoice.select(0);
			bottomPortChoice.select(0);
			rightPortChoice.select(0);
			String[] ports = new String[]{};
			for (String p : m.getPortNames()) {
				/*
				 * 0 is a safe default ("(none)").
				 */
				int portIndex = 0;
				for (int i = 0; i < ports.length; i++) {
					if (ports[i].equals(p))
						portIndex = i + 1;
				}
				Combo appropriatePortChoice = null;
				switch (m.getOffset(p)) {
				case 0:
					appropriatePortChoice = topPortChoice;
					break;
				case 1:
					appropriatePortChoice = leftPortChoice;
					break;
				case 2:
					appropriatePortChoice = bottomPortChoice;
					break;
				case 3:
					appropriatePortChoice = rightPortChoice;
					break;
				default:
					break;
				}
				appropriatePortChoice.select(portIndex);
			}
			
			widthSpinner.setSelection(m.getDefaultSize().x);
			heightSpinner.setSelection(m.getDefaultSize().y);
			resizeButton.setSelection(m.isResizable());
		}
	}
	
	public void registerControlFromValues() {
		String longName = nameInput.getText(),
		       label = nameInput.getText().substring(0, 1).toUpperCase();
		Control.Shape shape = Control.Shape.values()[appearanceChoice.getSelectionIndex()];
		Point defaultSize = new Point(widthSpinner.getSelection(), heightSpinner.getSelection());
		boolean constraintModifiable = resizeButton.getSelection();
		
		String topPort = null, leftPort = null, bottomPort = null, rightPort = null;
		
		if (topPortChoice.getSelectionIndex() > 0)
			topPort = portNames[topPortChoice.getSelectionIndex() - 1];
		
		if (leftPortChoice.getSelectionIndex() > 0)
			leftPort = portNames[leftPortChoice.getSelectionIndex() - 1];
		
		if (bottomPortChoice.getSelectionIndex() > 0)
			bottomPort = portNames[bottomPortChoice.getSelectionIndex() - 1];
		
		if (rightPortChoice.getSelectionIndex() > 0)
			rightPort = portNames[rightPortChoice.getSelectionIndex() - 1];
		
		Signature ma = getModel().getSignature();
		Control m = ma.getControl(longName);
		if (m == null) {
			m = ma.addControl(longName, label, shape, defaultSize, constraintModifiable);
			
			m.addPort(topPort, 0);
			m.addPort(leftPort, 1);
			m.addPort(bottomPort, 2);
			m.addPort(rightPort, 3);
		} else {
			/* XXX: propagate changes *properly* through the model! Ports! */
			m.setLabel(label);
			m.setShape(shape);
			m.setDefaultSize(defaultSize);
			m.setResizable(constraintModifiable);
		}
	}
}
