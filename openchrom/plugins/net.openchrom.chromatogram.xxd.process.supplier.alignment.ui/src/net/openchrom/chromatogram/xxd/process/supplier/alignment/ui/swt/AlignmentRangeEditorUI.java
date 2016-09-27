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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.AbstractChromatogram;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.AlignmentSettings;

public class AlignmentRangeEditorUI extends Composite {

	private static final Logger logger = Logger.getLogger(AlignmentRangeEditorUI.class);
	//
	private static final String ACTION_INITIALIZE = "ACTION_INITIALIZE";
	private static final String ACTION_CANCEL = "ACTION_CANCEL";
	private static final String ACTION_DELETE = "ACTION_DELETE";
	private static final String ACTION_ADD = "ACTION_ADD";
	private static final String ACTION_SELECT = "ACTION_SELECT";
	//
	private Button buttonCancel;
	private Button buttonDelete;
	private Button buttonAdd;
	//
	private Button buttonAddAlignmentRange;
	private Text textStartRetentionTime;
	private Text textStopRetentionTime;
	//
	private AlignmentRangeTableViewerUI alignmentRangeTableViewerUI;
	//
	private List<IAlignmentRange> alignmentRanges;

	public AlignmentRangeEditorUI(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	public void setInput(List<IAlignmentRange> alignmentRanges) {

		this.alignmentRanges = alignmentRanges;
		alignmentRangeTableViewerUI.setInput(alignmentRanges);
	}

	public List<IAlignmentRange> getAlignmentRanges() {

		return alignmentRanges;
	}

	public AlignmentRangeTableViewerUI getAlignmentRangeTableViewerUI() {

		return alignmentRangeTableViewerUI;
	}

	private void initialize() {

		setLayout(new FillLayout());
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		//
		// alignmentRanges = new ArrayList<IAlignmentRange>(); // default list
		alignmentRanges = new AlignmentSettings().getAlignmentRanges();
		//
		createButtonField(composite);
		createAddRangeField(composite);
		createTableField(composite);
		//
		enableButtonFields(ACTION_INITIALIZE);
	}

	private void createButtonField(Composite composite) {

		Label label = new Label(composite, SWT.NONE);
		label.setText("");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;
		label.setLayoutData(gridData);
		/*
		 * Buttons
		 */
		Composite compositeButtons = new Composite(composite, SWT.NONE);
		compositeButtons.setLayout(new GridLayout(3, true));
		GridData gridDataComposite = new GridData();
		gridDataComposite.horizontalAlignment = SWT.RIGHT;
		compositeButtons.setLayoutData(gridDataComposite);
		//
		buttonCancel = new Button(compositeButtons, SWT.PUSH);
		buttonCancel.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CANCEL, IApplicationImage.SIZE_16x16));
		buttonCancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				textStartRetentionTime.setText("");
				textStopRetentionTime.setText("");
				enableButtonFields(ACTION_CANCEL);
			}
		});
		//
		buttonDelete = new Button(compositeButtons, SWT.PUSH);
		buttonDelete.setEnabled(false);
		buttonDelete.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_DELETE, IApplicationImage.SIZE_16x16));
		buttonDelete.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				Table table = alignmentRangeTableViewerUI.getTable();
				int index = table.getSelectionIndex();
				if(index >= 0) {
					MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
					messageBox.setText("Delete range(s)?");
					messageBox.setMessage("Would you like to delete the range(s)?");
					if(messageBox.open() == SWT.OK) {
						//
						enableButtonFields(ACTION_DELETE);
						TableItem[] tableItems = table.getSelection();
						for(TableItem tableItem : tableItems) {
							Object object = tableItem.getData();
							if(object instanceof IAlignmentRange) {
								IAlignmentRange alignmentRange = (IAlignmentRange)object;
								alignmentRanges.remove(alignmentRange);
							}
						}
						alignmentRangeTableViewerUI.setInput(alignmentRanges);
					}
				}
			}
		});
		//
		buttonAdd = new Button(compositeButtons, SWT.PUSH);
		buttonAdd.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ADD, IApplicationImage.SIZE_16x16));
		buttonAdd.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				enableButtonFields(ACTION_ADD);
			}
		});
	}

	private void createAddRangeField(Composite composite) {

		Label labelRetentionTime = new Label(composite, SWT.NONE);
		labelRetentionTime.setText("Start RT (min):");
		//
		textStartRetentionTime = new Text(composite, SWT.BORDER);
		textStartRetentionTime.setText("");
		textStartRetentionTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		Label labelRetentionIndex = new Label(composite, SWT.NONE);
		labelRetentionIndex.setText("Stop RT (min):");
		//
		textStopRetentionTime = new Text(composite, SWT.BORDER);
		textStopRetentionTime.setText("");
		textStopRetentionTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		buttonAddAlignmentRange = new Button(composite, SWT.PUSH);
		buttonAddAlignmentRange.setText("");
		buttonAddAlignmentRange.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EXECUTE_ADD, IApplicationImage.SIZE_16x16));
		buttonAddAlignmentRange.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonAddAlignmentRange.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				try {
					enableButtonFields(ACTION_INITIALIZE);
					//
					int startRetentionTime = (int)(Double.parseDouble(textStartRetentionTime.getText().trim()) * AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
					int stopRetentionTime = (int)(Double.parseDouble(textStopRetentionTime.getText().trim()) * AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
					//
					textStartRetentionTime.setText("");
					textStopRetentionTime.setText("");
					//
					IAlignmentRange alignmentRange = new AlignmentRange(startRetentionTime, stopRetentionTime);
					alignmentRanges.add(alignmentRange);
					alignmentRangeTableViewerUI.setInput(alignmentRanges);
				} catch(Exception e1) {
					logger.warn(e1);
				}
			}
		});
	}

	private void createTableField(Composite composite) {

		alignmentRangeTableViewerUI = new AlignmentRangeTableViewerUI(composite, SWT.BORDER | SWT.MULTI);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 5;
		alignmentRangeTableViewerUI.getTable().setLayoutData(gridData);
		alignmentRangeTableViewerUI.getTable().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				enableButtonFields(ACTION_SELECT);
			}
		});
	}

	private void enableButtonFields(String action) {

		enableFields(false);
		switch(action) {
			case ACTION_INITIALIZE:
				buttonAdd.setEnabled(true);
				break;
			case ACTION_CANCEL:
				buttonAdd.setEnabled(true);
				break;
			case ACTION_DELETE:
				buttonAdd.setEnabled(true);
				break;
			case ACTION_ADD:
				buttonCancel.setEnabled(true);
				textStartRetentionTime.setEnabled(true);
				textStopRetentionTime.setEnabled(true);
				buttonAddAlignmentRange.setEnabled(true);
				break;
			case ACTION_SELECT:
				buttonAdd.setEnabled(true);
				if(alignmentRangeTableViewerUI.getTable().getSelectionIndex() >= 0) {
					buttonDelete.setEnabled(true);
				} else {
					buttonDelete.setEnabled(false);
				}
				break;
		}
	}

	private void enableFields(boolean enabled) {

		buttonCancel.setEnabled(enabled);
		buttonDelete.setEnabled(enabled);
		buttonAdd.setEnabled(enabled);
		//
		textStartRetentionTime.setEnabled(enabled);
		textStopRetentionTime.setEnabled(enabled);
		buttonAddAlignmentRange.setEnabled(enabled);
	}
}
