package com.esgi.vMail.view.options;

import com.esgi.vMail.control.LangManager;
import com.esgi.vMail.control.plugin.Parser;
import com.esgi.vMail.view.option_controler.OptionPluginListManager;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

/**
 * Created by Linneya on 29/08/2016.
 */
public class OptionPlugins extends OptionBuilder {



    @Override
    String getOptionName() {
        return "plugins";
    }

    @Override
    Pane makeOptionPane() {
        Tab installedPlugins = new Tab(LangManager.text("settings.list.plugins.tab.installed"));
        installedPlugins.setClosable(false);
        installedPlugins.setContent(this.makeInstalledPluginPane());
        Tab availablePlugins = new Tab(LangManager.text("settings.list.plugins.tab.available"));
        availablePlugins.setClosable(false);
        availablePlugins.setContent(this.makeAvailablePluginPane());
        TabPane pane = new TabPane(installedPlugins, availablePlugins);
        AnchorPane container = new AnchorPane(pane);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
        return container;
    }

    private SplitPane makeAvailablePluginPane() {
        SplitPane pane = new SplitPane();
        BorderPane pluginPane = new BorderPane();
        ListView<Text> pluginList = new ListView<>();
        pane.setOrientation(Orientation.HORIZONTAL);
        pluginList.setPrefWidth(Double.MAX_VALUE);

        //Table Init
        TableView<OptionPluginListManager.Property> pluginDesc = new TableView<>();
        pluginDesc.setPrefWidth(Double.MAX_VALUE);
        //Event for each plugins
        pluginList.getItems().addListener((ListChangeListener<? super Text>) change -> {
            while (change.next()) {
                if (change.wasAdded()) change.getAddedSubList().forEach(text -> text.setOnMouseClicked(new OptionPluginListManager.DisplayPluginRepoDetails(pluginDesc)));
            }
        });

        TableColumn<OptionPluginListManager.Property, String> col1 = new TableColumn<>(LangManager.text("settings.list.plugins.tab.installed.table.attribute"));
        col1.setResizable(false);
        col1.prefWidthProperty().bind(pluginDesc.widthProperty().divide(2));
        col1.setCellValueFactory(new PropertyValueFactory<>("key"));
        //Value column for table
        TableColumn<OptionPluginListManager.Property, String> col2 = new TableColumn<>(LangManager.text("settings.list.plugins.tab.installed.table.value"));
        col2.setResizable(false);
        col2.prefWidthProperty().bind(pluginDesc.widthProperty().divide(2));
        col2.setCellValueFactory(new PropertyValueFactory<>("value"));
        pluginDesc.getColumns().addAll(col1, col2);

        //Maj Install
        Button btnInstall = new Button(LangManager.text("settings.list.plugins.tab.available.handler.install"));
        btnInstall.setOnMouseClicked(new OptionPluginListManager.UpdatePlugin(pluginList));
        HBox pluginHandler = new HBox(btnInstall);

        Button btnRefresh = new Button(LangManager.text("settings.list.plugins.tab.available.handler.refresh"));
        btnRefresh.setOnMouseClicked(new OptionPluginListManager.UpdatePluginList(pluginList));
        HBox pluginRefresh = new HBox(btnRefresh);



        //La table doit se trouver dans le pluginpane, avec un boutton désinstaller et mettre a jour.
        pluginPane.setTop(pluginRefresh);
        pluginPane.setCenter(pluginDesc);
        pluginPane.setBottom(pluginHandler);
        pane.getItems().addAll(pluginList, pluginPane);

        return pane;
    }

    private SplitPane makeInstalledPluginPane() {
        SplitPane pane = new SplitPane();
        BorderPane pluginPane = new BorderPane();
        //List of the plugins
        ListView<Text> pluginList = new ListView<>();
        pluginList.setPrefWidth(Double.MAX_VALUE);

        //Table Init
        TableView<OptionPluginListManager.Property> pluginDesc = new TableView<>();
        pluginDesc.setPrefWidth(Double.MAX_VALUE);
        //Event for each plugins
        pluginList.getItems().addListener((ListChangeListener<? super Text>) change -> {
            while (change.next()) {
                if (change.wasAdded()) change.getAddedSubList().forEach(text -> text.setOnMouseClicked(new OptionPluginListManager.DisplayPluginDetails(pluginDesc)));
            }
        });
        //Fill list
        Parser.getLoadedModules().keySet().forEach(module -> pluginList.getItems().add(new Text(String.valueOf(module.getProperties().get("registryName")))));
        //Key column for table
        TableColumn<OptionPluginListManager.Property, String> col1 = new TableColumn<>(LangManager.text("settings.list.plugins.tab.installed.table.attribute"));
        col1.setResizable(false);
        col1.prefWidthProperty().bind(pluginDesc.widthProperty().divide(2));
        col1.setCellValueFactory(new PropertyValueFactory<>("key"));
        //Value column for table
        TableColumn<OptionPluginListManager.Property, String> col2 = new TableColumn<>(LangManager.text("settings.list.plugins.tab.installed.table.value"));
        col2.setResizable(false);
        col2.prefWidthProperty().bind(pluginDesc.widthProperty().divide(2));
        col2.setCellValueFactory(new PropertyValueFactory<>("value"));
        pluginDesc.getColumns().addAll(col1, col2);

        //Maj button + Uninstall
        Button btnMaj = new Button(LangManager.text("settings.list.plugins.tab.installed.handler.maj"));
        btnMaj.setOnMouseClicked(new OptionPluginListManager.UpdatePlugin(pluginList));
        Button btnDel = new Button(LangManager.text("settings.list.plugins.tab.installed.handler.del"));
        btnDel.setOnMouseClicked(new OptionPluginListManager.DeletePlugin(pluginList));

        HBox pluginHandler = new HBox(btnMaj, btnDel);

        //La table doit se trouver dans le pluginpane, avec un boutton désinstaller et mettre a jour.
        pluginPane.setCenter(pluginDesc);
        pluginPane.setBottom(pluginHandler);
        pane.getItems().addAll(pluginList, pluginPane);
        return pane;
    }
}
