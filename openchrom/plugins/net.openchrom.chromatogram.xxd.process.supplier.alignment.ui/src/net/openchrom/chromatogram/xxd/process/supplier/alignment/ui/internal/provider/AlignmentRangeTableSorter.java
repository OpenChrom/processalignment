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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.provider;

import org.eclipse.chemclipse.support.ui.swt.EnhancedViewerSorter;
import org.eclipse.jface.viewers.Viewer;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRange;

public class AlignmentRangeTableSorter extends EnhancedViewerSorter {

	public static final int ASCENDING = 0;
	//
	private int propertyIndex;
	private int direction = ASCENDING;

	public AlignmentRangeTableSorter() {
		propertyIndex = 0;
		direction = ASCENDING;
	}

	public void setColumn(int column) {

		if(column == this.propertyIndex) {
			// Toggle the direction
			direction = 1 - direction;
		} else {
			this.propertyIndex = column;
			direction = ASCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		int sortOrder = 0;
		if(e1 instanceof IAlignmentRange && e2 instanceof IAlignmentRange) {
			IAlignmentRange modelStandard1 = (IAlignmentRange)e1;
			IAlignmentRange modelStandard2 = (IAlignmentRange)e2;
			switch(propertyIndex) {
				case 0:
					sortOrder = Integer.compare(modelStandard2.getStartRetentionTime(), modelStandard1.getStartRetentionTime());
					break;
				case 1:
					sortOrder = Integer.compare(modelStandard2.getStopRetentionTime(), modelStandard1.getStopRetentionTime());
					break;
				default:
					sortOrder = 0;
			}
		}
		if(direction == ASCENDING) {
			sortOrder = -sortOrder;
		}
		return sortOrder;
	}
}
