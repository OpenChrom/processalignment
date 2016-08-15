/*******************************************************************************
 * Copyright (c) 2016 lgerber.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * lgerber - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.model;

import org.eclipse.chemclipse.model.core.IScan;

public class AlignmentProxyIScan implements IProviderIntensity, IProviderRetentionTime {

	private double retentionTime, intensity;

	public void setRetentionTime(double retentionTime) {

		this.retentionTime = retentionTime;
	}

	public void setIntensity(double intensity) {

		this.intensity = intensity;
	}

	public AlignmentProxyIScan() {
		// TODO Auto-generated constructor stub
	}

	public AlignmentProxyIScan(IScan delegate) {
		this.delegate = delegate;
	}

	private IScan delegate;

	public IScan getDelegate() {

		return delegate;
	}

	public void setDelegate(IScan delegate) {

		this.delegate = delegate;
	}

	@Override
	public double getRetentionTime() {

		if(delegate != null)
			return delegate.getRetentionTime();
		return retentionTime;
	}

	@Override
	public double getIntensity() {

		if(delegate != null)
			return delegate.getTotalSignal();
		return intensity;
	}
}
