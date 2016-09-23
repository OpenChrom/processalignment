/*******************************************************************************
 * Copyright (c) 2013, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.chemclipse.model.implementation.Chromatogram;

public class AlignmentResults implements IAlignmentResults {

	private List<IDataInputEntry> dataInputEntries;
	private int retentionTimeWindow;
	private Map<ISample, IAlignmentResult> alignmentResultMap;
	private IAlignmentRanges ranges;
	//

	public AlignmentResults() {
		this(new ArrayList<IDataInputEntry>());
	}

	public AlignmentResults(List<IDataInputEntry> dataInputEntries) {
		this.dataInputEntries = dataInputEntries;
		alignmentResultMap = new HashMap<ISample, IAlignmentResult>();
		for(IDataInputEntry entry : dataInputEntries) {
			AlignmentResult alignmentResult = new AlignmentResult();
			alignmentResultMap.put(new Sample(entry.getName()), alignmentResult);
		}
	}

	@Override
	public List<IDataInputEntry> getDataInputEntries() {

		return dataInputEntries;
	}

	@Override
	public int getRetentionTimeWindow() {

		return retentionTimeWindow;
	}

	@Override
	public void setRetentionTimeWindow(int retentionTimeWindow) {

		this.retentionTimeWindow = retentionTimeWindow;
	}

	@Override
	public Map<ISample, IAlignmentResult> getAlignmentResultMap() {

		return alignmentResultMap;
	}

	@Override
	public void setAlignmentRanges(IAlignmentRanges ranges) {

	}

	@Override
	public IAlignmentRanges getAlignmentRanges() {

		return this.ranges;
	}

	void applyShiftToPreviews() {

		for(int index = 0; index < this.getAlignmentRanges().getAlignmentRanges().size(); index++) {
			IAlignmentRange range = this.getAlignmentRanges().getAlignmentRanges().get(index);
			Iterator<IDataInputEntry> entry = this.dataInputEntries.iterator();
			IAlignmentResult result = this.alignmentResultMap.get(new Sample(entry.next().getName()));
			// TODO Here the actual shifting has to be implemented now
		}
	}
}