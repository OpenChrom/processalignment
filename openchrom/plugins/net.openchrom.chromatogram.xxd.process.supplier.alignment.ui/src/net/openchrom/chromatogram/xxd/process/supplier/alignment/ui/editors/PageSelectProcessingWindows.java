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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.editors;

import org.eclipse.chemclipse.thirdpartylibraries.swtchart.ext.InteractiveChartExtended;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

public class PageSelectProcessingWindows {

	private Color COLOR_BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	private Color COLOR_WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	private int SYMBOL_SIZE = 8;
	private EditorAlignment editorAlignment;
	private InteractiveChartExtended scorePlotChart;

	public PageSelectProcessingWindows(EditorAlignment editorAlignment, TabFolder tabFolder, FormToolkit formToolkit) {
		//
		this.editorAlignment = editorAlignment;
		initialize(tabFolder, formToolkit);
	}

	public void update() {

	}

	private void initialize(TabFolder tabFolder, FormToolkit formToolkit) {

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Select Processing Window");
		//
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());
		/*
		 * Composite parent = new Composite(composite, SWT.NONE);
		 * parent.setLayout(new GridLayout(1, true));
		 * parent.setLayoutData(GridData.FILL_BOTH);
		 */
		/*
		 * Forms API
		 */
		formToolkit = new FormToolkit(composite.getDisplay());
		ScrolledForm scrolledForm = formToolkit.createScrolledForm(composite);
		Composite scrolledFormComposite = scrolledForm.getBody();
		formToolkit.decorateFormHeading(scrolledForm.getForm());
		scrolledFormComposite.setLayout(new TableWrapLayout());
		scrolledForm.setText("Chromatogram Alignment");
		createTicOverlaySection(scrolledForm, formToolkit);
		createProcessingTableSection(scrolledForm, formToolkit);
		tabItem.setControl(composite);
	}

	private void createTicOverlaySection(Composite parent, FormToolkit formToolkit) {

		/*
		 * Section
		 */
		Section section = formToolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Properties");
		section.setDescription("Use the properties to define the retention time window.");
		section.marginWidth = 5;
		section.marginHeight = 5;
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		/*
		 * Client
		 */
		Composite client = formToolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		Label label = formToolkit.createLabel(client, "Select the Alignment settings:");
		label.setLayoutData(gridData);
		/*
		 * Plot the PCA chart.
		 */
		Composite chartComposite = new Composite(parent, SWT.NONE);
		chartComposite.setLayout(new GridLayout(1, true));
		chartComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		scorePlotChart = new InteractiveChartExtended(chartComposite, SWT.NONE);
		scorePlotChart.setLayoutData(new GridData(GridData.FILL_BOTH));
		scorePlotChart.getTitle().setText("PCA Score Plot");
		scorePlotChart.getTitle().setForeground(COLOR_BLACK);
		//
		scorePlotChart.setBackground(COLOR_WHITE);
		scorePlotChart.getLegend().setVisible(false);
		//
		scorePlotChart.getAxisSet().getXAxis(0).getTitle().setText("PC");
		scorePlotChart.getAxisSet().getXAxis(0).getTitle().setForeground(COLOR_BLACK);
		scorePlotChart.getAxisSet().getXAxis(0).getTick().setForeground(COLOR_BLACK);
		//
		scorePlotChart.getAxisSet().getYAxis(0).getTitle().setText("PC");
		scorePlotChart.getAxisSet().getYAxis(0).getTitle().setForeground(COLOR_BLACK);
		scorePlotChart.getAxisSet().getYAxis(0).getTick().setForeground(COLOR_BLACK);
		//
		IPlotArea plotArea = (IPlotArea)scorePlotChart.getPlotArea();
		/*
		 * Plot a marker at zero.
		 */
		plotArea.addCustomPaintListener(new ICustomPaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				Range xRange = scorePlotChart.getAxisSet().getXAxes()[0].getRange();
				Range yRange = scorePlotChart.getAxisSet().getYAxes()[0].getRange();
				/*
				 * Mark the zero lines if possible.
				 * Otherwise draw the marker in half width.
				 */
				if(xRange.lower < 0 && xRange.upper > 0 && yRange.lower < 0 && yRange.upper > 0) {
					Rectangle rectangle = scorePlotChart.getPlotArea().getClientArea();
					int width = rectangle.width;
					int height = rectangle.height;
					int xWidth;
					int yHeight;
					/*
					 * Dependent where the zero values are.
					 * xDelta and yDelta can't be zero -> protect from division by zero.
					 */
					double xDelta = xRange.upper - xRange.lower;
					double yDelta = yRange.upper - yRange.lower;
					double xDiff = xRange.lower * -1; // lower is negative
					double yDiff = yRange.upper;
					double xPart = ((100 / xDelta) * xDiff) / 100; // percent -> 0.0 - 1.0
					double yPart = ((100 / yDelta) * yDiff) / 100; // percent -> 0.0 - 1.0
					xWidth = (int)(width * xPart);
					yHeight = (int)(height * yPart);
					/*
					 * Draw the line.
					 */
					e.gc.setForeground(COLOR_BLACK);
					e.gc.drawLine(xWidth, 0, xWidth, height); // Vertical line through zero
					e.gc.drawLine(0, yHeight, width, yHeight); // Horizontal line through zero
				}
			}

			@Override
			public boolean drawBehindSeries() {

				return false;
			}
		});
		/*
		 * Plot the series name above the entry.
		 */
		plotArea.addCustomPaintListener(new ICustomPaintListener() {

			@Override
			public void paintControl(PaintEvent e) {

				ISeriesSet seriesSet = scorePlotChart.getSeriesSet();
				ISeries[] series = seriesSet.getSeries();
				for(ISeries serie : series) {
					String label = serie.getId();
					Point point = serie.getPixelCoordinates(0);
					/*
					 * Draw the label
					 */
					Point labelSize = e.gc.textExtent(label);
					e.gc.setForeground(COLOR_BLACK);
					e.gc.drawText(label, (int)(point.x - labelSize.x / 2.0d), (int)(point.y - labelSize.y - SYMBOL_SIZE / 2.0d), true);
				}
			}

			@Override
			public boolean drawBehindSeries() {

				return false;
			}
		});
		//
		// tabItem.setControl(composite);
		/*
		 * Add the client to the section and paint flat borders.
		 */
		section.setClient(client);
		formToolkit.paintBordersFor(client);
	}

	private void createProcessingTableSection(Composite parent, FormToolkit formToolkit) {

		Label label;
		/*
		 * Section
		 */
		Section section = formToolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Evaluation");
		section.setDescription("Run the alignment evaluation after the entries have been edited.");
		section.marginWidth = 5;
		section.marginHeight = 5;
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		/*
		 * Client
		 */
		Composite client = formToolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = 20;
		gridData.heightHint = 30;
		/*
		 * Input files section.
		 */
		label = formToolkit.createLabel(client, "Select the input chromatograms:\n");
		label.setLayoutData(gridData);
		/*
		 * Add the client to the section and paint flat borders.
		 */
		section.setClient(client);
		formToolkit.paintBordersFor(client);
	}
}
