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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.chemclipse.support.preferences.IPreferenceSupplier;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.Activator;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentRange;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.SupplierProcessorAlignmentSettings;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.ISupplierProcessorAlignmentSettings;

public class PreferenceSupplier implements IPreferenceSupplier {

	public static final String P_ALIGNMENT_METHOD = "alignmentMethod";
	public static final String DEF_ALIGNMENT_METHOD = "linear";
	public static final String P_RETENTION_TIME_WINDOW = "retentionTimeWindow";
	public static final int DEF_RETENTION_TIME_WINDOW = 200;
	//
	private static IPreferenceSupplier preferenceSupplier;

	public static IPreferenceSupplier INSTANCE() {

		if(preferenceSupplier == null) {
			preferenceSupplier = new PreferenceSupplier();
		}
		return preferenceSupplier;
	}

	@Override
	public IScopeContext getScopeContext() {

		return InstanceScope.INSTANCE;
	}

	@Override
	public String getPreferenceNode() {

		return Activator.getContext().getBundle().getSymbolicName();
	}

	@Override
	public Map<String, String> getDefaultValues() {

		Map<String, String> defaultValues = new HashMap<String, String>();
		defaultValues.put(P_ALIGNMENT_METHOD, DEF_ALIGNMENT_METHOD);
		defaultValues.put(P_RETENTION_TIME_WINDOW, Integer.toString(DEF_RETENTION_TIME_WINDOW));
		return defaultValues;
	}

	@Override
	public IEclipsePreferences getPreferences() {

		return getScopeContext().getNode(getPreferenceNode());
	}

	public static ISupplierProcessorAlignmentSettings getAlignmentProcessorSettings() {

		return new SupplierProcessorAlignmentSettings();
	}

	private static void setBasePeakSettings(ISupplierProcessorAlignmentSettings settings) {

		IEclipsePreferences preferences = INSTANCE().getPreferences();
	}
}
