package com.example.proyectoedd.Ventana;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaInicio extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Gestión de Vuelos ✈️");

        Button btnRegistrar = new Button("Registrar usuario");
        Button btnLogin = new Button("Iniciar sesión");

        btnRegistrar.setOnAction(e -> {
            try {
                new VentanaRegistro().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnLogin.setOnAction(e -> {
            try {
                new VentanaLogin().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(20, btnRegistrar, btnLogin);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

