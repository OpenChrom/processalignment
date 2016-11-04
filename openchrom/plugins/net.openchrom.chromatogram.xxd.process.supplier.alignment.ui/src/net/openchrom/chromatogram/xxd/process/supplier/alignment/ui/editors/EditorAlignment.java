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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.selection.ChromatogramSelection;
import org.eclipse.chemclipse.model.selection.IChromatogramSelection;
import org.eclipse.chemclipse.support.events.IPerspectiveAndViewIds;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.forms.widgets.FormToolkit;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.AlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResult;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IAlignmentResults;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.IDataInputEntry;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.model.ISample;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.AlignmentSettings;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.settings.IAlignmentSettings;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.runnable.ApplyAlignmentRunnable;
import net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.internal.runnable.CalculateAlignmentRunnable;

public class EditorAlignment {

	public static final String ID = "net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.editors.editorAlignment";
	public static final String CONTRIBUTION_URI = "bundleclass://net.openchrom.chromatogram.xxd.process.supplier.alignment.ui/net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.editors.EditorAlignment";
	public static final String ICON_URI = "platform:/plugin/org.eclipse.chemclipse.rcp.ui.icons/icons/16x16/chromatogram.gif";
	public static final String TOOLTIP = "Alignment Editor";
	//
	private static final Logger logger = Logger.getLogger(EditorAlignment.class);
	/*
	 * Injected member in constructor
	 */
	@Inject
	private MPart part;
	@Inject
	private MDirtyable dirtyable;
	@Inject
	private MApplication application;
	@Inject
	private EModelService modelService;
	/*
	 * Showing additional info in tabs.
	 */
	private TabFolder tabFolder;
	private FormToolkit formToolkit;
	/*
	 * Pages
	 */
	private PageOverview pageOverview;
	private PageInputFiles pageInputFiles;
	private PageProcessingWindows pageProcessingWindows;
	private PageResults pageResults;
	private List<Object> pages;
	private IAlignmentResults alignmentResults;

	public EditorAlignment() {
		//
		pages = new ArrayList<Object>();
	}

	@PostConstruct
	private void createControl(Composite parent) {

		createPages(parent);
	}

	@Focus
	public void setFocus() {

		tabFolder.setFocus();
	}

