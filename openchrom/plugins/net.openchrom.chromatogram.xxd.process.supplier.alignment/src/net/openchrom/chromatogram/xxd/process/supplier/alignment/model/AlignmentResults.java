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
		this.ranges = new AlignmentRanges();
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

		this.ranges = ranges;
	}

	@Override
	public IAlignmentRanges getAlignmentRanges() {

		return this.ranges;
	}

	public void applyShiftToPreviews() {

		for(int index = 0; index < this.getAlignmentRanges().size(); index++) {
			IAlignmentRange range = this.getAlignmentRanges().get(index);
			Iterator<IDataInputEntry> entry = this.dataInputEntries.iterator();
			IAlignmentResult result = this.alignmentResultMap.get(new Sample(entry.next().getName()));
			Chromatogram ticAfterAlignment = result.getTicAfterAlignment();
			int shift = result.getShifts().get(index);
			int rangeStartScanNumber = result.getTicAfterAlignment().getScanNumber(range.getStartRetentionTime());
			int rangeStopScanNumber = result.getTicAfterAlignment().getScanNumber(range.getStopRetentionTime());
			int totalStopScanNumber = result.getTicAfterAlignment().getScanNumber(result.getTicAfterAlignment().getStopRetentionTime());
			if(shift < 0) {
				if(rangeStartScanNumber - shift < 0) {
					// shifting will 'fall out' of chromatogram in the beginning
				} else {
					// normal shift to the left
				}
			} else if(shift > 0) {
				if(rangeStopScanNumber + shift > totalStopScanNumber) {
					// shifting will 'fall out' of the chromatogram at the end
				} else {
					// normal shift to the right
					// TODO need to loop over each sample also
					for(int scanToShift = rangeStopScanNumber; scanToShift >= rangeStopScanNumber; scanToShift--) {
						ticAfterAlignment.getScan(scanToShift + shift).adjustTotalSignal(ticAfterAlignment.getScan(scanToShift).getTotalSignal());
					}
					// copy the scan to the right of the shift range as many times as there are shifts to the left
					for(int copyScanDestination = rangeStopScanNumber; copyScanDestination >= rangeStopScanNumber - shift; copyScanDestination--) {
						ticAfterAlignment.getScan(copyScanDestination).adjustTotalSignal(ticAfterAlignment.getScan(rangeStopScanNumber + 1).getTotalSignal());
					}
				}
			}
		}
	}
}