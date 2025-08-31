package com.example.proyectoedd.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Nav {
    private static Stage stage;
    private static Scene scene;

    public static void init(Stage primary, double w, double h) {
        stage = primary;
        scene = new Scene(new javafx.scene.Group(), w, h);
        stage.setScene(scene);
        stage.show();
    }

    public static void go(Parent root, String title, double w, double h) {
        scene.setRoot(root);
        stage.setTitle(title);
        stage.setWidth(w);
        stage.setHeight(h);
        stage.centerOnScreen();
    }
}
