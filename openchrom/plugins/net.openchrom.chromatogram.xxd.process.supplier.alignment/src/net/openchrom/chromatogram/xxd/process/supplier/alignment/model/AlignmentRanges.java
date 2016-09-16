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

import java.util.ArrayList;
import java.util.List;

public class AlignmentRanges implements IAlignmentRanges {

	private List<IAlignmentRange> ranges;

	/**
	 * Initialize mass spectra and create a new internal mass spectra list.
	 */
	public AlignmentRanges() {
		ranges = new ArrayList<IAlignmentRange>();
	}

	@Override
	public void addAlignmentRange(IAlignmentRange range) {

		if(range != null) {
			ranges.add(range);
		}
	}

	@Override
	public void removeAlignmentRange(IAlignmentRange range) {

		if(range != null) {
			ranges.remove(range);
		}
	}

	@Override
	public IAlignmentRange getAlignmentRange(int i) {

		IAlignmentRange range = null;
		if(i > 0 && i <= ranges.size()) {
			range = ranges.get(--i);
		}
		return range;
	}

	@Override
	public List<IAlignmentRange> getAlignmentRanges() {

		return ranges;
	}

	@Override
	public int size() {

		return ranges.size();
	}
}
