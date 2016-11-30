/*******************************************************************************
 * Copyright (c) 2016 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lorenz Gerber - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.fragment.test;

import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IVendorMassSpectrum;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;

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
			assertEquals(500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(9500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_4() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(10, chromatogram.getNumberOfScans());
			assertEquals(1, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(10);
			assertEquals(9001, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_5() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(9, chromatogram.getNumberOfScans());
			assertEquals(1000, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(1000, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(9);
			assertEquals(9000, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}

	public void testShiftBackward_6() {

		IVendorMassSpectrum scan;
		try {
			assertEquals(9, chromatogram.getNumberOfScans());
			assertEquals(500, chromatogram.getScanDelay());
			scan = chromatogram.getSupplierScan(1);
			assertEquals(500, scan.getRetentionTime());
			scan = chromatogram.getSupplierScan(9);
			assertEquals(8500, scan.getRetentionTime());
		} catch(Exception e) {
			assertTrue("FilterException", false);
		}
	}
}
