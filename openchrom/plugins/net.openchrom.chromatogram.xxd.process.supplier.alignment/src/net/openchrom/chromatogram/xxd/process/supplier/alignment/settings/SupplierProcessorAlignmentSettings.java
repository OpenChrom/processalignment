/*******************************************************************************
 * Copyright (c) 2016 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lorenz - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.settings;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentRanges;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRanges;

public class SupplierProcessorAlignmentSettings implements ISupplierProcessorAlignmentSettings {

	private static final int DEFAULT_RETENTION_TIME_WINDOW_MILLISECONDS = 200;
	private static final int DEFAULT_LOWER_RETENTION_TIME_SELECTION_MINUTES = 0;
	private static final int DEFAULT_UPPER_RETENTION_TIME_SELECTION_MINUTES = 15;
	private int retentionTimeWindow;
	private AlignmentRanges ranges = new AlignmentRanges();

	public SupplierProcessorAlignmentSettings() {
		this.retentionTimeWindow = DEFAULT_RETENTION_TIME_WINDOW_MILLISECONDS;
		AlignmentRange range;
		try {
			range = new AlignmentRange(DEFAULT_LOWER_RETENTION_TIME_SELECTION_MINUTES, DEFAULT_UPPER_RETENTION_TIME_SELECTION_MINUTES);
			this.ranges.addAlignmentRange(range);
		} catch(Exception e) {
		}
	}

	public int getRetentionTimeWindow() {

		return retentionTimeWindow;
	}

	public IAlignmentRanges getAlignmentRangesList() {

		return this.ranges;
	}
}