	@PreDestroy
	private void preDestroy() {

		/*
		 * Remove the editor from the listed parts.
		 */
		if(modelService != null) {
			MPartStack partStack = (MPartStack)modelService.find(IPerspectiveAndViewIds.EDITOR_PART_STACK_ID, application);
			part.setToBeRendered(false);
			part.setVisible(false);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					partStack.getChildren().remove(part);
				}
			});
		}
		/*
		 * Dispose the form toolkit.
		 */
		if(formToolkit != null) {
			formToolkit.dispose();
		}
		/*
		 * Run the garbage collector.
		 */
		System.gc();
	}

	@Persist
	public void save() {

		System.out.println("Save results to chromatogram files.");
	}

	public void calculateAlignment() {

		dirtyable.setDirty(true);
		List<IDataInputEntry> dataInputEntries = pageInputFiles.getDataInputEntries();
		/*
		 * Run the process.
		 */
		IAlignmentSettings alignmentSettings = new AlignmentSettings();
		alignmentSettings.getAlignmentRanges().clear();
		alignmentSettings.getAlignmentRanges().addAll(pageProcessingWindows.getAlignmentRanges());
		CalculateAlignmentRunnable runnable = new CalculateAlignmentRunnable(dataInputEntries, alignmentSettings);
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		try {
			/*
			 * Calculate the results and show the score plot page.
			 */
			monitor.run(true, true, runnable);
			//
			List<IChromatogramSelection> chromatogramSelectionsRaw = new ArrayList<IChromatogramSelection>();
			List<IChromatogramSelection> chromatogramSelectionsShifted = new ArrayList<IChromatogramSelection>();
			//
			IAlignmentResults aligmentResults = runnable.getAlignmentResults();
			Map<ISample, IAlignmentResult> alignmentResultMap = aligmentResults.getAlignmentResultMap();
			for(Entry<ISample, IAlignmentResult> entry : alignmentResultMap.entrySet()) {
				IAlignmentResult alignmentResult = entry.getValue();
				try {
					chromatogramSelectionsRaw.add(new ChromatogramSelection(alignmentResult.getTicBeforeAlignment()));
					chromatogramSelectionsShifted.add(new ChromatogramSelection(alignmentResult.getTicAfterAlignment()));
				} catch(Exception e) {
					logger.warn(e);
				}
			}
			/*
			 * Show the results.
			 */
			pageResults.setChromatogramData(chromatogramSelectionsRaw, chromatogramSelectionsShifted);
			//
			reloadResults();
			reloadProcessingWindows();
		} catch(InvocationTargetException e) {
			logger.warn(e);
			logger.warn(e.getCause());
		} catch(InterruptedException e) {
			logger.warn(e);
		}
	}
	
	public void applyAlignment(){
		
		List<IDataInputEntry> dataInputEntries = pageInputFiles.getDataInputEntries();
		IAlignmentSettings alignmentSettings = new AlignmentSettings();
		IAlignmentResults alignmentResults = new AlignmentResults();
		alignmentSettings.getAlignmentRanges().clear();
		alignmentSettings.getAlignmentRanges().addAll(pageProcessingWindows.getAlignmentRanges());
		ApplyAlignmentRunnable runnable = new ApplyAlignmentRunnable(dataInputEntries, alignmentSettings, alignmentResults);
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		try {
			/*
			 * Apply the results and save the files.
			 */
			monitor.run(true, true, runnable);
			//
			} catch(InvocationTargetException e) {
				logger.warn(e);
				logger.warn(e.getCause());
				
			} catch(InterruptedException e) {
				logger.warn(e);
				
			
			}
	}
	

	public int getChromatogramType() {

		return pageOverview.getChromatogramType();
	}

	public void showOverviewFilesPage() {

		int pageIndex = 0;
		for(int index = 0; index < pages.size(); index++) {
			if(pages.get(index) == pageOverview) {
				pageIndex = index;
			}
		}
		tabFolder.setSelection(pageIndex);
	}

	public void showInputFilesPage() {

		int pageIndex = 0;
		for(int index = 0; index < pages.size(); index++) {
			if(pages.get(index) == pageInputFiles) {
				pageIndex = index;
			}
		}
		tabFolder.setSelection(pageIndex);
	}

	public void showProcessingWindowsPage() {

		int pageIndex = 0;
		for(int index = 0; index < pages.size(); index++) {
			if(pages.get(index) == pageProcessingWindows) {
				pageIndex = index;
			}
		}
		tabFolder.setSelection(pageIndex);
	}

	public void showResultsPage() {

		int pageIndex = 0;
		for(int index = 0; index < pages.size(); index++) {
			if(pages.get(index) == pageResults) {
				pageIndex = index;
			}
		}
		tabFolder.setSelection(pageIndex);
	}

	public void calculateOverlayTicsPriorAlignment() {

	}

	private void createPages(Composite parent) {

		part.setLabel("Alignment");
		tabFolder = new TabFolder(parent, SWT.BOTTOM);
		//
		pages.add(pageOverview = new PageOverview(this, tabFolder, formToolkit));
		pages.add(pageInputFiles = new PageInputFiles(this, tabFolder));
		pages.add(pageProcessingWindows = new PageProcessingWindows(this, tabFolder));
		pages.add(pageResults = new PageResults(this, tabFolder));
	}

	private void reloadResults() {

		pageResults.update();
	}

	public void reloadProcessingWindows() {

		pageProcessingWindows.update();
	}

	public IAlignmentResults getAlignmentResults() {

		return alignmentResults;
	}
}
