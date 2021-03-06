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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import java.util.List;
import java.util.Map;

public interface IAlignmentResults {

	List<IDataInputEntry> getDataInputEntries();

	int getRetentionTimeWindow();

	void setRetentionTimeWindow(int retentionTimeWindow);

	Map<ISample, IAlignmentResult> getAlignmentResultMap();

	void setAlignmentRanges(IAlignmentRanges ranges);

	IAlignmentRanges getAlignmentRanges();

	void applyShiftToPreviews();
}
