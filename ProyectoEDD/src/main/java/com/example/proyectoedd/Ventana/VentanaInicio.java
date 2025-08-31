package com.example.proyectoedd.Ventana;

import com.example.proyectoedd.ui.Nav;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class VentanaInicio {
    public Parent getView() {
        Button btnRegistrar = new Button("Registrar usuario");
        Button btnLogin     = new Button("Iniciar sesión");

        btnRegistrar.setOnAction(e -> Nav.go(new VentanaRegistro().getView(), "Registro", 600, 480));
        btnLogin.setOnAction(e     -> Nav.go(new VentanaLogin().getView(), "Login", 600, 400));

        VBox root = new VBox(20, btnRegistrar, btnLogin);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        return root;
    }
}
