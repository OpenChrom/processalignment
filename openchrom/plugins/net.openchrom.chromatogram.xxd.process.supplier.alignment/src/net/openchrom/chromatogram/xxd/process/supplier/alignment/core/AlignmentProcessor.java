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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.core;

import java.util.List;

import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.core.runtime.IProgressMonitor;

public class AlignmentProcessor {

	public IProcessingInfo alignChromatograms(List<String> chromatograms, IProgressMonitor monitor) {

		IProcessingInfo processingInfo = new ProcessingInfo();
		for(String chromatogram : chromatograms) {
			System.out.println("Process chromatogram: " + chromatogram);
		}
		//
		processingInfo.addInfoMessage("Chromatogram Aligment", "Done");
		return processingInfo;
	}
}
