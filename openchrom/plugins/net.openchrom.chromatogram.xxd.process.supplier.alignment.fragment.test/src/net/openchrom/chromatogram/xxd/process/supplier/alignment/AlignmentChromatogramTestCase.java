/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lorenz Gerber - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment;

import java.util.ArrayList;

import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IIon;
import org.eclipse.chemclipse.msd.model.core.IVendorMassSpectrum;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;
import org.eclipse.chemclipse.msd.model.implementation.ChromatogramMSD;
import org.eclipse.chemclipse.msd.model.implementation.Ion;
import org.eclipse.chemclipse.msd.model.implementation.VendorMassSpectrum;

import junit.framework.TestCase;

/*
 * Chromatogram
 * Retention times:
 * Scan 1 1500
 * Scan 2 2500
 * Scan 3 3500
 * Scan 4 4500
 * Scan 5 5500
 * Scan 6 6500
 * Scan 7 7500
 * Scan 8 8500
 * Scan 9 9500
 * Scan 10 10500
 */
public class AlignmentChromatogramTestCase extends TestCase {

	private IChromatogramMSD chromatogram;
	private ArrayList<IChromatogramMSD> chromatogramList;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		chromatogram = createChromatogram();
		chromatogramList = new ArrayList<IChromatogramMSD>();
		chromatogramList.add(createChromatogramWithPeak(2));
		chromatogramList.add(createChromatogramWithPeak(3));
		chromatogramList.add(createChromatogramWithPeak(4));
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public IChromatogramMSD getChromatogram() {

		return chromatogram;
	}

	public IChromatogramMSD getChromatogram(int index) {

		return chromatogramList.get(index);
	}

	private IChromatogramMSD createChromatogram() {

		IChromatogramMSD chromatogram = new ChromatogramMSD();
		IVendorMassSpectrum scan;
		for(int i = 1; i <= 10; i++) {
			scan = new VendorMassSpectrum();
			chromatogram.addScan(scan);
		}
		chromatogram.setScanDelay(1500);
		chromatogram.setScanInterval(1000);
		chromatogram.recalculateRetentionTimes();
		return chromatogram;
	}

	private IChromatogramMSD createChromatogramWithPeak(int peakPosition) {

		IChromatogramMSD chromatogram = new ChromatogramMSD();
		IVendorMassSpectrum scan;
		IIon ion;
		for(int i = 1; i <= 10; i++) {
			scan = new VendorMassSpectrum();
			try {
				ion = new Ion(100);
				int abundance = (int)(10000 * Math.pow(2.7, -(Math.pow(i - peakPosition, 2) / (Math.pow(2 * 1, 2)))));
				ion.setAbundance(abundance);
				scan.addIon(ion);
				chromatogram.addScan(scan);
			} catch(IonLimitExceededException e) {
				System.out.println(e);
			} catch(AbundanceLimitExceededException e) {
				System.out.println(e);
			}
		}
		chromatogram.setScanDelay(1500);
		chromatogram.setScanInterval(1000);
		chromatogram.recalculateRetentionTimes();
		return chromatogram;
	}
}
