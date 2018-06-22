/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 * Lorenz Gerber - main functionality
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.chemclipse.csd.converter.chromatogram.ChromatogramConverterCSD;
import org.eclipse.chemclipse.csd.model.core.selection.ChromatogramSelectionCSD;
import org.eclipse.chemclipse.csd.model.core.selection.IChromatogramSelectionCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.exceptions.ChromatogramIsNullException;
import org.eclipse.chemclipse.model.implementation.Chromatogram;
import org.eclipse.chemclipse.model.implementation.Scan;
import org.eclipse.chemclipse.model.signals.ITotalScanSignal;
import org.eclipse.chemclipse.model.signals.ITotalScanSignalExtractor;
import org.eclipse.chemclipse.model.signals.ITotalScanSignals;
import org.eclipse.chemclipse.model.signals.TotalScanSignalExtractor;
import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ejml.simple.SimpleMatrix;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResult;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.Sample;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.IAlignmentSettings;

public class AlignmentProcessor {

	private static final Logger logger = Logger.getLogger(AlignmentProcessor.class);
	private static final int MAX_SHIFT = 10;

	public AlignmentResults calculateAlignment(List<IDataInputEntry> dataInputEntries, IAlignmentSettings alignmentSettings, IProgressMonitor monitor) {

		/*
		 * Preparing environment
		 */
		AlignmentResults alignmentResults = new AlignmentResults(dataInputEntries);
		alignmentResults.setRetentionTimeWindow(alignmentSettings.getRetentionTimeWindow());
		alignmentResults.setAlignmentRanges(alignmentSettings.getAlignmentRanges());
		int retentionTimeWindow = alignmentSettings.getRetentionTimeWindow();
		int chromatogramType = alignmentSettings.getChromatogramType();
		/*
		 * Find lowest and highest Scans over the whole chromatogram set
		 */
		// IProcessingInfo processingInfo = new ProcessingInfo();
		int highestRetentionTime = 0;
		int lowestRetentionTime = 0;
		/*
		 * get TIC Data
		 */
		List<ITotalScanSignals> alignmentTicsList = new ArrayList<ITotalScanSignals>();
		loadData(alignmentTicsList, dataInputEntries, chromatogramType, monitor);
		highestRetentionTime = findHighestRetentionTime(alignmentTicsList);
		lowestRetentionTime = findLowestRetentionTime(alignmentTicsList);
		/*
		 * Calculate two sets of standardized chromatograms
		 */
		List<IChromatogram<? extends IPeak>> standardizedTICsBefore = null;
		standardizedTICsBefore = standardizeChromatograms(alignmentTicsList, retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
		List<IChromatogram<? extends IPeak>> standardizedTICsAfter = null;
		standardizedTICsAfter = standardizeChromatograms(alignmentTicsList, retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
		/*
		 * store standardized TIC chromatograms in results
		 */
		Iterator<IChromatogram<? extends IPeak>> chromatogramBeforeIterator = standardizedTICsBefore.iterator();
		Iterator<IChromatogram<? extends IPeak>> chromatogramAfterIterator = standardizedTICsAfter.iterator();
		Iterator<IDataInputEntry> entryIterator = dataInputEntries.iterator();
		// Iterator<File> fileIterator = inputFiles.iterator();
		while(chromatogramBeforeIterator.hasNext() && entryIterator.hasNext()) {
			IAlignmentResult currentResult = alignmentResults.getAlignmentResultMap().get(new Sample(entryIterator.next().getName()));
			currentResult.setTicBeforeAlignment(chromatogramBeforeIterator.next());
			currentResult.setTicAfterAlignment(chromatogramAfterIterator.next());
		}
		/*
		 * Iterate over alignment Ranges
		 */
		Iterator<IAlignmentRange> range = alignmentSettings.getAlignmentRanges().iterator();
		while(range.hasNext()) {
			// get current Range to calculate
			IAlignmentRange currentRange = range.next();
			int lowerRetentionTimeSelection = currentRange.getStartRetentionTime();
			int upperRetentionTimeSelection = currentRange.getStopRetentionTime();
			/*
			 * Calculate standardized chromatograms
			 */
			List<IChromatogram<? extends IPeak>> standardizedChromatograms = null;
			standardizedChromatograms = standardizeChromatograms(alignmentTicsList, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection);
			/*
			 * Calculate sample TIC matrix
			 */
			double[][] sampleTics = composeSampleTics(standardizedChromatograms);
			SimpleMatrix sampleTicsMatrix = new SimpleMatrix(sampleTics);
			sampleTicsMatrix = sampleTicsMatrix.transpose();
			/*
			 * Calculate averaged sample
			 */
			double[] averageSample = calculateAverageSample(standardizedChromatograms);
			/*
			 * calculate shifted TICs of averaged sample
			 */
			double[][] targetTics = composeTargetTics(averageSample);
			/*
			 * calculate shift matrix
			 */
			SimpleMatrix targetTicsMatrix = new SimpleMatrix(targetTics);
			SimpleMatrix matrixShiftResults = new SimpleMatrix(targetTicsMatrix.mult(sampleTicsMatrix));
			int[] columnMaximumIndices = calculateColumnMaximumIndices(matrixShiftResults);
			/*
			 * store shift values in AlignmentResult of each chromatogram/sample
			 */
			Iterator<IDataInputEntry> entry = dataInputEntries.listIterator();
			int shiftIndex = 0;
			while(entry.hasNext()) {
				IAlignmentResult alignmentResult = alignmentResults.getAlignmentResultMap().get(new Sample(entry.next().getName()));
				Integer shift = columnMaximumIndices[shiftIndex];
				alignmentResult.addShift(shift);
				shiftIndex++;
			}
		}
		alignmentResults.applyShiftToPreviews();
		return alignmentResults;
	}

	/**
	 * 
	 * This is so far just a stub. This will be the function to be called for applying the actual alignment
	 * 
	 * @param results
	 * @param settings
	 * @param monitor
	 * @return
	 */
	public void applyAlignment(List<IDataInputEntry> dataInputEntries, IAlignmentResults results, IAlignmentSettings settings, IProgressMonitor monitor) {

		int chromatogramType = settings.getChromatogramType();
		IProcessingInfo processingInfo = new ProcessingInfo();
		/*
		 * apply shift to files and export
		 */
		if(chromatogramType == 0) {
			// Loop through each file
			for(IDataInputEntry entry : dataInputEntries) {
				// Open file
				IProcessingInfo importProcessingInfo = ChromatogramConverterMSD.convert(new File(entry.getInputFile()), monitor);
				IChromatogram chromatogram = importProcessingInfo.getProcessingResult(IChromatogramMSD.class);
				// Loop through each alignmentRange
				int rangeCounter = 0;
				for(IAlignmentRange range : settings.getAlignmentRanges()) {
					int startShift = range.getStartRetentionTime();
					int endShift = range.getStopRetentionTime();
					int shift = results.getAlignmentResultMap().get(new Sample(entry.getName())).getShifts().get(rangeCounter);
					rangeCounter++;
					// make adjustment
					if(shift < 0) {
						chromatogram.removeScans(chromatogram.getScanNumber(startShift), chromatogram.getScanNumber(startShift + Math.abs(shift)));
						int scanToShift = chromatogram.getScanNumber(startShift);
						int lastShift = chromatogram.getScanNumber(endShift);
						while(scanToShift < lastShift) {
							chromatogram.getScan(scanToShift).setRetentionTime(chromatogram.getScan(scanToShift).getRetentionTime() + shift);
							scanToShift++;
						}
					} else if(shift > 0) {
						chromatogram.removeScans(chromatogram.getScanNumber(endShift - shift), chromatogram.getScanNumber(endShift));
						int scanToShift = chromatogram.getScanNumber(endShift);
						int lastShift = chromatogram.getScanNumber(startShift);
						while(scanToShift > lastShift) {
							chromatogram.getScan(scanToShift).setRetentionTime(chromatogram.getScan(scanToShift).getRetentionTime() + shift);
							scanToShift--;
						}
					}
				}
				// write/export file back
				ChromatogramConverterMSD.convert(new File(entry.getInputFile()), (IChromatogramMSD)chromatogram, chromatogram.getConverterId(), monitor);
			}
		} else if(chromatogramType == 1) {
		}
		processingInfo.addInfoMessage("Chromatogram Aligment", "Done");
	}

	/**
	 * Find the highest retention time among a number of MSD input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findHighestRetentionTime(List<ITotalScanSignals> totalScanSignals) {

		int highestRetentionTime = 0;
		for(ITotalScanSignals tics : totalScanSignals) {
			if(tics.getTotalScanSignals().get(tics.getStopScan() - 1).getRetentionTime() > highestRetentionTime) {
				highestRetentionTime = tics.getTotalScanSignals().get(tics.getStopScan() - 1).getRetentionTime();
			}
		}
		return highestRetentionTime;
	}

	/**
	 * Find the lowest retention time among a number of MSD input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findLowestRetentionTime(List<ITotalScanSignals> totalScanSignals) {

		int lowestRetentionTime = 0;
		for(ITotalScanSignals tics : totalScanSignals) {
			if(tics.getTotalScanSignals().get(tics.getStartScan() - 1).getRetentionTime() < lowestRetentionTime) {
				lowestRetentionTime = tics.getTotalScanSignals().get(tics.getStartScan() - 1).getRetentionTime();
			}
		}
		return lowestRetentionTime;
	}

	/**
	 * Create equispaced template chromatogram
	 * 
	 * @param retentionTimeWindow
	 * @param lowestRetentionTime
	 * @param highestRetentionTime
	 * @return regularChromatogramTemplate
	 */
	private Chromatogram constructEquispacedChromatogram(int retentionTimeWindow, int lowestRetentionTime, int highestRetentionTime) {

		Chromatogram standard = new Chromatogram();
		int totalDeltaRetentionTime = highestRetentionTime - lowestRetentionTime;
		int numberOfRetentionTimePoints = totalDeltaRetentionTime / retentionTimeWindow + 1;
		int deltaRetentionTime = totalDeltaRetentionTime / numberOfRetentionTimePoints;
		int currentRetentionTime = lowestRetentionTime;
		for(int i = 0; i < numberOfRetentionTimePoints; i++) {
			Scan equiSpacedScan = new Scan(0);
			equiSpacedScan.setRetentionTime(currentRetentionTime);
			standard.addScan(equiSpacedScan);
			currentRetentionTime += deltaRetentionTime;
		}
		return standard;
	}

	/**
	 * composeSampleTics
	 * 
	 * @param standardizedChromatograms
	 * @return
	 */
	private double[][] composeSampleTics(List<IChromatogram<? extends IPeak>> standardizedChromatograms) {

		int numberOfSamples = standardizedChromatograms.size();
		int numberOfScans = standardizedChromatograms.get(0).getNumberOfScans();
		double[][] sampleTics = new double[numberOfSamples][numberOfScans - 2 * MAX_SHIFT];
		for(int currentSample = 0; currentSample < numberOfSamples; currentSample++) {
			for(int currentScan = 0; currentScan < numberOfScans - 2 * MAX_SHIFT; currentScan++) {
				sampleTics[currentSample][currentScan] = standardizedChromatograms.get(currentSample).getScan(currentScan + MAX_SHIFT).getTotalSignal();
			}
		}
		return sampleTics;
	}

	/**
	 * calculateAverageSample
	 * 
	 * @param numberOfScans
	 * @param numberOfSamples
	 * @param standardizedChromatograms
	 * @return
	 */
	private double[] calculateAverageSample(List<IChromatogram<? extends IPeak>> standardizedChromatograms) {

		int numberOfSamples = standardizedChromatograms.size();
		int numberOfScans = standardizedChromatograms.get(0).getNumberOfScans();
		double[][] sampleTics = new double[numberOfSamples][numberOfScans + 2 * MAX_SHIFT + 1];
		for(int currentSample = 0; currentSample < standardizedChromatograms.size(); currentSample++) {
			Iterator<IScan> scanIterator = standardizedChromatograms.get(currentSample).getScans().iterator();
			for(int currentScan = 0; currentScan < numberOfScans; currentScan++) {
				sampleTics[currentSample][currentScan + MAX_SHIFT] = scanIterator.next().getTotalSignal();
			}
		}
		double[] averageSample = new double[numberOfScans];
		int signalSum = 0;
		for(int scanIndex = 0; scanIndex < numberOfScans; scanIndex++) {
			for(int sampleIndex = 0; sampleIndex < numberOfSamples; sampleIndex++) {
				signalSum += sampleTics[sampleIndex][scanIndex + MAX_SHIFT];
			}
			averageSample[scanIndex] = signalSum / numberOfSamples;
			signalSum = 0;
		}
		return averageSample;
	}

	/**
	 * composeTargetTics
	 * 
	 * @param numberOfScans
	 * @param averageSample
	 * @return
	 */
	private double[][] composeTargetTics(double[] averageSample) {

		int numberOfScans = averageSample.length;
		double[][] targetTics = new double[2 * MAX_SHIFT + 1][numberOfScans - 2 * MAX_SHIFT];
		for(int shiftIndex = 0; shiftIndex < 2 * MAX_SHIFT + 1; shiftIndex++) {
			for(int scanIndex = 0; scanIndex < numberOfScans - 2 * MAX_SHIFT; scanIndex++) {
				targetTics[shiftIndex][scanIndex] = averageSample[scanIndex + shiftIndex];
			}
		}
		return targetTics;
	}

	/**
	 * standardizeChromatograms
	 * 
	 * @param totalScanSignals
	 * @param retentionTimeWindow
	 * @param lowestRetentionTime
	 * @param highestRetentionTime
	 * @param monitor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<IChromatogram<? extends IPeak>> standardizeChromatograms(List<ITotalScanSignals> totalScanSignals, int retentionTimeWindow, int lowestRetentionTime, int highestRetentionTime) {

		List<IChromatogram<? extends IPeak>> standardizedChromatograms = new ArrayList<IChromatogram<? extends IPeak>>();
		for(ITotalScanSignals tics : totalScanSignals) {
			IChromatogram<? extends IPeak> standard = constructEquispacedChromatogram(retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
			Iterator<ITotalScanSignal> iterator = tics.getTotalScanSignals().iterator();
			ITotalScanSignal currentScan = iterator.next();
			float intensityBefore = 0;
			for(IScan scan : standard.getScans()) {
				while(iterator.hasNext() && currentScan.getRetentionTime() < scan.getRetentionTime()) {
					intensityBefore = currentScan.getTotalSignal();
					currentScan = iterator.next();
				}
				float intensityAfter = currentScan.getTotalSignal();
				float intensityAverage = (intensityBefore + intensityAfter) / 2;
				scan.adjustTotalSignal(intensityAverage);
				if(iterator.hasNext()) {
					currentScan = iterator.next();
				}
			}
			standardizedChromatograms.add(standard);
		}
		return standardizedChromatograms;
	}

	/**
	 * calculateColumnMaximumIndices
	 * 
	 * @param numberOfColumns
	 * @param matrix
	 * @return
	 */
	private int[] calculateColumnMaximumIndices(SimpleMatrix matrix) {

		int numberOfColumns = matrix.numCols();
		int[] columnMaximumIndices = new int[numberOfColumns];
		double[] columnMaximum = new double[numberOfColumns];
		for(int sampleIndex = 0; sampleIndex < numberOfColumns; sampleIndex++) {
			for(int shiftIndex = 0; shiftIndex < (2 * MAX_SHIFT + 1); shiftIndex++) {
				if(matrix.get(shiftIndex, sampleIndex) > columnMaximum[sampleIndex]) {
					columnMaximum[sampleIndex] = matrix.get(shiftIndex, sampleIndex);
					if((shiftIndex) / (MAX_SHIFT) > 0) {
						columnMaximumIndices[sampleIndex] = (shiftIndex % (MAX_SHIFT));
					} else {
						columnMaximumIndices[sampleIndex] = shiftIndex - MAX_SHIFT;
					}
				}
			}
		}
		return columnMaximumIndices;
	}

	private void loadData(List<ITotalScanSignals> alignmentTicsList, List<IDataInputEntry> dataInputEntries, int chromatogramType, IProgressMonitor monitor) {

		if(chromatogramType == 0) {
			for(IDataInputEntry entry : dataInputEntries) {
				IProcessingInfo processingInfo = ChromatogramConverterMSD.convert(new File(entry.getInputFile()), monitor);
				try {
					IChromatogram chromatogram = processingInfo.getProcessingResult(IChromatogramMSD.class);
					ITotalScanSignalExtractor totalIonSignalExtractor = new TotalScanSignalExtractor(chromatogram);
					IChromatogramSelectionMSD chromatogramSelection = new ChromatogramSelectionMSD(chromatogram);
					alignmentTicsList.add(totalIonSignalExtractor.getTotalScanSignals(chromatogramSelection));
				} catch(TypeCastException e) {
					logger.warn(e);
				} catch(ChromatogramIsNullException e) {
					logger.warn(e);
				}
			}
		} else if(chromatogramType == 1) {
			for(IDataInputEntry entry : dataInputEntries) {
				IProcessingInfo processingInfo = ChromatogramConverterCSD.convert(new File(entry.getInputFile()), monitor);
				try {
					IChromatogram chromatogram = processingInfo.getProcessingResult(IChromatogram.class);
					ITotalScanSignalExtractor totalIonSignalExtractor = new TotalScanSignalExtractor(chromatogram);
					IChromatogramSelectionCSD chromatogramSelection = new ChromatogramSelectionCSD(chromatogram);
					alignmentTicsList.add(totalIonSignalExtractor.getTotalScanSignals(chromatogramSelection));
				} catch(TypeCastException e) {
					logger.warn(e);
				} catch(ChromatogramIsNullException e) {
					logger.warn(e);
				}
			}
		}
	}
}
