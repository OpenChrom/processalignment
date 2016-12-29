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
package net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.views;

import java.net.URL;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import net.openchrom.chromatogram.xxd.process.supplier.alignment.ui.FXMLController;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SamplePart {

	private FXMLController controller;
	private FXCanvas fxCanvas;

	@PostConstruct
	public void createComposite(Composite parent) {

		init(parent);
	}

	private void createScene() {

		System.out.println("Initializing FX");
		try {
			final URL location = getClass().getResource("/fxml/FXML.fxml");
			final FXMLLoader fXMLLoader = new FXMLLoader();
			fXMLLoader.setLocation(location);
			fXMLLoader.setBuilderFactory(new JavaFXBuilderFactory());
			final Parent root = fXMLLoader.load(location.openStream());
			final Scene scene = new Scene(root);
			scene.getStylesheets().add("/styles/fxml.css");
			fxCanvas.setScene(scene);
			controller = fXMLLoader.getController();
			System.out.println("Initializing FX successful");
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}

	private void init(Composite parent) {

		// this will initialize the FX Toolkit
		fxCanvas = new FXCanvas(parent, SWT.NONE);
		Platform.setImplicitExit(false);
		Platform.runLater(() -> createScene());
	}
}