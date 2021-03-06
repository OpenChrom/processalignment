/*******************************************************************************
 * Copyright (c) 2016, 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.runnable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.core.AlignmentProcessor;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.IAlignmentSettings;

public class CalculateAlignmentRunnable implements IRunnableWithProgress {

	private List<IDataInputEntry> dataInputEntries;
	private AlignmentResults alignmentResults;
	private IAlignmentSettings alignmentSettings;

	public CalculateAlignmentRunnable(List<IDataInputEntry> dataInputEntries, IAlignmentSettings alignmentSettings) {
		this.dataInputEntries = dataInputEntries;
		this.alignmentSettings = alignmentSettings;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		AlignmentProcessor alignmentProcessor = new AlignmentProcessor();
		alignmentResults = alignmentProcessor.calculateAlignment(dataInputEntries, alignmentSettings, monitor);
	}

	public AlignmentResults getAlignmentResults() {

		return alignmentResults;
	}
}