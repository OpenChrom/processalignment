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

import org.eclipse.chemclipse.logging.core.Logger;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentRanges;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRanges;

public class AlignmentSettings implements IAlignmentSettings {


	private static final Logger logger = Logger.getLogger(AlignmentSettings.class);
	//
	private static final int DEFAULT_RETENTION_TIME_WINDOW = 200;
	private static final int DEFAULT_LOWER_RETENTION_TIME_SELECTION = 0;
	private static final int DEFAULT_UPPER_RETENTION_TIME_SELECTION = 900000; // 15 minutes
	private static final int DEFAULT_CHROMATOGRAM_TYPE = 0;
	//
	private int retentionTimeWindow;
	private AlignmentRanges alignmentRanges = new AlignmentRanges();
	private int chromatogramType;

	public AlignmentSettings() {
		this.retentionTimeWindow = DEFAULT_RETENTION_TIME_WINDOW;
		this.chromatogramType = DEFAULT_CHROMATOGRAM_TYPE;
		try {
			AlignmentRange alignmentRange = new AlignmentRange(DEFAULT_LOWER_RETENTION_TIME_SELECTION, DEFAULT_UPPER_RETENTION_TIME_SELECTION);
			this.alignmentRanges.add(alignmentRange);
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	@Override
	public int getRetentionTimeWindow() {

		return retentionTimeWindow;
	}

	@Override
	public IAlignmentRanges getAlignmentRanges() {

		return this.alignmentRanges;
	}

	@Override
	public int getChromatogramType() {

		return chromatogramType;
	}
}
