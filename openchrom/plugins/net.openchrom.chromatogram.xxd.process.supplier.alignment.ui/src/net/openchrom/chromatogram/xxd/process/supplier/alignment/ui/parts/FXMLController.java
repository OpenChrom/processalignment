/*******************************************************************************
 * Copyright (c) 2016 Lablicate UG (haftungsbeschränkt).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lorenz Gerber - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.parts;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

@SuppressWarnings("rawtypes")
public class FXMLController implements Initializable {

	@FXML
	private AreaChart areaChart;
	private XYChart.Series series;

	@SuppressWarnings("unchecked")
	private void createChart() {

		series = new XYChart.Series();
		areaChart.getData().addAll(series);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		createChart();
	}
}
