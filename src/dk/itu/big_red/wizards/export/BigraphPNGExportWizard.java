package dk.itu.big_red.wizards.export;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.editors.BigraphEditor;
import dk.itu.big_red.figure.import_export.BigraphPNGExport;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.Utility;

public class BigraphPNGExportWizard extends Wizard implements IExportWizard {
	private WizardPage page = null;
	
	private IWorkbench workbench = null;
	
	private String filename = null;
	private int format = SWT.IMAGE_PNG;
	
	@Override
	public boolean performFinish() {
		BigraphPNGExport be = new BigraphPNGExport();
		be.setModel(getEditor().getPrintLayer());
		try {
			be.setOutputFile(filename);
			be.exportObject();
			return true;
		} catch (Exception e) {
			page.setErrorMessage(e.getLocalizedMessage());
			return false;
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		setWindowTitle("Export as bitmap image");
		
		page = new WizardPage("Big Red.BitmapExportWizard") {
			@Override
			public void createControl(final Composite parent) {
				setTitle("Export as bitmap image");
				setMessage("Choose the name and format of the file to export to.");
				
				Composite form = new Composite(parent, SWT.NONE);
				
				GridLayout l = new GridLayout();
				l.numColumns = 2;
				form.setLayout(l);
				
				final Label filenameCaption = new Label(form, SWT.LEFT);
				filenameCaption.setText("Filename:");
				
				final Button filenameButton = new Button(form, SWT.PUSH);
				filenameButton.setImage(Utility.getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT));
				filenameButton.setText("(none)");
				
				final Label formatCaption = new Label(form, SWT.LEFT);
				formatCaption.setText("Format:");
				
				final Combo formatCombo = new Combo(form, SWT.BORDER | SWT.READ_ONLY);
				formatCombo.setItems(new String[] {
					"PNG (lossless, compressed)",
					"BMP (lossless, uncompressed)",
					"GIF (lossy, compressed)",
					"JPEG (lossy, photograph)"
				});
				formatCombo.select(0);
				
				formatCombo.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						switch (formatCombo.getSelectionIndex()) {
						case 0:
							setFormat(SWT.IMAGE_PNG);
							break;
						case 1:
							setFormat(SWT.IMAGE_BMP);
							break;
						case 2:
							setFormat(SWT.IMAGE_GIF);
							break;
						case 3:
							setFormat(SWT.IMAGE_JPEG);
							break;
						}
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						return;
					}
				});
				
				filenameButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						FileDialog f = UI.getFileDialog(parent.getShell(), SWT.SAVE | SWT.APPLICATION_MODAL);
						f.setText("Export as...");
						f.setFilterExtensions(new String[] {
							"*.png;*.bmp;*.gif;*.jpg;*.jpeg"
						});
						f.setFilterNames(new String[] {
							"Supported export formats (*.png, *.bmp, *.gif, *.jpg, *.jpeg)"
						});
						
						String filename = f.open();
						if (filename != null) {
							setFilename(filename);
							
							filenameButton.setText(filename);
							filenameButton.pack();
							
							String loFi = filename.toLowerCase();
							
							if (loFi.endsWith(".gif")) {
								formatCombo.select(2);
								setFormat(SWT.IMAGE_GIF);
							} else if (loFi.endsWith(".jpeg") || loFi.endsWith(".jpg")) {
								formatCombo.select(3);
								setFormat(SWT.IMAGE_JPEG);
							} else if (loFi.endsWith(".bmp")) {
								formatCombo.select(1);
								setFormat(SWT.IMAGE_BMP);
							} else {
								formatCombo.select(0);
								setFormat(SWT.IMAGE_PNG);
							}
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						return;
					}
				});
				
				setControl(form);
			}
		};
		
		addPage(page);
	}

	private BigraphEditor getEditor() {
		try {
			return (BigraphEditor)workbench.getActiveWorkbenchWindow().getActivePage().getActivePart();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public int getFormat() {
		return format;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
}
