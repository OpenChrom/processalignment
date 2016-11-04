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

import java.util.List;

import org.eclipse.chemclipse.model.selection.IChromatogramSelection;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.swt.ui.components.chromatogram.MultipleChromatogramOffsetUI;
import org.eclipse.chemclipse.swt.ui.support.AxisTitlesIntensityScale;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class PageResults {

	private EditorAlignment editorAlignment;
	//
	private MultipleChromatogramOffsetUI chromatogramOverlayRawData;
	private MultipleChromatogramOffsetUI chromatogramOverlayShiftedData;

	public PageResults(EditorAlignment editorAlignment, TabFolder tabFolder) {
		//
		this.editorAlignment = editorAlignment;
		initialize(tabFolder);
	}

	public void update() {

	}

	public void setChromatogramData(List<IChromatogramSelection> chromatogramSelectionsRaw, List<IChromatogramSelection> chromatogramSelectionsShifted) {

		chromatogramOverlayRawData.updateSelection(chromatogramSelectionsRaw, true);
		chromatogramOverlayShiftedData.updateSelection(chromatogramSelectionsShifted, true);
	}

	private void initialize(TabFolder tabFolder) {

		//
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Processing Results");
		//
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Chromatogram Compare Views
		 */
		Composite compositeChromatograms = new Composite(composite, SWT.NONE);
		compositeChromatograms.setLayout(new GridLayout(1, true));
		compositeChromatograms.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		Composite compositeRawData = new Composite(compositeChromatograms, SWT.BORDER);
		compositeRawData.setLayoutData(new GridData(GridData.FILL_BOTH));
		compositeRawData.setLayout(new FillLayout());
		chromatogramOverlayRawData = new MultipleChromatogramOffsetUI(compositeRawData, SWT.NONE, new AxisTitlesIntensityScale());
		//
		Composite compositeShiftedData = new Composite(compositeChromatograms, SWT.BORDER);
		compositeShiftedData.setLayoutData(new GridData(GridData.FILL_BOTH));
		compositeShiftedData.setLayout(new FillLayout());
		chromatogramOverlayShiftedData = new MultipleChromatogramOffsetUI(compositeShiftedData, SWT.NONE, new AxisTitlesIntensityScale());
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
		createSaveButton(compositeButtons, gridDataButtons);
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

				editorAlignment.showProcessingWindowsPage();
			}
		});
		return button;
	}

	private Button createSaveButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Save All");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_SAVEALL, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				editorAlignment.applyAlignment();

				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				messageBox.setText("Shift chromatogram(s)?");
				messageBox.setMessage("Save the shifted chromatogram file(s)?");
				if(messageBox.open() == SWT.OK) {
					System.out.println("Save the chromatograms");
					
				}
			}
		});
		return button;
	}
}
