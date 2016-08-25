/*******************************************************************************
 * Copyright (c) 2016 Lablicate GmbH.
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

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class PageOverview {

	private static final Logger logger = Logger.getLogger(PageOverview.class);
	private static final int DEFAULT_RETENTION_TIME_WINDOW = 200;
	//
	private EditorAlignment editorAlignment;
	private Text retentionTimeWindowText;
	private int chromatogramType;

	public PageOverview(EditorAlignment pcaEditor, TabFolder tabFolder, FormToolkit formToolkit) {
		//
		this.editorAlignment = pcaEditor;
		initialize(tabFolder, formToolkit);
	}

	private void initialize(TabFolder tabFolder, FormToolkit formToolkit) {

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Overview");
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());
		/*
		 * Forms API
		 */
		formToolkit = new FormToolkit(composite.getDisplay());
		ScrolledForm scrolledForm = formToolkit.createScrolledForm(composite);
		Composite scrolledFormComposite = scrolledForm.getBody();
		formToolkit.decorateFormHeading(scrolledForm.getForm());
		scrolledFormComposite.setLayout(new TableWrapLayout());
		scrolledForm.setText("Chromatogram Alignment");
		/*
		 * Add the sections
		 */
		createPropertiesSection(scrolledFormComposite, formToolkit);
		createExecuteSection(scrolledFormComposite, formToolkit);
		//
		tabItem.setControl(composite);
	}

	public int getRetentionTimeWindow() {

		int retentionTimeWindow = DEFAULT_RETENTION_TIME_WINDOW;
		try {
			retentionTimeWindow = Integer.parseInt(retentionTimeWindowText.getText().trim());
		} catch(NumberFormatException e) {
			logger.warn(e);
		}
		return retentionTimeWindow;
	}
	
	public int getChromatogramType() {

		return chromatogramType;
	}

	/**
	 * Creates the properties section.
	 */
	private void createPropertiesSection(Composite parent, FormToolkit formToolkit) {

		/*
		 * Section
		 */
		Section section = formToolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Properties");
		section.setDescription("Use the properties to define the retention time window.");
		section.marginWidth = 5;
		section.marginHeight = 5;
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		/*
		 * Client
		 */
		Composite client = formToolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		Label label = formToolkit.createLabel(client, "Select the Alignment settings:");
		label.setLayoutData(gridData);
		/*
		 * Settings
		 */
		createRetentionTimeWindowText(client, formToolkit);
		createChromatogramTypeButtons(client, formToolkit);
		/*
		 * Add the client to the section and paint flat borders.
		 */
		section.setClient(client);
		formToolkit.paintBordersFor(client);
	}

	private void createRetentionTimeWindowText(Composite client, FormToolkit formToolkit) {

		formToolkit.createLabel(client, "Retention Time Window (milliseconds)");
		//
		retentionTimeWindowText = formToolkit.createText(client, Integer.toString(DEFAULT_RETENTION_TIME_WINDOW), SWT.NONE);
		//
		GridData gridData = new GridData();
		gridData.widthHint = 300;
		retentionTimeWindowText.setLayoutData(gridData);
	}
	
	private void createChromatogramTypeButtons(Composite client, FormToolkit formToolkit) {

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.heightHint = 30;
		/*
		 * Chromatogram type radio buttons.
		 */
		Label radioLabels = formToolkit.createLabel(client, "Select the Chromatogram type:");
		radioLabels.setLayoutData(gridData);
		SelectionListener selectionListener = new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {

				Button button = ((Button)event.widget);
				if(button.getText().equals("MSD")) {
					chromatogramType = 0;
				} else {
					chromatogramType = 1;
				}
			};
		};
		Button[] radioButtons = new Button[2];
		//
		radioButtons[0] = new Button(client, SWT.RADIO);
		radioButtons[0].setSelection(true);
		radioButtons[0].setText("MSD");
		radioButtons[0].setLayoutData(gridData);
		radioButtons[0].addSelectionListener(selectionListener);
		//
		radioButtons[1] = new Button(client, SWT.RADIO);
		radioButtons[1].setText("CSD");
		radioButtons[1].setLayoutData(gridData);
		radioButtons[1].addSelectionListener(selectionListener);
	}
	

	/**
	 * Creates the run section.
	 * 
	 * @param parent
	 */
	private void createExecuteSection(Composite parent, FormToolkit formToolkit) {

		Label label;
		/*
		 * Section
		 */
		Section section = formToolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Evaluation");
		section.setDescription("Run the alignment evaluation after the entries have been edited.");
		section.marginWidth = 5;
		section.marginHeight = 5;
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		/*
		 * Client
		 */
		Composite client = formToolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = 20;
		gridData.heightHint = 30;
		/*
		 * Input files section.
		 */
		label = formToolkit.createLabel(client, "Select the input chromatograms:\n");
		label.setLayoutData(gridData);
		createInputFilesPageHyperlink(client, gridData, formToolkit);
		/*
		 * Add the client to the section and paint flat borders.
		 */
		section.setClient(client);
		formToolkit.paintBordersFor(client);
	}

	private void createInputFilesPageHyperlink(Composite client, GridData gridData, FormToolkit formToolkit) {

		ImageHyperlink imageHyperlink;
		/*
		 * Settings
		 */
		imageHyperlink = formToolkit.createImageHyperlink(client, SWT.NONE);
		imageHyperlink.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CONFIGURE, IApplicationImage.SIZE_16x16));
		imageHyperlink.setText("Data Input Files");
		imageHyperlink.setLayoutData(gridData);
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

			public void linkActivated(HyperlinkEvent e) {

				editorAlignment.showInputFilesPage();
			}
		});
	}
}
