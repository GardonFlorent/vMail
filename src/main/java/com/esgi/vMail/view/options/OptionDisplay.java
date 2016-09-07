package com.esgi.vMail.view.options;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class OptionDisplay extends OptionBuilder {

	@Override
	String getOptionName() {
		return "display";
	}

	@Override
	Pane makeOptionPane() {
		// TODO Auto-generated method stub
		return new BorderPane();
	}

}
