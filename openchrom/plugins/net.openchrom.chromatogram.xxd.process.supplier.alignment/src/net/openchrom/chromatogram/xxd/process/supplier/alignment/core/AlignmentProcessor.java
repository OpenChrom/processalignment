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
import java.util.List;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.implementation.Chromatogram;
import org.eclipse.chemclipse.model.implementation.Scan;
import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;
import org.eclipse.core.runtime.IProgressMonitor;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;

public class AlignmentProcessor {

	private static final Logger logger = Logger.getLogger(AlignmentProcessor.class);

	public IProcessingInfo alignChromatograms(List<IDataInputEntry> dataInputEntries, int retentionTimeWindow, IProgressMonitor monitor) {

		IProcessingInfo processingInfo = new ProcessingInfo();
		List<File> inputFiles = new ArrayList<File>();
		for(IDataInputEntry inputEntry : dataInputEntries) {
			System.out.println("Reading chromatogram: " + inputEntry.getName() + "\t" + inputEntry.getInputFile());
			inputFiles.add(new File(inputEntry.getInputFile()));
			int highestRetentionTime = findHighestRt(inputFiles, monitor);
			int lowestRetentionTime = findLowestRt(inputFiles, monitor);
			Chromatogram standard = constructEquispacedChromatogram(retentionTimeWindow, lowestRetentionTime, highestRetentionTime);
		}
		//
		processingInfo.addInfoMessage("Chromatogram Aligment", "Done");
		return processingInfo;
	}

	public int findHighestRt(List<File> inputFiles, IProgressMonitor monitor) {

		int highestRt = 0;
		for(File scanFile : inputFiles) {
			IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(scanFile, monitor);
			try {
				IChromatogramMSD chromatogram = processingInfo.getChromatogram();
				// String name = extractNameFromFile(scanFile, "n.a.");
				if(chromatogram.getStopRetentionTime() > highestRt) {
					highestRt = chromatogram.getStopRetentionTime();
				}
			} catch(TypeCastException e) {
				logger.warn(e);
			}
		}
		return highestRt;
	}

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
	 * Extracts the file name.
	 * 
	 * @param file
	 * @param nameDefault
	 * @return String
	 */
	private String extractNameFromFile(File file, String nameDefault) {

		if(file != null) {
			String fileName = file.getName();
			if(fileName != "" && fileName != null) {
				/*
				 * Extract the file name.
				 */
				String[] parts = fileName.split("\\.");
				if(parts.length > 2) {
					StringBuilder builder = new StringBuilder();
					for(int i = 0; i < parts.length - 1; i++) {
						builder.append(parts[i]);
						builder.append(".");
					}
					String name = builder.toString();
					nameDefault = name.substring(0, name.length() - 1);
				} else {
					/*
					 * If there are not 2 parts, it's assumed that the file had no extension.
					 */
					if(parts.length == 2) {
						nameDefault = parts[0];
					}
				}
			}
		}
		return nameDefault;
	}

	/**
	 * Create regular template chromatogram
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
		for(int xyz = lowestRt; xyz < numberOfRtPoints; xyz = xyz++) {
			Scan equiSpacedScan = new Scan(0);
			equiSpacedScan.setRetentionTime(xyz);
			standard.addScan(equiSpacedScan);
		}
		return standard;
	}
}
