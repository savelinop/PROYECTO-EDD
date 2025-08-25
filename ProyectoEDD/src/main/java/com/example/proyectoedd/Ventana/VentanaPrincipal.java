package com.example.proyectoedd.Ventana;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaPrincipal extends Application {

    private static String usuarioLogeado;

    public static void setUsuarioLogeado(String correo) {
        usuarioLogeado = correo;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Gestión de Vuelos");

        Label lblBienvenida = new Label("✈️ Bienvenido al sistema, " + usuarioLogeado);

        VBox root = new VBox(20, lblBienvenida);
        Scene scene = new Scene(root, 500, 300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
