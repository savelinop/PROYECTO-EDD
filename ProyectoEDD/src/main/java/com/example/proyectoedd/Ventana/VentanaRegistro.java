package com.example.proyectoedd.Ventana;

import com.example.proyectoedd.ui.Nav;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class VentanaRegistro {
    public Parent getView() {
        TextField txtNombre = new TextField();
        TextField txtApellido = new TextField();
        TextField txtCorreo = new TextField();
        PasswordField txtPassword = new PasswordField();
        Button btnRegistrar = new Button("Registrar");
        Button btnVolverLogin = new Button("Volver a Login");
        Label lblMensaje = new Label();

        btnRegistrar.setOnAction(e -> {
            if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty()
                    || txtCorreo.getText().isEmpty() || txtPassword.getText().isEmpty()) {
                lblMensaje.setText("⚠️ Todos los campos son obligatorios");
            } else {
                RegistroUsuarios.guardarUsuario(
                        new Usuario(txtNombre.getText(), txtApellido.getText(), txtCorreo.getText(), txtPassword.getText())
                );
                lblMensaje.setText("✅ Usuario registrado con éxito");
                txtNombre.clear(); txtApellido.clear(); txtCorreo.clear(); txtPassword.clear();
            }
        });
        btnVolverLogin.setOnAction(e -> Nav.go(new VentanaLogin().getView(), "Login", 600, 400));

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15)); grid.setVgap(10); grid.setHgap(10);
        grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1); grid.add(txtApellido, 1, 1);
        grid.add(new Label("Correo:"), 0, 2); grid.add(txtCorreo, 1, 2);
        grid.add(new Label("Contraseña:"), 0, 3); grid.add(txtPassword, 1, 3);
        grid.add(btnRegistrar, 1, 4);
        grid.add(btnVolverLogin, 1, 5);
        grid.add(lblMensaje, 1, 6);
        return grid;
    }
}
