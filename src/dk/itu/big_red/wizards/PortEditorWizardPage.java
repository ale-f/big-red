package dk.itu.big_red.wizards;

import dk.itu.big_red.GraphicalEditor;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Signature;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

public class PortEditorWizardPage extends WizardPage {
	protected Bigraph getModel() {
		return ((GraphicalEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getModel();
	}
	
	protected String getWizardPort() {
		return ((IPortSelector)getWizard()).getSelectedPort();
	}
	
	protected PortEditorWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		setTitle("Edit");
		setMessage("Change port connections.");
	}
	
	protected Text nameInput = null;
	protected Tree connectionsTree = null;
	
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
		if (getWizard().getClass() != PortAddWizard.class)
			nameInput.setEnabled(false);
		
		nameInput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(((Text)e.widget).getText().length() > 0);
			}
		});
		
		Label permittedConnections = new Label(form, SWT.NONE); /* padding */
		permittedConnections.setText("&Permitted\nconnections:");
		
		Composite bottomHalf = new Composite(form, SWT.NONE);
		bottomHalf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout l2 = new GridLayout();
		l2.numColumns = 2;
		bottomHalf.setLayout(l2);
		
		connectionsTree = new Tree(bottomHalf, SWT.SINGLE | SWT.BORDER | SWT.VIRTUAL);
		connectionsTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite buttonPanel = new Composite(bottomHalf, SWT.NONE);
		RowLayout r = new RowLayout();
		r.type = SWT.VERTICAL;
		r.fill = true;
		buttonPanel.setLayout(r);
		
		Button addButton = new Button(buttonPanel, SWT.PUSH);
		addButton.setText("&Add");
		addButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = new TreeItem(connectionsTree, SWT.NONE);
				item.setText("#new");
				Event evt = new Event(); evt.item = item;
				connectionsTree.select(item);
				connectionsTree.notifyListeners(SWT.Selection, evt);
				connectionsTree.notifyListeners(SWT.Selection, evt);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		
		Button removeButton = new Button(buttonPanel, SWT.PUSH);
		removeButton.setText("&Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = connectionsTree.getSelection()[0];
				if (item != null) {
					String port = item.getText();
					Signature pa = getModel().getSignature();
					pa.denyConnection(getWizardPort(), port);
					item.dispose();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		
		/*
		 * 
		 */
		
		final TreeItem[] lastItem = new TreeItem[1];
		
	    final TreeEditor editor = new TreeEditor(connectionsTree);
	    connectionsTree.addListener(SWT.Selection, new Listener() {
	      protected boolean itemUpdated = false;
	      public void handleEvent(Event event) {
	        final TreeItem item = (TreeItem) event.item;
	        if (item != null && item == lastItem[0]) {
	          final Composite composite = new Composite(connectionsTree, SWT.NONE);
	          final Text text = new Text(composite, SWT.NONE);
	          composite.addListener(SWT.Resize, new Listener() {
	            public void handleEvent(Event e) {
	              Rectangle rect = composite.getClientArea();
	              text.setBounds(rect.x, rect.y, rect.width, rect.height);
	            }
	          });
	          Listener textListener = new Listener() {
	            public void handleEvent(final Event e) {
	              switch (e.type) {
	              case SWT.FocusOut:
	                item.setText(text.getText());
	                if (item.getText() == "")
		            	  item.dispose();
	                composite.dispose();
	                	                
	                break;
	              case SWT.Verify:
	                String newText = text.getText();
	                String leftText = newText.substring(0, e.start);
	                String rightText = newText.substring(e.end, newText.length());
	                GC gc = new GC(text);
	                Point size = gc.textExtent(leftText + e.text + rightText);
	                gc.dispose();
	                size = text.computeSize(size.x, SWT.DEFAULT);
	                editor.horizontalAlignment = SWT.LEFT;
	                Rectangle itemRect = item.getBounds(),
	                rect = connectionsTree.getClientArea();
	                editor.minimumWidth = Math.max(size.x, itemRect.width);
	                int left = itemRect.x,
	                right = rect.x + rect.width;
	                editor.minimumWidth = Math.min(editor.minimumWidth, right - left);
	                editor.minimumHeight = size.y;
	                editor.layout();
	                break;
	              case SWT.Traverse:
	                switch (e.detail) {
	                case SWT.TRAVERSE_RETURN:
	                  item.setText(text.getText());
	                // FALL THROUGH
	                case SWT.TRAVERSE_ESCAPE:
	                	if (item.getText() == "")
			            	  item.dispose();
	                  composite.dispose();
	                  e.doit = false;
	                }
	                break;
	              }
	            }
	          };
	          text.addListener(SWT.FocusOut, textListener);
	          text.addListener(SWT.Traverse, textListener);
	          text.addListener(SWT.Verify, textListener);
	          editor.setEditor(composite, item);
	          text.setText(item.getText());
	          text.selectAll();
	          text.setFocus();
	        }
	        lastItem[0] = item;
	      }
	    });
		
		setControl(form);
	}

	public void updateFromPort() {
		String port = getWizardPort();
		nameInput.setText(port);
		connectionsTree.removeAll();
		Signature pa = getModel().getSignature();
		for (String s : pa.getConnections(port)) {
			TreeItem item = new TreeItem(connectionsTree, SWT.NONE);
			item.setText(s);
		}
	}
	
	public void registerPortFromValues() {
		String port = nameInput.getText();
		Signature pa = getModel().getSignature();
		pa.clearConnections(port);
		for (TreeItem i : connectionsTree.getItems())
			pa.allowConnection(port, i.getText());
	}
}