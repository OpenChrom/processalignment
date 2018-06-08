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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IPeak;

public class AlignmentResult implements IAlignmentResult {

	private IChromatogram<? extends IPeak> ticBeforeAlignment;
	private IChromatogram<? extends IPeak> ticAfterAlignment;
	private List<Integer> shifts;

	public AlignmentResult() {
		this.shifts = new ArrayList<Integer>();
	}

	@Override
	public void setTicBeforeAlignment(IChromatogram<? extends IPeak> ticChromatogram) {

		this.ticBeforeAlignment = ticChromatogram;
	}

	@Override
	public IChromatogram<? extends IPeak> getTicBeforeAlignment() {

		return ticBeforeAlignment;
	}

	@Override
	public void setTicAfterAlignment(IChromatogram<? extends IPeak> ticChromatogram) {

		this.ticAfterAlignment = ticChromatogram;
	}

	@Override
	public IChromatogram<? extends IPeak> getTicAfterAlignment() {

		return ticAfterAlignment;
	}

	@Override
	public void addShift(Integer shift) {

		this.shifts.add(shift);
	}

	@Override
	public List<Integer> getShifts() {

		return shifts;
	}
}
