/*******************************************************************************
 * Copyright (c) 2016, 2017 Lablicate GmbH.
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

public class AlignmentRange implements IAlignmentRange {

	private int startRetentionTime;
	private int stopRetentionTime;

	public AlignmentRange(int startRetentionTime, int stopRetentionTime) throws Exception {
		/*
		 * Validations
		 */
		if(startRetentionTime >= stopRetentionTime) {
			throw new Exception("The start retention time is >= stop retention time.");
		}
		//
		this.startRetentionTime = startRetentionTime;
		this.stopRetentionTime = stopRetentionTime;
	}

	@Override
	public int getStartRetentionTime() {

		return startRetentionTime;
	}

	@Override
	public int getStopRetentionTime() {

		return stopRetentionTime;
	}

	@Override
	public boolean equals(Object otherObject) {

		if(this == otherObject) {
			return true;
		}
		if(otherObject == null) {
			return false;
		}
		if(getClass() != otherObject.getClass()) {
			return false;
		}
		IAlignmentRange other = (IAlignmentRange)otherObject;
		return startRetentionTime == other.getStartRetentionTime() && //
				stopRetentionTime == other.getStopRetentionTime();
	}

	@Override
	public int hashCode() {

		return Integer.valueOf(startRetentionTime).hashCode() + //
				Integer.valueOf(stopRetentionTime).hashCode();
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append("[");
		builder.append("startRetentionTime=" + startRetentionTime);
		builder.append(",");
		builder.append("stopRetentionTime=" + stopRetentionTime);
		builder.append("]");
		return builder.toString();
	}
}
