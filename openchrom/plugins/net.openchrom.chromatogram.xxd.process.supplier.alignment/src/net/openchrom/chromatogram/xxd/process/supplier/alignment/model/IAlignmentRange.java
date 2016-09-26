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

public interface IAlignmentRange {

	String[] TITLES = {"Start RT (min)", "Stop RT (min)"};
	int[] BOUNDS = {160, 160};

	int getStartRetentionTime();

	int getStopRetentionTime();
}
