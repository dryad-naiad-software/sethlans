package com.dryadandnaiad.sethlans.gui;

import com.dryadandnaiad.sethlans.utils.Configuration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class SethlansGUI extends Application {
    private Configuration config;

    @Override
    public void start(Stage stage) throws Exception {
        config = Configuration.getInstance();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("Sethlans");
        stage.setScene(scene);
        stage.show();
    }
}
