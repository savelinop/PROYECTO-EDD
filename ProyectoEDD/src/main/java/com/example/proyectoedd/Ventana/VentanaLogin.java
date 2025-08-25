package com.example.proyectoedd.Ventana;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class VentanaLogin extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login de Usuario");

        // Campos de entrada
        TextField txtCorreo = new TextField();
        PasswordField txtPassword = new PasswordField();

        // Botón
        Button btnLogin = new Button("Iniciar sesión");

        // Mensaje
        Label lblMensaje = new Label();

        btnLogin.setOnAction(e -> {
            String correo = txtCorreo.getText();
            String password = txtPassword.getText();

            if (correo.isEmpty() || password.isEmpty()) {
                lblMensaje.setText("⚠️ Ingrese correo y contraseña");
            } else {
                if (RegistroUsuarios.validarLogin(correo, password)) {
                    lblMensaje.setText("✅ Bienvenido " + correo);

                    // cerrar login
                    primaryStage.close();

                    // abrir ventana principal
                    VentanaPrincipal.setUsuarioLogeado(correo);
                    try {
                        new VentanaPrincipal().start(new Stage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    lblMensaje.setText("❌ Credenciales inválidas");
                }
            }
        });

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Correo:"), 0, 0);
        grid.add(txtCorreo, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(txtPassword, 1, 1);
        grid.add(btnLogin, 1, 2);
        grid.add(lblMensaje, 1, 3);

        Scene scene = new Scene(grid, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}