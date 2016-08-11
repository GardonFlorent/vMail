package com.esgi.vMail.control;

import com.esgi.vMail.control.plugin.Parser;
import com.esgi.vMail.view.MainWindow;
import custo.java.nio.Directory;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws IOException {
		Parser.init1();
		ConnectionManager.init();
		MainWindow mainWindow = new MainWindow(primaryStage);
		mainWindow.getWindowStage().show();
        mainWindow.getWindowStage().setOnCloseRequest(event -> Directory.terminateWatcher());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
