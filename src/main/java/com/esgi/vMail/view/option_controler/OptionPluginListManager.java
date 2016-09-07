package com.esgi.vMail.view.option_controler;

import com.esgi.vMail.control.plugin.Parser;
import com.fasterxml.jackson.databind.ObjectMapper;
import custo.javax.module.model.Module;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Linneya on 29/08/2016.
 */
public class OptionPluginListManager {
    private final static URL REST_URL = OptionPluginListManager.getMirror();
    private final static String EXTENTION = ".jar";

    private static URL getMirror() {
        try {
            return new URL("http://localhost:3000");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ObservableList<Module> modules = FXCollections.observableArrayList(Parser.getLoadedModules().keySet());
    private static ArrayList<LinkedHashMap> infoPluginRepo;
    public static class DisplayPluginDetails implements EventHandler<MouseEvent> {
        private TableView<Property> pluginDesc;
        public DisplayPluginDetails (TableView<Property> pluginDesc) {
            this.pluginDesc = pluginDesc;
        }
        
        @Override
        public void handle(MouseEvent event) {
            Text plugin = (Text) event.getSource();
            Module targetModule = modules.filtered(module -> module.getProperties().get("registryName").equals(plugin.getText())).get(0);
            pluginDesc.getItems().clear();
            targetModule.getProperties().forEach((key, value) -> pluginDesc.getItems().add(new Property(key, String.valueOf(value))));
            pluginDesc.refresh();
        }
    }

    public static class DisplayPluginRepoDetails implements EventHandler<MouseEvent> {
        private TableView<Property> pluginDesc;
        public DisplayPluginRepoDetails (TableView<Property> pluginDesc) {
            this.pluginDesc = pluginDesc;
        }

        @Override
        public void handle(MouseEvent event) {
            Text plugin = (Text) event.getSource();
            infoPluginRepo.forEach(linkedHashMap -> {
                if (linkedHashMap.get("registryName").equals(plugin.getText())) {
                    pluginDesc.getItems().clear();
                    linkedHashMap.forEach((key, value) -> pluginDesc.getItems().add(new Property((String) key, String.valueOf(value))));
                    pluginDesc.refresh();
                }
            });
        }
    }

    public static class UpdatePlugin implements EventHandler<MouseEvent> {
        private ListView<Text> list;
        public UpdatePlugin (ListView<Text> list) {
            this.list = list;
        }

        @Override
        public void handle(MouseEvent event) {
            try {
                URL repoUrl = new URL(REST_URL.toExternalForm() + "/pluginRepo/" + list.getSelectionModel().getSelectedItem().getText() + EXTENTION);
                System.err.println(repoUrl);
                HttpURLConnection connexion = (HttpURLConnection) repoUrl.openConnection();
                connexion.setRequestMethod("GET");
                connexion.setRequestProperty("Accept", "all");
                if (connexion.getResponseCode() != 200) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error: " + connexion.getResponseCode());
                    alert.setHeaderText("Unable to get plugin!");
                    alert.setContentText("This error can be a network error, or a server error, or the plugin just doesn't exist in the actual repo");
                    alert.showAndWait();
                    return;
                }
                if (Parser.addPlugin(connexion.getInputStream(), list.getSelectionModel().getSelectedItem().getText() + EXTENTION)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText(list.getSelectionModel().getSelectedItem().getText() + "Sucessfully installed");
                    alert.showAndWait();
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }


        }
    }

    public static class DeletePlugin implements EventHandler<MouseEvent> {
        private ListView<Text> list;
        public DeletePlugin (ListView<Text> list) {
            this.list = list;
        }

        @Override
        public void handle(MouseEvent event) {
            if (Parser.delPlugin(list.getSelectionModel().getSelectedItem().getText() + EXTENTION)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText(list.getSelectionModel().getSelectedItem().getText() + "Sucessfully installed");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fail");
                alert.setContentText(list.getSelectionModel().getSelectedItem().getText() + "not removed. What happened Here?");
                alert.showAndWait();
            }
        }
    }

    public static class UpdatePluginList implements EventHandler<MouseEvent> {
        private ListView<Text> pluginRepoList;
        public UpdatePluginList (ListView<Text> pluginRepoList) {
            this.pluginRepoList = pluginRepoList;
        }
        @Override
        public void handle(MouseEvent event) {
            try {
                URL repoPluginList = new URL(REST_URL.toExternalForm() + "/api/plugins");
                HttpURLConnection connexion = (HttpURLConnection) repoPluginList.openConnection();
                connexion.setRequestMethod("GET");
                connexion.setRequestProperty("Accept", "application/json");
                if (connexion.getResponseCode() != 200) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error: " + connexion.getResponseCode());
                    alert.setHeaderText("Unable to get plugin list!");
                    alert.setContentText("This error can be a network error, or a server error.");
                    alert.showAndWait();
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                infoPluginRepo = mapper.readValue(repoPluginList, ArrayList.class);
                pluginRepoList.getItems().clear();
                infoPluginRepo.forEach(linkedHashMap -> pluginRepoList.getItems().add(new Text((String) linkedHashMap.get("registryName"))));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Property {
        private StringProperty key = new SimpleStringProperty();

        public String getKey() {
            return keyProperty().get();
        }

        public void setKey(String key) {
            this.keyProperty().set(key);
        }

        public StringProperty keyProperty() {
            if (key == null) key = new SimpleStringProperty(this, "key");
            return key;
        }

        private StringProperty value = new SimpleStringProperty();

        public String getValue() {
            return valueProperty().get();
        }

        public void setValue(String value) {
            this.valueProperty().set(value);
        }

        public StringProperty valueProperty() {
            if (value == null) value = new SimpleStringProperty(this, "value");
            return value;
        }

        public Property (String key, String value ) {
            this.key.set(key);
            this.value.set(value);
        }
    }
}
