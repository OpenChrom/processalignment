/*******************************************************************************
 * Copyright (c) 2016, 2019 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.ui.wizards.IChromatogramWizardElements;
import org.eclipse.chemclipse.ux.extension.xxd.ui.wizards.InputEntriesWizard;
import org.eclipse.chemclipse.ux.extension.xxd.ui.wizards.InputWizardSettings;
import org.eclipse.chemclipse.ux.extension.xxd.ui.wizards.InputWizardSettings.InputDataType;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.DataInputEntry;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.preferences.PreferenceSupplier;

public class PageInputFiles {

	private EditorAlignment editorAlignment;
	private List<IDataInputEntry> dataInputEntries;
	private Table inputFilesTable;
	//
	private Shell shell = Display.getDefault().getActiveShell();

	public PageInputFiles(EditorAlignment editorAlignment, TabFolder tabFolder) {
		//
		this.editorAlignment = editorAlignment;
		dataInputEntries = new ArrayList<IDataInputEntry>();
		initialize(tabFolder);
	}

	public void update() {

	}

	public List<IDataInputEntry> getDataInputEntries() {

		return dataInputEntries;
	}

	private void initialize(TabFolder tabFolder) {

		//
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Input Files");
		//
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Results Table
		 */
		inputFilesTable = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		inputFilesTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputFilesTable.setHeaderVisible(true);
		inputFilesTable.setLinesVisible(true);
		/*
		 * Button Bar
		 */
		Composite compositeButtons = new Composite(composite, SWT.NONE);
		compositeButtons.setLayout(new GridLayout(1, true));
		compositeButtons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		//
		GridData gridDataButtons = new GridData(GridData.FILL_HORIZONTAL);
		gridDataButtons.minimumWidth = 150;
		//
		createAddButton(compositeButtons, gridDataButtons);
		createRemoveButton(compositeButtons, gridDataButtons);
		createPreviousButton(compositeButtons, gridDataButtons);
		createNextButton(compositeButtons, gridDataButtons);
		//
		tabItem.setControl(composite);
	}

	private Button createAddButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Add");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ADD, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				int chromatogramType = editorAlignment.getChromatogramType();
				super.widgetSelected(e);
				//
				if(chromatogramType == 0) {
					InputWizardSettings inputWizardSettings = new InputWizardSettings(InputDataType.MSD_CHROMATOGRAM);
					inputWizardSettings.setTitle("MSD Import");
					inputWizardSettings.setDescription("Select the chromatogram(s) to analyze.");
					inputWizardSettings.setPathPreferences(PreferenceSupplier.INSTANCE().getPreferences(), PreferenceSupplier.P_FILTER_PATH_CHROMATOGRAM_MSD);
					//
					InputEntriesWizard inputWizard = new InputEntriesWizard(inputWizardSettings);
					WizardDialog wizardDialog = new WizardDialog(shell, inputWizard);
					wizardDialog.create();
					//
					if(wizardDialog.open() == WizardDialog.OK) {
						IChromatogramWizardElements chromatogramWizardElements = inputWizard.getChromatogramWizardElements();
						List<String> selectedChromatograms = chromatogramWizardElements.getSelectedChromatograms();
						if(selectedChromatograms.size() > 0) {
							/*
							 * If it contains at least 1 element, add it to the input files list.
							 */
							addEntries(selectedChromatograms);
							reloadInputFilesTable();
						}
					}
				} else {
					InputWizardSettings inputWizardSettings = new InputWizardSettings(InputDataType.CSD_CHROMATOGRAM);
					inputWizardSettings.setTitle("CSD Import");
					inputWizardSettings.setDescription("Select the chromatogram(s) to analyze.");
					inputWizardSettings.setPathPreferences(PreferenceSupplier.INSTANCE().getPreferences(), PreferenceSupplier.P_FILTER_PATH_CHROMATOGRAM_CSD);
					//
					InputEntriesWizard inputWizard = new InputEntriesWizard(inputWizardSettings);
					WizardDialog wizardDialog = new WizardDialog(shell, inputWizard);
					wizardDialog.create();
					//
					if(wizardDialog.open() == WizardDialog.OK) {
						IChromatogramWizardElements chromatogramWizardElements = inputWizard.getChromatogramWizardElements();
						List<String> selectedChromatograms = chromatogramWizardElements.getSelectedChromatograms();
						if(selectedChromatograms.size() > 0) {
							/*
							 * If it contains at least 1 element, add it to the input files list.
							 */
							addEntries(selectedChromatograms);
							reloadInputFilesTable();
						}
					}
				}
			}
		});
		return button;
	}

	private Button createRemoveButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Remove");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_DELETE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				messageBox.setText("Remove chromatogram(s)?");
				messageBox.setMessage("Would you like to remove the chromatogram(s)?");
				if(messageBox.open() == SWT.YES) {
					removeEntries(inputFilesTable.getSelectionIndices());
				}
			}
		});
		return button;
	}

	private Button createPreviousButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Previous");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_PREVIOUS, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				editorAlignment.showOverviewFilesPage();
			}
		});
		return button;
	}

	private Button createNextButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Next");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_NEXT, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				editorAlignment.showProcessingWindowsPage();
			}
		});
		return button;
	}

	private void reloadInputFilesTable() {

		if(inputFilesTable != null) {
			/*
			 * Remove all entries.
			 */
			inputFilesTable.removeAll();
			/*
			 * Header
			 */
			String[] titles = {"Filename", "Path"};
			for(int i = 0; i < titles.length; i++) {
				TableColumn column = new TableColumn(inputFilesTable, SWT.NONE);
				column.setText(titles[i]);
			}
			/*
			 * Data
			 */
			for(IDataInputEntry entry : dataInputEntries) {
				TableItem item = new TableItem(inputFilesTable, SWT.NONE);
				item.setText(0, entry.getName());
				item.setText(1, entry.getInputFile());
			}
			/*
			 * Pack to make the entries visible.
			 */
			for(int i = 0; i < titles.length; i++) {
				inputFilesTable.getColumn(i).pack();
			}
		}
	}

	/**
	 * Remove the given entries.
	 * The table need not to be reloaded.
	 * 
	 * @param indices
	 */
	private void removeEntries(int[] indices) {

		if(indices == null || indices.length == 0) {
			return;
		}
		/*
		 * Remove the entries from the UI table.
		 */
		inputFilesTable.remove(indices);
		/*
		 * Remove the entries from the batchProcessJob instance.
		 */
		int counter = 0;
		for(int index : indices) {
			/*
			 * Decrease the index and increase the counter to remove the correct entries.
			 */
			index -= counter;
			dataInputEntries.remove(index);
			counter++;
		}
	}

	private void addEntries(List<String> selectedFiles) {

		IDataInputEntry inputEntry;
		for(String inputFile : selectedFiles) {
			inputEntry = new DataInputEntry(inputFile);
			dataInputEntries.add(inputEntry);
		}
	}
}
