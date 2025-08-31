package com.example.proyectoedd.ui;

import com.example.proyectoedd.Ventana.VentanaInicio;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Inicializa el router con una escena base
        Nav.init(stage, 600, 400);

        // Pantalla inicial (desde ahí vas a Login/Registro y luego a Principal)
        Nav.go(new VentanaInicio().getView(), "Inicio", 600, 400);
    }

    // Si quieres lógica al cerrar (guardar CSV, etc.), hazlo en VentanaPrincipal
    @Override
    public void stop() {
        // vacío a propósito
    }
}
