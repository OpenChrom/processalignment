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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.signals.ITotalScanSignalExtractor;
import org.eclipse.chemclipse.model.signals.ITotalScanSignals;
import org.eclipse.chemclipse.model.signals.TotalScanSignalExtractor;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IVendorMassSpectrum;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.AlignmentChromatogramTestCase;

public class Alignment_1_Test extends AlignmentChromatogramTestCase {

	private IChromatogramMSD chromatogram;
	private IChromatogramSelectionMSD chromatogramSelection;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		chromatogram = getChromatogram();
		chromatogramSelection = new ChromatogramSelectionMSD(chromatogram);
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void test_0() {

		assertNotNull(chromatogramSelection);
	}

	public void testloadChromatograms_1() {

		ArrayList<ITotalScanSignals> alignmentTicsList = new ArrayList<ITotalScanSignals>();
		ITotalScanSignalExtractor totalIonSignalExtractor = new TotalScanSignalExtractor(getChromatogram(0));
		IChromatogramSelectionMSD chromatogramSelection = new ChromatogramSelectionMSD(getChromatogram(0));
		alignmentTicsList.add(totalIonSignalExtractor.getTotalScanSignals(chromatogramSelection));
	}

	public void testShiftBackward_1() {

		IVendorMassSpectrum scan;
		/*
		 * PRE TESTS
		 */
		assertEquals(10, chromatogram.getNumberOfScans());
		assertEquals(1500, chromatogram.getScanDelay());
		scan = chromatogram.getSupplierScan(1);
		assertEquals(1500, scan.getRetentionTime());
		scan = chromatogram.getSupplierScan(10);
		assertEquals(10500, scan.getRetentionTime());
	}

	public void testShiftBackward_2() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(10, chromatogram.getNumberOfScans());
			assertEquals(1500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(10500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_3() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(10, chromatogram.getNumberOfScans());
			assertEquals(1500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(10500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_4() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(10, chromatogram.getNumberOfScans());
			assertEquals(1500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(10500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_5() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(10, chromatogram.getNumberOfScans());
			assertEquals(1500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(10500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_6() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(10, chromatogram.getNumberOfScans());
			assertEquals(1500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(10500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}
}
