/*******************************************************************************
 * Copyright (c) 2016, 2017 Lablicate GmbH.
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

import java.util.List;

import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.swt.AlignmentRangeEditorUI;

public class PageProcessingWindows {

	private EditorAlignment editorAlignment;
	private AlignmentRangeEditorUI alignmentRangeEditorUI;

	public PageProcessingWindows(EditorAlignment editorAlignment, TabFolder tabFolder) {
		//
		this.editorAlignment = editorAlignment;
		initialize(tabFolder);
	}

	public void update() {

	}

	public List<IAlignmentRange> getAlignmentRanges() {

		return alignmentRangeEditorUI.getAlignmentRanges();
	}

	private void initialize(TabFolder tabFolder) {

		//
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Processing Windows");
		//
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Results Table
		 */
		Composite compositeTable = new Composite(composite, SWT.NONE);
		compositeTable.setLayout(new GridLayout(1, true));
		compositeTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		alignmentRangeEditorUI = new AlignmentRangeEditorUI(compositeTable, SWT.NONE);
		alignmentRangeEditorUI.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		createPreviousButton(compositeButtons, gridDataButtons);
		createNextButton(compositeButtons, gridDataButtons);
		createProcessButton(compositeButtons, gridDataButtons);
		//
		tabItem.setControl(composite);
	}

	private Button createPreviousButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Previous");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_PREVIOUS, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				editorAlignment.showInputFilesPage();
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

				editorAlignment.showResultsPage();
			}
		});
		return button;
	}

	private Button createProcessButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Process");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EXECUTE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				editorAlignment.calculateAlignment();
				editorAlignment.showResultsPage();
			}
		});
		return button;
	}
}
