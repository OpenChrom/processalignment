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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.rtshifter.core.ChromatogramFilterShift;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.rtshifter.settings.SupplierFilterShiftSettings;
import org.eclipse.chemclipse.csd.converter.chromatogram.ChromatogramConverterCSD;
import org.eclipse.chemclipse.csd.converter.processing.chromatogram.IChromatogramCSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.csd.model.core.IChromatogramCSD;
import org.eclipse.chemclipse.csd.model.core.selection.ChromatogramSelectionCSD;
import org.eclipse.chemclipse.csd.model.core.selection.IChromatogramSelectionCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.exceptions.ChromatogramIsNullException;
import org.eclipse.chemclipse.model.implementation.Chromatogram;
import org.eclipse.chemclipse.model.implementation.Scan;
import org.eclipse.chemclipse.model.selection.ChromatogramSelection;
import org.eclipse.chemclipse.model.signals.ITotalScanSignal;
import org.eclipse.chemclipse.model.signals.ITotalScanSignalExtractor;
import org.eclipse.chemclipse.model.signals.ITotalScanSignals;
import org.eclipse.chemclipse.model.signals.TotalScanSignalExtractor;
import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;
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
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.AlignmentSettings;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.IAlignmentSettings;

public class AlignmentProcessor {

	private static final Logger logger = Logger.getLogger(AlignmentProcessor.class);
	private static final int MAX_SHIFT = 10;

