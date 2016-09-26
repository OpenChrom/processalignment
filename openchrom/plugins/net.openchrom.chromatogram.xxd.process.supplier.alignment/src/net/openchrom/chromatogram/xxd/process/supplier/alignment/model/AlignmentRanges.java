/*******************************************************************************
 * Copyright (c) 2016 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Lorenz Gerber - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import java.util.ArrayList;
import java.util.Iterator;

public class AlignmentRanges extends ArrayList<IAlignmentRange> implements IAlignmentRanges {

	/**
	 * Renew this UUID on class change.
	 */
	private static final long serialVersionUID = -6833457252061925168L;

	@Override
	public int getLowestStartRetentionTime() {

		int lowestRetentionTime = 0;
		Iterator<IAlignmentRange> iterator = this.iterator();
		while(iterator.hasNext()) {
			IAlignmentRange alignmentRange = iterator.next();
			if(alignmentRange.getStartRetentionTime() < lowestRetentionTime) {
				lowestRetentionTime = alignmentRange.getStartRetentionTime();
			}
		}
		return lowestRetentionTime;
	}

	@Override
	public int getHighestStopRetentionTime() {

		int highestRetentionTime = 0;
		Iterator<IAlignmentRange> iterator = this.iterator();
		while(iterator.hasNext()) {
			IAlignmentRange alignmentRange = iterator.next();
			if(alignmentRange.getStopRetentionTime() > highestRetentionTime) {
				highestRetentionTime = alignmentRange.getStopRetentionTime();
			}
		}
		return highestRetentionTime;
	}
}
