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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import java.util.List;

import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IPeak;

public interface IAlignmentResult {

	void setTicBeforeAlignment(IChromatogram<? extends IPeak> ticChromatogram);

	IChromatogram<? extends IPeak> getTicBeforeAlignment();

	void setTicAfterAlignment(IChromatogram<? extends IPeak> ticChromatogram);

	IChromatogram<? extends IPeak> getTicAfterAlignment();

	void addShift(Integer shift);

	List<Integer> getShifts();
}
