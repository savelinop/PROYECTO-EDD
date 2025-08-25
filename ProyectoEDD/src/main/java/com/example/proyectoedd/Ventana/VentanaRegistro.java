package com.example.proyectoedd.Ventana;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class VentanaRegistro extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Registro de Usuario");

        // Campos de entrada
        TextField txtNombre = new TextField();
        TextField txtApellido = new TextField();
        TextField txtCorreo = new TextField();
        PasswordField txtPassword = new PasswordField();

        // Botón de registro
        Button btnRegistrar = new Button("Registrar");

        // Mensaje
        Label lblMensaje = new Label();

        btnRegistrar.setOnAction(e -> {
            String nombre = txtNombre.getText();
            String apellido = txtApellido.getText();
            String correo = txtCorreo.getText();
            String password = txtPassword.getText();

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                lblMensaje.setText("⚠️ Todos los campos son obligatorios");
            } else {
                Usuario usuario = new Usuario(nombre, apellido, correo, password);
                RegistroUsuarios.guardarUsuario(usuario);
                lblMensaje.setText("✅ Usuario registrado con éxito");

                // limpiar campos
                txtNombre.clear();
                txtApellido.clear();
                txtCorreo.clear();
                txtPassword.clear();
            }
        });

        // Layout con GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(txtApellido, 1, 1);
        grid.add(new Label("Correo:"), 0, 2);
        grid.add(txtCorreo, 1, 2);
        grid.add(new Label("Contraseña:"), 0, 3);
        grid.add(txtPassword, 1, 3);
        grid.add(btnRegistrar, 1, 4);
        grid.add(lblMensaje, 1, 5);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}

