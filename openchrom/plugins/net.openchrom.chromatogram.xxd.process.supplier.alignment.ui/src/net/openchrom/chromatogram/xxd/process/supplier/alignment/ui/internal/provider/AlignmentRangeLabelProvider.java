/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.provider;

import java.text.DecimalFormat;

import org.eclipse.chemclipse.model.core.AbstractChromatogram;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.text.ValueFormat;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentRange;

public class AlignmentRangeLabelProvider extends LabelProvider implements ITableLabelProvider {

	private DecimalFormat decimalFormat = ValueFormat.getDecimalFormatEnglish();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if(columnIndex == 0) {
			return ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CONFIGURE, IApplicationImage.SIZE_16x16);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {

		String text = "";
		if(element instanceof IAlignmentRange) {
			IAlignmentRange aligmentRange = (IAlignmentRange)element;
			switch(columnIndex) {
				case 0:
					text = decimalFormat.format(aligmentRange.getStartRetentionTime() / AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
					break;
				case 1:
					text = decimalFormat.format(aligmentRange.getStopRetentionTime() / AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
					break;
				default:
					text = "n.a.";
			}
		}
		return text;
	}
}
