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

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.implementation.Chromatogram;
import org.eclipse.chemclipse.model.implementation.Scan;
import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ejml.simple.SimpleMatrix;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;

public class AlignmentProcessor {

	private static final Logger logger = Logger.getLogger(AlignmentProcessor.class);
	private static final int MAX_SHIFT = 20;

	public IProcessingInfo alignChromatograms(List<IDataInputEntry> dataInputEntries, int retentionTimeWindow, IProgressMonitor monitor) {

		IProcessingInfo processingInfo = new ProcessingInfo();
		List<File> inputFiles = new ArrayList<File>();
		for(IDataInputEntry inputEntry : dataInputEntries) {
			System.out.println("Reading chromatogram: " + inputEntry.getName() + "\t" + inputEntry.getInputFile());
			inputFiles.add(new File(inputEntry.getInputFile()));
		}
		int highestRetentionTime = findHighestRt(inputFiles, monitor);
		int lowestRetentionTime = findLowestRt(inputFiles, monitor);
		int numberOfScans = (highestRetentionTime - lowestRetentionTime) / retentionTimeWindow;
		int numberOfSamples = dataInputEntries.size();
		List<Chromatogram> standardizedChromatograms = standardizeChromatograms(dataInputEntries, retentionTimeWindow, lowestRetentionTime, highestRetentionTime, monitor);
		double[][] sampleTics = composeSampleTics(numberOfSamples, numberOfScans, standardizedChromatograms);
		SimpleMatrix sampleTicsMatrix = new SimpleMatrix(sampleTics);
		sampleTicsMatrix = sampleTicsMatrix.transpose();
		double[] averageSample = calculateAverageSample(numberOfScans, numberOfSamples, standardizedChromatograms);
		double[][] targetTics = composeTargetTics(numberOfScans, averageSample);
		SimpleMatrix targetTicsMatrix = new SimpleMatrix(targetTics);
		SimpleMatrix matrixShiftResults = new SimpleMatrix(targetTicsMatrix.mult(sampleTicsMatrix));
		int[] colMaxIndices = calcColMaxIndices(numberOfSamples, matrixShiftResults);
		// apply shift to chromatograms using rtshifter
		processingInfo.addInfoMessage("Chromatogram Aligment", "Done");
		return processingInfo;
	}

	/**
	 * Find the highest retention time among a number of input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findHighestRt(List<File> inputFiles, IProgressMonitor monitor) {

		int highestRt = 0;
		for(File scanFile : inputFiles) {
			IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(scanFile, monitor);
			try {
				IChromatogramMSD chromatogram = processingInfo.getChromatogram();
				if(chromatogram.getStopRetentionTime() > highestRt) {
					highestRt = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
		}
		return highestRt;
	}

	/**
	 * Find the lowest retention time among a number of input files
	 * 
	 * @param inputFiles
	 * @param monitor
	 * @return
	 */
	public int findLowestRt(List<File> inputFiles, IProgressMonitor monitor) {

		int lowestRt = 0;
		for(File scanFile : inputFiles) {
			IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(scanFile, monitor);
			try {
				IChromatogramMSD chromatogram = processingInfo.getChromatogram();
				// String name = extractNameFromFile(scanFile, "n.a.");
				if(chromatogram.getStopRetentionTime() < lowestRt) {
					lowestRt = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
		}
		return lowestRt;
	}

	/**
	 * Create equispaced template chromatogram
	 * 
	 * @param retentionTimeWindow
	 * @param lowestRt
	 * @param highestRt
	 * @return regularChromatogramTemplate
	 */
	private Chromatogram constructEquispacedChromatogram(int retentionTimeWindow, int lowestRt, int highestRt) {

		Chromatogram standard = new Chromatogram();
		int deltaRt = highestRt - lowestRt;
		int numberOfRtPoints = deltaRt / retentionTimeWindow;
		// int moduloTime = deltaRt % retentionTimeWindow;
		for(int xyz = lowestRt; xyz < numberOfRtPoints; xyz++) {
			Scan equiSpacedScan = new Scan(0);
			equiSpacedScan.setRetentionTime(xyz);
			standard.addScan(equiSpacedScan);
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
	 * standardizeChromatograms
	 * 
	 * @param dataInputEntries
	 * @param retentionTimeWindow
	 * @param lowestRetentionTime
	 * @param highestRetentionTime
	 * @param monitor
	 * @return
	 */
	private List<Chromatogram> standardizeChromatograms(List<IDataInputEntry> dataInputEntries, int retentionTimeWindow, int lowestRetentionTime, int highestRetentionTime, IProgressMonitor monitor) {

		List<Chromatogram> standardizedChromatograms = new ArrayList<Chromatogram>();
		for(IDataInputEntry inputEntry : dataInputEntries) {
			Chromatogram standard = constructEquispacedChromatogram(retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
			// iterate through the standard chromatogram and fill in intensity values
			// from interpolating from the currently loaded chromatogram
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
					// need to check here also if the currentScan is not higher than the standard's retention time.
					float intensityAfter = currentScan.getTotalSignal();
					// will implement more advanced interpolation later.
					float intensityAverage = (intensityBefore + intensityAfter) / 2;
					scan.adjustTotalSignal(intensityAverage);
					if(iterator.hasNext()) {
						currentScan = iterator.next();
					}
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
			// somehow need to store the standard chromatogram here.
			standardizedChromatograms.add(standard);
		}
		return standardizedChromatograms;
	}

	/**
	 * calcColMaxIndicies
	 * 
	 * @param numberOfCols
	 * @param matrix
	 * @return
	 */
	int[] calcColMaxIndices(int numberOfCols, SimpleMatrix matrix) {

		int[] colMaxIndices = new int[numberOfCols];
		double[] colMax = new double[numberOfCols];
		for(int sampleIndex = 0; sampleIndex < numberOfCols; sampleIndex++) {
			for(int shiftIndex = 0; shiftIndex < (2 * MAX_SHIFT + 1); shiftIndex++) {
				if(matrix.get(shiftIndex, sampleIndex) > colMax[sampleIndex]) {
					colMax[sampleIndex] = matrix.get(shiftIndex, sampleIndex);
					if((shiftIndex + 1) / (MAX_SHIFT + 1) > 0) {
						colMaxIndices[sampleIndex] = (shiftIndex % (MAX_SHIFT + 1));
					} else {
						colMaxIndices[sampleIndex] = shiftIndex - MAX_SHIFT;
					}
				}
			}
		}
		return colMaxIndices;
	}
}
