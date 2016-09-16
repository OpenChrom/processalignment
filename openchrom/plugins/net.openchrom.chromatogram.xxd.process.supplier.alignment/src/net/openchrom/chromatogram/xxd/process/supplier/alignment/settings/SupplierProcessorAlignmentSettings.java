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

public class SupplierProcessorAlignmentSettings implements ISupplierProcessorAlignmentSettings {

	private static final int DEFAULT_RETENTION_TIME_WINDOW_MILLISECONDS = 200;
	private static final int DEFAULT_LOWER_RETENTION_TIME_SELECTION_MINUTES = 0;
	private static final int DEFAULT_UPPER_RETENTION_TIME_SELECTION_MINUTES = 15;
	private int retentionTimeWindow = DEFAULT_RETENTION_TIME_WINDOW_MILLISECONDS;
	private int lowerRetentionTimeSelection = DEFAULT_LOWER_RETENTION_TIME_SELECTION_MINUTES;
	private int upperRetentionTimeSelection = DEFAULT_UPPER_RETENTION_TIME_SELECTION_MINUTES;

	public SupplierProcessorAlignmentSettings() {
	}

	public int getRetentionTimeWindow() {

		return retentionTimeWindow;
	}

	public int getLowerRetentionTimeSelection() {

		return lowerRetentionTimeSelection;
	}

	public int getUpperRetentionTimeSelection() {

		return upperRetentionTimeSelection;
	}
}
