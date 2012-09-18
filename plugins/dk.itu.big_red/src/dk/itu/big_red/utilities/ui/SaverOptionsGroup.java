package dk.itu.big_red.utilities.ui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import org.bigraph.model.savers.ISaver;
import org.bigraph.model.savers.Saver;

public class SaverOptionsGroup {
	private Composite optionsGroup;
	
	protected Composite getGroup() {
		return optionsGroup;
	}
	
	public SaverOptionsGroup(Composite parent) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 0;
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		sc.setLayoutData(gd);
		
		RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.marginLeft = rl.marginRight = rl.marginTop = 10;
		rl.spacing = 5;
		optionsGroup = new Composite(sc, SWT.NONE);
		optionsGroup.setLayout(rl);
		sc.setContent(optionsGroup);
		
		setSaver(null);
	}
	
	public void setSaver(Saver s) {
		for (Control c : getGroup().getChildren())
			c.dispose();
		getGroup().pack();
		if (s != null && s.getOptions().size() > 0) {
			ArrayList<Control> optionControls = new ArrayList<Control>();
			for (final ISaver.Option d : s.getOptions()) {
				Composite opt = new Composite(getGroup(), SWT.NONE);
				opt.setLayout(new RowLayout(SWT.VERTICAL));
				optionControls.add(opt);
				
				Widget w = null;
				Object ov = d.get();
				if (ov instanceof Boolean) {
					final Button b = new Button(opt, SWT.CHECK);
					b.setText(d.getName());
					b.setSelection((Boolean)ov);
					b.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							d.set(b.getSelection());
						}
					});
					w = b;
				}
				if (w != null && d.getDescription() != null)
					new Label(opt, SWT.NONE).setText(d.getDescription());
			}
		} else {
			Label l = new Label(getGroup(), SWT.CENTER);
			l.setText("no options");
		}
		recompute(true);
	}
	
	private void recompute(boolean changed) {
		getGroup().setSize(
				getGroup().computeSize(SWT.DEFAULT, SWT.DEFAULT, false));
	}
}