	public IAlignmentResults calculateAlignment(List<IDataInputEntry> dataInputEntries, IAlignmentSettings alignmentSettings, IProgressMonitor monitor) {

		/*
		 * Preparing evironment
		 */
		IAlignmentResults alignmentResults = new AlignmentResults(dataInputEntries);
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
		if(chromatogramType == 0) {
			alignmentTicsList = new ArrayList<ITotalScanSignals>();
			for(IDataInputEntry entry : dataInputEntries) {
				IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(new File(entry.getInputFile()), monitor);
				try {
					IChromatogram chromatogram = processingInfo.getChromatogram();
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
			alignmentTicsList = new ArrayList<ITotalScanSignals>();
			for(IDataInputEntry entry : dataInputEntries) {
				IChromatogramCSDImportConverterProcessingInfo processingInfo = ChromatogramConverterCSD.convert(new File(entry.getInputFile()), monitor);
				try {
					IChromatogram chromatogram = processingInfo.getChromatogram();
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
		highestRetentionTime = findHighestRetentionTime(alignmentTicsList);
		lowestRetentionTime = findLowestRetentionTime(alignmentTicsList);
		/*
		 * Calculate two sets of standardized chromatograms
		 */
		List<Chromatogram> standardizedTICsBefore = null;
		standardizedTICsBefore = standardizeChromatograms(alignmentTicsList, retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
		List<Chromatogram> standardizedTICsAfter = null;
		standardizedTICsAfter = standardizeChromatograms(alignmentTicsList, retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
		/*
		 * store standardized TIC chromatograms in results
		 */
		Iterator<Chromatogram> chromatogramBeforeIterator = standardizedTICsBefore.iterator();
		Iterator<Chromatogram> chromatogramAfterIterator = standardizedTICsAfter.iterator();
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
			List<Chromatogram> standardizedChromatograms = null;
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
				/*
				 * alignmentResults.getAlignmentResultMap().get(new Sample(entry.next().getName())).addShift(columnMaximumIndices[shiftIndex]);
				 */
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
	AlignmentResults applyAlignment(AlignmentResults results, AlignmentSettings settings, IProgressMonitor monitor) {

		int chromatogramType = settings.getChromatogramType();
		IProcessingInfo processingInfo = new ProcessingInfo();
		/*
		 * apply shift to files and export
		 */
		if(chromatogramType == 0) {
			// exportMSD(inputFiles, columnMaximumIndices, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		} else {
			// exportCSD(inputFiles, columnMaximumIndices, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		}
		processingInfo.addInfoMessage("Chromatogram Aligment", "Done");
		return results;
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
	 * Sample Matrix
	 * 
	 * @param numberOfSamples
	 * @param numberOfScans
	 * @param standardizedChromatograms
	 * @return
	 */
	private double[][] composeSampleTics2(List<Chromatogram> standardizedChromatograms) {

		int numberOfSamples = standardizedChromatograms.size();
		int numberOfScans = standardizedChromatograms.get(0).getNumberOfScans();
		double[][] sampleTics = new double[numberOfSamples][numberOfScans + 2 * MAX_SHIFT + 1];
		for(int currentSample = 0; currentSample < standardizedChromatograms.size(); currentSample++) {
			Iterator<IScan> scanIterator = standardizedChromatograms.get(currentSample).getScans().iterator();
			for(int currentScan = 0; currentScan < numberOfScans; currentScan++) {
				sampleTics[currentSample][currentScan + MAX_SHIFT] = scanIterator.next().getTotalSignal();
			}
		}
		return sampleTics;
	}

	/*
	 * this version is adapted to the reduced matrix size New Version, to be linked in
	 * when the coresponding function for TargetTics is finished.
	 */
	private double[][] composeSampleTics(List<Chromatogram> standardizedChromatograms) {

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
	private double[] calculateAverageSample(List<Chromatogram> standardizedChromatograms) {

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
	private List<Chromatogram> standardizeChromatograms(List<ITotalScanSignals> totalScanSignals, int retentionTimeWindow, int lowestRetentionTime, int highestRetentionTime) {

		List<Chromatogram> standardizedChromatograms = new ArrayList<Chromatogram>();
		for(ITotalScanSignals tics : totalScanSignals) {
			Chromatogram standard = constructEquispacedChromatogram(retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
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
	int[] calculateColumnMaximumIndices(SimpleMatrix matrix) {

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

	/**
	 * exportMSD - export aligned files to MSD
	 * 
	 * @param inputFiles
	 * @param columnMaximumIndices
	 * @param retentionTimeWindow
	 * @param monitor
	 */
	void exportMSD(List<File> inputFiles, int columnMaximumIndices[], int retentionTimeWindow, int lowerRetentionTimeSelection, int upperRetentionTimeSelection, IProgressMonitor monitor) {

		int counter = 0;
		for(File scanFile : inputFiles) {
			IChromatogramMSDImportConverterProcessingInfo processingInfo2 = ChromatogramConverterMSD.convert(scanFile, monitor);
			try {
				ChromatogramSelection currentChromatogram = new ChromatogramSelection(processingInfo2.getChromatogram());
				currentChromatogram.setStartRetentionTime(lowerRetentionTimeSelection);
				currentChromatogram.setStopRetentionTime(upperRetentionTimeSelection);
				ChromatogramFilterShift shifter = new ChromatogramFilterShift();
				SupplierFilterShiftSettings settings = new SupplierFilterShiftSettings(columnMaximumIndices[counter] * retentionTimeWindow, false);
				shifter.applyFilter(currentChromatogram, settings, monitor);
				ChromatogramConverterMSD.convert(scanFile, (IChromatogramMSD)currentChromatogram.getChromatogram(), currentChromatogram.getChromatogram().getConverterId(), monitor);
			} catch(TypeCastException | ChromatogramIsNullException e) {
				logger.warn(e);
			}
			counter++;
		}
	}

	/**
	 * exportCSD - export aligned files to CSD
	 * 
	 * @param inputFiles
	 * @param columnMaximumIndices
	 * @param retentionTimeWindow
	 * @param monitor
	 */
	void exportCSD(List<File> inputFiles, int columnMaximumIndices[], int retentionTimeWindow, int lowerRetentionTimeSelection, int upperRetentionTimeSelection, IProgressMonitor monitor) {

		int counter = 0;
		for(File scanFile : inputFiles) {
			IChromatogramCSDImportConverterProcessingInfo processingInfo2 = ChromatogramConverterCSD.convert(scanFile, monitor);
			try {
				ChromatogramSelection currentChromatogram = new ChromatogramSelection(processingInfo2.getChromatogram());
				currentChromatogram.setStartRetentionTime(lowerRetentionTimeSelection);
				currentChromatogram.setStopRetentionTime(upperRetentionTimeSelection);
				ChromatogramFilterShift shifter = new ChromatogramFilterShift();
				SupplierFilterShiftSettings settings = new SupplierFilterShiftSettings(columnMaximumIndices[counter] * retentionTimeWindow, true);
				shifter.applyFilter(currentChromatogram, settings, monitor);
				ChromatogramConverterCSD.convert(scanFile, (IChromatogramCSD)currentChromatogram.getChromatogram(), currentChromatogram.getChromatogram().getConverterId(), monitor);
			} catch(TypeCastException | ChromatogramIsNullException e) {
				logger.warn(e);
			}
			counter++;
		}
	}
}
