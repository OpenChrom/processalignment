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

import org.eclipse.chemclipse.support.ui.provider.ListContentProvider;
import org.eclipse.chemclipse.support.ui.swt.ExtendedTableViewer;
import org.eclipse.swt.widgets.Composite;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.provider.AlignmentRangeLabelProvider;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.provider.AlignmentRangeTableSorter;

public class AlignmentRangeTableViewerUI extends ExtendedTableViewer {

	public AlignmentRangeTableViewerUI(Composite parent, int style) {
		super(parent, style);
		createColumns();
	}

	private void createColumns() {

		createColumns(IAlignmentRange.TITLES, IAlignmentRange.BOUNDS);
		/*
		 * Set the provider.
		 */
		setLabelProvider(new AlignmentRangeLabelProvider());
		setContentProvider(new ListContentProvider());
		setSorter(new AlignmentRangeTableSorter());
	}
}
