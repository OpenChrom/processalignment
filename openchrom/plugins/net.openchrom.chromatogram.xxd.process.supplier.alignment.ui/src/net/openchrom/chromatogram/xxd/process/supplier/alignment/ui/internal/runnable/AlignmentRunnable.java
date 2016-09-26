/*******************************************************************************
 * Copyright (c) 2016 Lablicate GmbH.
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
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.SupplierProcessorAlignmentSettings;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;

public class AlignmentRunnable implements IRunnableWithProgress {

	private List<IDataInputEntry> dataInputEntries;
	private IAlignmentResults alignmentResults;

	public AlignmentRunnable(List<IDataInputEntry> dataInputEntries) {
		this.dataInputEntries = dataInputEntries;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		AlignmentProcessor alignmentProcessor = new AlignmentProcessor();
		SupplierProcessorAlignmentSettings settings = new SupplierProcessorAlignmentSettings();
		alignmentResults = alignmentProcessor.calculateAlignment(dataInputEntries, settings, monitor);
	}

	public IAlignmentResults getAlignmentResults() {

		return alignmentResults;
	}
}
