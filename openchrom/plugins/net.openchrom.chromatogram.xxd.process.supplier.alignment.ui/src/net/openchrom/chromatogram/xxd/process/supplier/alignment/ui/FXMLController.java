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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

public class FXMLController implements Initializable {

	@FXML
	private AreaChart areaChart;
	private XYChart.Series series;

	private void createChart() {

		series = new XYChart.Series();
		series.setName("Fancy stuff");
		XYChart.Series series2 = new XYChart.Series();
		series2.setName("Fancy stuff 2");
		for(int i = 0; i < 100; i++) {
			series.getData().add(new XYChart.Data(i, Math.random()));
		}
		for(int i = 0; i < 100; i++) {
			series2.getData().add(new XYChart.Data(i, Math.random()));
		}
		areaChart.getData().addAll(series);
		areaChart.getData().addAll(series2);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		createChart();
	}
}
