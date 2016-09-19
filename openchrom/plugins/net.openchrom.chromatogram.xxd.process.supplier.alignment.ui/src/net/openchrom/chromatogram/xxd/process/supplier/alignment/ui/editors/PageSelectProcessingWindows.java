/*******************************************************************************
 * Copyright (c) 2016 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lorenz Gerber - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResults;

public class PageSelectProcessingWindows {

	private EditorAlignment editorAlignment;
	private Table peakListIntensityTable;

	public PageSelectProcessingWindows(EditorAlignment editorAlignment, TabFolder tabFolder, FormToolkit formToolkit) {
		//
		this.editorAlignment = editorAlignment;
		initialize(tabFolder, formToolkit);
	}

	public void update() {

	}

	private void initialize(TabFolder tabFolder, FormToolkit formToolkit) {

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Select Processing Window");
		//
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());
		/*
		 * Composite parent = new Composite(composite, SWT.NONE);
		 * parent.setLayout(new GridLayout(1, true));
		 * parent.setLayoutData(GridData.FILL_BOTH);
		 */
		/*
		 * Forms API
		 */
		formToolkit = new FormToolkit(composite.getDisplay());
		ScrolledForm scrolledForm = formToolkit.createScrolledForm(composite);
		Composite scrolledFormComposite = scrolledForm.getBody();
		formToolkit.decorateFormHeading(scrolledForm.getForm());
		scrolledFormComposite.setLayout(new TableWrapLayout());
		scrolledForm.setText("Choose Processing Windows");
		/*
		 * Add sections
		 */
		createTicOverlaySection(scrolledFormComposite, formToolkit);
		createProcessingTableSection(scrolledFormComposite, formToolkit);
		tabItem.setControl(composite);
	}

	private void createTicOverlaySection(Composite parent, FormToolkit formToolkit) {

		/*
		 * Section
		 */
		Section section = formToolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Properties");
		section.setDescription("Use the properties to define the retention time window.");
		section.marginWidth = 5;
		section.marginHeight = 5;
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
	}

	private void createProcessingTableSection(Composite parent, FormToolkit formToolkit) {

		Section section;
		Composite client;
		GridLayout layout;
		/*
		 * Section
		 */
		section = formToolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Evaluation");
		section.setDescription("Run the alignment evaluation after the entries have been edited.");
		section.marginWidth = 5;
		section.marginHeight = 5;
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		/*
		 * Set the layout for the client.
		 */
		client = formToolkit.createComposite(section, SWT.WRAP);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		GridData gridData;
		peakListIntensityTable = formToolkit.createTable(client, SWT.MULTI | SWT.VIRTUAL | SWT.CHECK);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 300;
		gridData.widthHint = 100;
		gridData.verticalSpan = 3;
		peakListIntensityTable.setLayoutData(gridData);
		peakListIntensityTable.setHeaderVisible(true);
		peakListIntensityTable.setLinesVisible(true);
		peakListIntensityTable.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {

				TableItem[] selection = peakListIntensityTable.getSelection();
				for(int i = 0; i < selection.length; i++) {
					selection[i].dispose();
				}
			}
		});
		peakListIntensityTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {

				IAlignmentResults alignmentResults = editorAlignment.getAlignmentResults();
				//
			}
		});
	}
}
