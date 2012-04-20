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

import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.Saver.Option;

public class SaverOptionsGroup {
	private Composite optionsGroup;
	
	protected Composite getGroup() {
		return optionsGroup;
	}
	
	public SaverOptionsGroup(Composite parent) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 0;
		ScrolledComposite sc =
			UI.chain(new ScrolledComposite(parent, SWT.V_SCROLL)).
			layoutData(gd).done();
		
		RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.marginLeft = rl.marginRight = rl.marginTop = 10;
		rl.spacing = 5;
		optionsGroup =
			UI.chain(new Composite(sc, SWT.NONE)).layout(rl).
			done();
		sc.setContent(optionsGroup);
		
		setSaver(null);
	}
	
	public void setSaver(Saver s) {
		for (Control c : getGroup().getChildren())
			c.dispose();
		if (s != null && s.getOptions().size() > 0) {
			ArrayList<Control> optionControls = new ArrayList<Control>();
			for (final Option d : s.getOptions()) {
				Composite opt = new Composite(getGroup(), SWT.NONE);
				opt.setLayout(new RowLayout(SWT.VERTICAL));
				optionControls.add(opt);
				
				Object ov = d.get();
				if (ov instanceof Boolean) {
					final Button b =
						UI.chain(new Button(opt, SWT.CHECK)).
						text(d.getDescription()).done();
					b.setSelection((Boolean)ov);
					b.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							d.set(b.getSelection());
						}
					});
				}
			}
		} else {
			Label l = new Label(getGroup(), SWT.CENTER);
			l.setText("no options");
		}
		
		recompute();
	}
	
	private void recompute() {
		getGroup().setSize(getGroup().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
}
