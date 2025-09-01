package com.example.proyectoedd.Ventana;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class VentanaRegistro {

    private TextField txtNombre, txtCorreo;
    private PasswordField txtPass, txtPass2;
    private Label lblMsg;

    public Parent getView() {
        StackPane bg = new StackPane();
        bg.setStyle("-fx-background-color: linear-gradient(to bottom, #0b1020, #18233a);");
        bg.setPadding(new Insets(24));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);
        card.setStyle("""
            -fx-background-color: rgba(17,24,39,0.9);
            -fx-background-radius:16;
            -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.42), 24, 0.25, 0, 8);
            -fx-border-color: rgba(255,255,255,0.06);
            -fx-border-radius: 16;
            -fx-border-width:1;
        """);

        Label title = new Label("Crear cuenta");
        title.setStyle("-fx-text-fill:#e5e7eb; -fx-font-size:20px; -fx-font-weight:700;");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);

        txtNombre = new TextField(); styleField(txtNombre, "Nombre completo");
        txtCorreo = new TextField(); styleField(txtCorreo, "correo@ejemplo.com");
        txtPass   = new PasswordField(); styleField(txtPass, "Contraseña");
        txtPass2  = new PasswordField(); styleField(txtPass2, "Repetir contraseña");

        form.add(coloredLabel("Nombre"), 0, 0); form.add(txtNombre, 1, 0);
        form.add(coloredLabel("Correo"), 0, 1); form.add(txtCorreo, 1, 1);
        form.add(coloredLabel("Contraseña"), 0, 2); form.add(txtPass, 1, 2);
        form.add(coloredLabel("Repetir"), 0, 3); form.add(txtPass2, 1, 3);

        HBox actions = new HBox(10);
        Button btnRegistrar = new Button("Registrar");
        Button btnLogin = new Button("Ya tengo cuenta");
        styleSuccess(btnRegistrar);   // verde
        styleGhost(btnLogin);         // enlace/outline

        lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill:#ef4444; -fx-font-size:12px;");

        btnRegistrar.setDefaultButton(true);
        btnRegistrar.setOnAction(e -> onRegistrar());
        btnLogin.setOnAction(e -> VentanaInicio.go(btnLogin, new VentanaLogin().getView(), "Login"));

        actions.getChildren().addAll(btnRegistrar, btnLogin);

        card.getChildren().addAll(title, form, lblMsg, actions);
        bg.getChildren().add(card);
        return bg;
    }

    private void onRegistrar() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String p1 = txtPass.getText();
        String p2 = txtPass2.getText();

        if (nombre.isBlank() || correo.isBlank() || p1.isBlank() || p2.isBlank()) {
            lblMsg.setText("⚠️ Complete todos los campos"); return;
        }
        if (!p1.equals(p2)) { lblMsg.setText("⚠️ Las contraseñas no coinciden"); return; }

        var reg = RegistroUsuarios.getInstance();
        if (reg.existeCorreo(correo)) { lblMsg.setText("⚠️ Ya existe un usuario con ese correo"); return; }

        reg.registrar(nombre, correo, p1);
        new Alert(Alert.AlertType.INFORMATION, "Registro exitoso. Ahora puede iniciar sesión.").showAndWait();
        limpiarForm();
    }

    private void limpiarForm() {
        txtNombre.clear(); txtCorreo.clear(); txtPass.clear(); txtPass2.clear();
        lblMsg.setText("");
    }

    /* ---------- helpers de estilo ---------- */
    private static Label coloredLabel(String s){
        Label l = new Label(s);
        l.setStyle("-fx-text-fill:#cbd5e1; -fx-font-size:12px; -fx-font-weight:700;");
        return l;
    }

    private static void styleField(TextField f, String prompt){
        f.setPromptText(prompt);
        f.setStyle("""
            -fx-background-color:#111827;
            -fx-text-fill:#e5e7eb;
            -fx-prompt-text-fill:#6b7280;
            -fx-background-radius:10;
            -fx-border-radius:10;
            -fx-border-color:#1f2937;
            -fx-padding:9 12;
            -fx-font-size:13px;
        """);
        f.focusedProperty().addListener((o,a,b)->{
            if(b) f.setStyle(f.getStyle()+"-fx-border-color:#06b6d4; -fx-border-width:1.2;");
            else  f.setStyle(f.getStyle()+"-fx-border-color:#1f2937; -fx-border-width:1;");
        });
    }

    private static void baseButton(Button b) {
        b.setStyle("""
            -fx-background-radius: 10;
            -fx-padding: 10 16;
            -fx-font-size: 13px;
            -fx-font-weight: 700;
        """);
        b.setOnMouseEntered(e -> b.setOpacity(0.95));
        b.setOnMouseExited (e -> b.setOpacity(1.0));
        b.setOnMousePressed(e -> b.setScaleX(0.99));
        b.setOnMouseReleased(e -> b.setScaleX(1.0));
    }
    private static void styleSuccess(Button b){ baseButton(b); b.setStyle(b.getStyle()+"-fx-background-color:#34d399; -fx-text-fill:#0b1020;"); }
    private static void styleGhost(Button b){ baseButton(b); b.setStyle(b.getStyle()+"-fx-background-color:transparent; -fx-text-fill:#60a5fa; -fx-border-color:#60a5fa; -fx-border-radius:10;"); }
}
