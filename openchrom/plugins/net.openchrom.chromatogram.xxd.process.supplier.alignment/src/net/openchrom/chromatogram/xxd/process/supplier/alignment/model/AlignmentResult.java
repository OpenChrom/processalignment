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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import java.util.List;

import org.eclipse.chemclipse.model.implementation.Chromatogram;

public class AlignmentResult implements IAlignmentResult {

	private Chromatogram ticBeforeAlignment;
	private Chromatogram ticAfterAlignment;
	private List<Integer> shifts;

	public AlignmentResult() {
	}

	public void setTicBeforeAlignment(Chromatogram ticChromatogram) {

		this.ticBeforeAlignment = ticChromatogram;
	}

	public Chromatogram getTicBeforeAlignment() {

		return ticBeforeAlignment;
	}

	public void setTicAfterAlignment(Chromatogram ticChromatogram) {

		this.ticAfterAlignment = ticChromatogram;
	}

	public Chromatogram getTicAfterAlignment() {

		return ticAfterAlignment;
	}

	public void addShift(Integer shift) {

		this.shifts.add(shift);
	}

	public List<Integer> getShifts() {

		return shifts;
	}
}
