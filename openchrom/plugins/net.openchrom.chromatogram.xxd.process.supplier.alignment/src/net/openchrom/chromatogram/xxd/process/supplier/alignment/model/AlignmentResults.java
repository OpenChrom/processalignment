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

		// iterating over the Ranges
		for(int index = 0; index < this.getAlignmentRanges().size(); index++) {
			IAlignmentRange range = this.getAlignmentRanges().get(index);
			Iterator<IDataInputEntry> entry = this.dataInputEntries.iterator();
			while(entry.hasNext()) {
				IAlignmentResult result = this.alignmentResultMap.get(new Sample(entry.next().getName()));
				Chromatogram ticAfterAlignment = result.getTicAfterAlignment();
				int shift = result.getShifts().get(index);
				int rangeStartScanNumber = result.getTicAfterAlignment().getScanNumber(range.getStartRetentionTime());
				int rangeStopScanNumber = result.getTicAfterAlignment().getScanNumber(range.getStopRetentionTime());
				int totalStopScanNumber = result.getTicAfterAlignment().getScanNumber(result.getTicAfterAlignment().getStopRetentionTime());
				int totalStartScanNumber = result.getTicAfterAlignment().getScanNumber(result.getTicAfterAlignment().getStartRetentionTime());
				if(shift < 0) {
					/*
					 * Left Shift
					 */
					// check if rangeStartScan is closer to totalStartScan than the actual shift
					int scanToShift = rangeStartScanNumber;
					if(rangeStartScanNumber - shift < totalStartScanNumber) {
						scanToShift = totalStartScanNumber + shift;
					}
					while(scanToShift <= rangeStopScanNumber) {
						ticAfterAlignment.getScan(scanToShift - shift).adjustTotalSignal(ticAfterAlignment.getScan(scanToShift).getTotalSignal());
						scanToShift++;
					}
					// copy the rangeStopScan as many times as there are shifts to the left from the end
					for(int copyScanDestination = rangeStopScanNumber - 1; copyScanDestination >= rangeStopScanNumber - shift; copyScanDestination--) {
						ticAfterAlignment.getScan(copyScanDestination).adjustTotalSignal(ticAfterAlignment.getScan(rangeStopScanNumber).getTotalSignal());
					}
				} else if(shift > 0) {
					/*
					 * Right Shift
					 */
					int scanToShift = rangeStopScanNumber;
					if(rangeStopScanNumber + shift > totalStopScanNumber) {
						scanToShift = totalStopScanNumber - shift;
					}
					while(scanToShift >= rangeStartScanNumber) {
						ticAfterAlignment.getScan(scanToShift + shift).adjustTotalSignal(ticAfterAlignment.getScan(scanToShift).getTotalSignal());
						scanToShift--;
					}
					// copy the scan to the left of the shift range as many times as there are shifts to the right
					for(int copyScanDestination = rangeStartScanNumber + 1; copyScanDestination <= rangeStartScanNumber + shift; copyScanDestination++) {
						ticAfterAlignment.getScan(copyScanDestination).adjustTotalSignal(ticAfterAlignment.getScan(rangeStopScanNumber).getTotalSignal());
					}
				}
			}
		}
	}
}