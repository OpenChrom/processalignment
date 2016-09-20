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
import java.util.Map;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.rtshifter.core.ChromatogramFilterShift;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.rtshifter.settings.SupplierFilterShiftSettings;
import org.eclipse.chemclipse.csd.converter.chromatogram.ChromatogramConverterCSD;
import org.eclipse.chemclipse.csd.converter.processing.chromatogram.IChromatogramCSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.csd.model.core.IChromatogramCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.exceptions.ChromatogramIsNullException;
import org.eclipse.chemclipse.model.implementation.Chromatogram;
import org.eclipse.chemclipse.model.implementation.Scan;
import org.eclipse.chemclipse.model.selection.ChromatogramSelection;
import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ejml.simple.SimpleMatrix;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentResult;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResult;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.ISample;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.Sample;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.SupplierProcessorAlignmentSettings;

public class AlignmentProcessor {

	private static final Logger logger = Logger.getLogger(AlignmentProcessor.class);
	private static final int MAX_SHIFT = 20;

	public IAlignmentResults alignChromatograms(List<IDataInputEntry> dataInputEntries, SupplierProcessorAlignmentSettings settings, int retentionTimeWindow, IProgressMonitor monitor, int chromatogramType, int lowerRetentionTimeSelection, int upperRetentionTimeSelection) {

		/*
		 * Initialize ALignment Results
		 */
		IAlignmentResults alignmentResults = new AlignmentResults(dataInputEntries);
		alignmentResults.setRetentionTimeWindow(settings.getRetentionTimeWindow());
		List<File> inputFiles = getInputFiles(dataInputEntries);
		prepareAlignmentResults(inputFiles, alignmentResults);
		// adjusting user input of processing selection to milliseconds
		lowerRetentionTimeSelection = settings.getAlignmentRanges().getLowestStartRetentionTime() * 60000;
		upperRetentionTimeSelection = settings.getAlignmentRanges().getHighestStopRetentionTime() * 60000;
		/*
		 * Find lowest and highest Scans over the whole chromatogram set
		 */
		IProcessingInfo processingInfo = new ProcessingInfo();
		int highestRetentionTime = 0;
		int lowestRetentionTime = 0;
		if(chromatogramType == 0) {
			highestRetentionTime = findHighestRetentionTimeMSD(inputFiles, monitor);
			lowestRetentionTime = findLowestRetentionTimeMSD(inputFiles, monitor);
		} else {
			highestRetentionTime = findHighestRetentionTimeCSD(inputFiles, monitor);
			lowestRetentionTime = findLowestRetentionTimeCSD(inputFiles, monitor);
		}
		/*
		 * Calculate standardized chromatograms
		 */
		List<Chromatogram> standardizedTICs = null;
		if(chromatogramType == 0) {
			standardizedTICs = standardizeChromatogramsMSD(dataInputEntries, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		} else {
			standardizedTICs = standardizeChromatogramsCSD(dataInputEntries, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		}
		/*
		 * store standardized TIC chromatograms in results
		 */
		Iterator<Chromatogram> chromatogramIterator = standardizedTICs.iterator();
		Iterator<File> fileIterator = inputFiles.iterator();
		while(chromatogramIterator.hasNext() && inputFiles.iterator().hasNext()) {
			IAlignmentResult currentResult = alignmentResults.getAlignmentResultMap().get(new Sample(fileIterator.next().getName()));
			currentResult.setTicBeforeAlignment(chromatogramIterator.next());
		}
		/*
		 * compare user choice of retention time with lowest and highest of the set
		 */
		if(upperRetentionTimeSelection > highestRetentionTime) {
			upperRetentionTimeSelection = highestRetentionTime;
		}
		if(lowerRetentionTimeSelection < lowestRetentionTime) {
			lowerRetentionTimeSelection = lowestRetentionTime;
		}
		/*
		 * Calculate standardized chromatograms
		 */
		List<Chromatogram> standardizedChromatograms = null;
		if(chromatogramType == 0) {
			standardizedChromatograms = standardizeChromatogramsMSD(dataInputEntries, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		} else {
			standardizedChromatograms = standardizeChromatogramsCSD(dataInputEntries, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		}
		/*
		 * Calculate sample TIC matrix
		 */
		int numberOfScans = (upperRetentionTimeSelection - lowerRetentionTimeSelection) / retentionTimeWindow + 1;
		int numberOfSamples = dataInputEntries.size();
		double[][] sampleTics = composeSampleTics(numberOfSamples, numberOfScans, standardizedChromatograms);
		SimpleMatrix sampleTicsMatrix = new SimpleMatrix(sampleTics);
		sampleTicsMatrix = sampleTicsMatrix.transpose();
		/*
		 * Calculate averaged sample
		 */
		double[] averageSample = calculateAverageSample(numberOfScans, numberOfSamples, standardizedChromatograms);
		/*
		 * calculate shifted TICs of averaged sample
		 */
		double[][] targetTics = composeTargetTics(numberOfScans, averageSample);
		/*
		 * calculate shift matrix
		 */
		SimpleMatrix targetTicsMatrix = new SimpleMatrix(targetTics);
		SimpleMatrix matrixShiftResults = new SimpleMatrix(targetTicsMatrix.mult(sampleTicsMatrix));
		int[] columnMaximumIndices = calculateColumnMaximumIndices(numberOfSamples, matrixShiftResults);
		/*
		 * apply shift to files and export
		 */
		if(chromatogramType == 0) {
			exportMSD(inputFiles, columnMaximumIndices, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		} else {
			exportCSD(inputFiles, columnMaximumIndices, retentionTimeWindow, lowerRetentionTimeSelection, upperRetentionTimeSelection, monitor);
		}
		processingInfo.addInfoMessage("Chromatogram Aligment", "Done");
		return alignmentResults;
	}

	/**
	 * getInputFiles
	 * 
	 * @param dataInputEntries
	 * @return
	 */
	private List<File> getInputFiles(List<IDataInputEntry> dataInputEntries) {

		List<File> inputFiles = new ArrayList<File>();
		for(IDataInputEntry inputEntry : dataInputEntries) {
			System.out.println("Reading chromatogram: " + inputEntry.getName() + "\t" + inputEntry.getInputFile());
			inputFiles.add(new File(inputEntry.getInputFile()));
		}
		return inputFiles;
	}

	/**
	 * prepareAlignmentResults
	 * 
	 */
	private void prepareAlignmentResults(List<File> inputFiles, IAlignmentResults alignmentResults) {

		Map<ISample, IAlignmentResult> alignmentResultMap = alignmentResults.getAlignmentResultMap();
		for(File file : inputFiles) {
			AlignmentResult alignmentResult = new AlignmentResult();
			alignmentResultMap.put(new Sample(file.getName()), alignmentResult);
		}
	}

	/**
	 * Find the highest retention time among a number of MSD input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findHighestRetentionTimeMSD(List<File> inputFiles, IProgressMonitor monitor) {

		int highestRetentionTime = 0;
		for(File scanFile : inputFiles) {
			IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(scanFile, monitor);
			try {
				IChromatogram chromatogram = processingInfo.getChromatogram();
				if(chromatogram.getStopRetentionTime() > highestRetentionTime) {
					highestRetentionTime = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
		}
		return highestRetentionTime;
	}

	/**
	 * Find the highest retention time among a number of CSD input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findHighestRetentionTimeCSD(List<File> inputFiles, IProgressMonitor monitor) {

		int highestRetentionTime = 0;
		for(File scanFile : inputFiles) {
			IChromatogramCSDImportConverterProcessingInfo processingInfo = ChromatogramConverterCSD.convert(scanFile, monitor);
			try {
				IChromatogram chromatogram = processingInfo.getChromatogram();
				if(chromatogram.getStopRetentionTime() > highestRetentionTime) {
					highestRetentionTime = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
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
	public int findLowestRetentionTimeMSD(List<File> inputFiles, IProgressMonitor monitor) {

		int lowestRetentionTime = 0;
		for(File scanFile : inputFiles) {
			IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(scanFile, monitor);
			try {
				IChromatogramMSD chromatogram = processingInfo.getChromatogram();
				if(chromatogram.getStopRetentionTime() < lowestRetentionTime) {
					lowestRetentionTime = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
		}
		return lowestRetentionTime;
	}

	/**
	 * Find the lowest retention time among a number of CSD input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findLowestRetentionTimeCSD(List<File> inputFiles, IProgressMonitor monitor) {

		int lowestRetentionTime = 0;
		for(File scanFile : inputFiles) {
			IChromatogramCSDImportConverterProcessingInfo processingInfo = ChromatogramConverterCSD.convert(scanFile, monitor);
			try {
				IChromatogram chromatogram = processingInfo.getChromatogram();
				if(chromatogram.getStopRetentionTime() < lowestRetentionTime) {
					lowestRetentionTime = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
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
		int deltaRetentionTime = highestRetentionTime - lowestRetentionTime;
		int numberOfRetentionTimePoints = deltaRetentionTime / retentionTimeWindow + 1;
		int currentRetentionTime = lowestRetentionTime;
		// TODO this does not work yet for ranges that don't start at zero
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
	private double[][] composeSampleTics(int numberOfSamples, int numberOfScans, List<Chromatogram> standardizedChromatograms) {

		double[][] sampleTics = new double[numberOfSamples][numberOfScans + 2 * MAX_SHIFT + 1];
		for(int currentSample = 0; currentSample < standardizedChromatograms.size(); currentSample++) {
			Iterator<IScan> scanIterator = standardizedChromatograms.get(currentSample).getScans().iterator();
			for(int currentScan = 0; currentScan < numberOfScans; currentScan++) {
				sampleTics[currentSample][currentScan + MAX_SHIFT] = scanIterator.next().getTotalSignal();
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
	private double[] calculateAverageSample(int numberOfScans, int numberOfSamples, List<Chromatogram> standardizedChromatograms) {

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
	private double[][] composeTargetTics(int numberOfScans, double[] averageSample) {

		double[][] targetTics = new double[2 * MAX_SHIFT + 1][numberOfScans + 2 * MAX_SHIFT + 1];
		for(int shiftIndex = 0; shiftIndex < 2 * MAX_SHIFT + 1; shiftIndex++) {
			for(int scanIndex = 0; scanIndex < numberOfScans; scanIndex++) {
				targetTics[shiftIndex][shiftIndex + scanIndex] = averageSample[scanIndex];
			}
		}
		return targetTics;
	}

	/**
	 * standardizeChromatogramsMSD
	 * 
	 * @param dataInputEntries
	 * @param retentionTimeWindow
	 * @param lowestRetentionTime
	 * @param highestRetentionTime
	 * @param monitor
	 * @return
	 */
	private List<Chromatogram> standardizeChromatogramsMSD(List<IDataInputEntry> dataInputEntries, int retentionTimeWindow, int lowestRetentionTime, int highestRetentionTime, IProgressMonitor monitor) {

		List<Chromatogram> standardizedChromatograms = new ArrayList<Chromatogram>();
		for(IDataInputEntry inputEntry : dataInputEntries) {
			Chromatogram standard = constructEquispacedChromatogram(retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
			IChromatogramMSDImportConverterProcessingInfo processingInfo2 = ChromatogramConverterMSD.convert(new File(inputEntry.getInputFile()), monitor);
			try {
				IChromatogramMSD chromatogram = processingInfo2.getChromatogram();
				Iterator<IScan> iterator = chromatogram.getScans().iterator();
				IScan currentScan = iterator.next();
				float intensityBefore = 0;
				for(IScan scan : standard.getScans()) {
					while(iterator.hasNext() && currentScan.getRetentionTime() < scan.getRetentionTime()) {
						intensityBefore = currentScan.getTotalSignal();
						currentScan = iterator.next();
					}
					// TODO need to check here also if the currentScan is not higher than the standard's retention time.
					float intensityAfter = currentScan.getTotalSignal();
					float intensityAverage = (intensityBefore + intensityAfter) / 2;
					scan.adjustTotalSignal(intensityAverage);
					if(iterator.hasNext()) {
						currentScan = iterator.next();
					}
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
			standardizedChromatograms.add(standard);
		}
		return standardizedChromatograms;
	}

	/**
	 * standardizeChromatogramsCSD
	 * 
	 * @param dataInputEntries
	 * @param retentionTimeWindow
	 * @param lowestRetentionTime
	 * @param highestRetentionTime
	 * @param monitor
	 * @return
	 */
	private List<Chromatogram> standardizeChromatogramsCSD(List<IDataInputEntry> dataInputEntries, int retentionTimeWindow, int lowestRetentionTime, int highestRetentionTime, IProgressMonitor monitor) {

		List<Chromatogram> standardizedChromatograms = new ArrayList<Chromatogram>();
		for(IDataInputEntry inputEntry : dataInputEntries) {
			Chromatogram standard = constructEquispacedChromatogram(retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
			IChromatogramCSDImportConverterProcessingInfo processingInfo2 = ChromatogramConverterCSD.convert(new File(inputEntry.getInputFile()), monitor);
			try {
				IChromatogram chromatogram = processingInfo2.getChromatogram();
				Iterator<IScan> iterator = chromatogram.getScans().iterator();
				IScan currentScan = iterator.next();
				float intensityBefore = 0;
				// currently the average signal from the scans just before and after the equispaced model
				// scan in question are used. In a more advanced version, interpolation should be done.
				for(IScan scan : standard.getScans()) {
					while(iterator.hasNext() && currentScan.getRetentionTime() < scan.getRetentionTime()) {
						intensityBefore = currentScan.getTotalSignal();
						currentScan = iterator.next();
					}
					// TODO need to check here also if the currentScan is not higher than the standard's retention time.
					float intensityAfter = currentScan.getTotalSignal();
					float intensityAverage = (intensityBefore + intensityAfter) / 2;
					scan.adjustTotalSignal(intensityAverage);
					if(iterator.hasNext()) {
						currentScan = iterator.next();
					}
				}
			} catch(TypeCastException e) {
				logger.warn(e);
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
	int[] calculateColumnMaximumIndices(int numberOfColumns, SimpleMatrix matrix) {

		int[] columnMaximumIndices = new int[numberOfColumns];
		double[] columnMaximum = new double[numberOfColumns];
		for(int sampleIndex = 0; sampleIndex < numberOfColumns; sampleIndex++) {
			for(int shiftIndex = 0; shiftIndex < (2 * MAX_SHIFT + 1); shiftIndex++) {
				if(matrix.get(shiftIndex, sampleIndex) > columnMaximum[sampleIndex]) {
					columnMaximum[sampleIndex] = matrix.get(shiftIndex, sampleIndex);
					if((shiftIndex + 1) / (MAX_SHIFT + 1) > 0) {
						columnMaximumIndices[sampleIndex] = (shiftIndex % (MAX_SHIFT + 1));
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
