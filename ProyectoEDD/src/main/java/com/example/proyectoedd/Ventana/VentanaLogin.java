package com.example.proyectoedd.Ventana;

import com.example.proyectoedd.ui.Nav;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class VentanaLogin {
    public Parent getView() {
        TextField txtCorreo = new TextField();
        PasswordField txtPassword = new PasswordField();
        Button btnLogin = new Button("Iniciar sesión");
        Button btnIrRegistro = new Button("Crear cuenta");
        Label lblMensaje = new Label();

        btnLogin.setOnAction(e -> {
            var correo = txtCorreo.getText();
            var pass   = txtPassword.getText();
            if (correo.isEmpty() || pass.isEmpty()) {
                lblMensaje.setText("⚠️ Ingrese correo y contraseña");
            } else if (RegistroUsuarios.validarLogin(correo, pass)) {
                lblMensaje.setText("✅ Bienvenido " + correo);
                VentanaPrincipal.setUsuarioLogeado(correo);
                Nav.go(new VentanaPrincipal().getView(), "Gestión de Vuelos - PKX", 1100, 750);
            } else {
                lblMensaje.setText("❌ Credenciales inválidas");
            }
        });

        btnIrRegistro.setOnAction(e -> Nav.go(new VentanaRegistro().getView(), "Registro", 600, 480));

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15)); grid.setVgap(10); grid.setHgap(10);
        grid.add(new Label("Correo:"), 0, 0);    grid.add(txtCorreo, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);grid.add(txtPassword, 1, 1);
        grid.add(btnLogin, 1, 2); grid.add(btnIrRegistro, 1, 3); grid.add(lblMensaje, 1, 4);
        return grid;
    }
}
