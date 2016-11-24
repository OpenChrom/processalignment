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
import org.eclipse.chemclipse.msd.model.implementation.ChromatogramMSD;
import org.eclipse.chemclipse.msd.model.implementation.VendorMassSpectrum;

import junit.framework.TestCase;

public class AlignmentChromatogramTestCase extends TestCase {

	private IChromatogramMSD chromatogram;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		chromatogram = createChromatogram();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public IChromatogramMSD getChromatogram() {

		return chromatogram;
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
}
