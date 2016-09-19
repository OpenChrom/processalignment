/*******************************************************************************
 * Copyright (c) 2016 loge.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * loge - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import java.util.List;

public interface IAlignmentRanges {

	void addAlignmentRange(IAlignmentRange range);

	void removeAlignmentRange(IAlignmentRange range);

	IAlignmentRange getAlignmentRange(int i);

	List<IAlignmentRange> getAlignmentRanges();

	int size();

	int getLowestStartRetentionTime();

	int getHighestStopRetentionTime();
}
