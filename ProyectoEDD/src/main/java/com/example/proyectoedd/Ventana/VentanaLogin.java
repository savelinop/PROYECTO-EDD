package com.example.proyectoedd.Ventana;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class VentanaLogin {

    public Parent getView() {
        StackPane bg = new StackPane();
        bg.setStyle("-fx-background-color: linear-gradient(to bottom, #0b1020, #18233a);");
        bg.setPadding(new Insets(24));

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(28));
        card.setMaxWidth(520);
        card.setStyle("""
            -fx-background-color: rgba(17,24,39,0.9);
            -fx-background-radius:16;
            -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.42), 24, 0.25, 0, 8);
            -fx-border-color: rgba(255,255,255,0.06);
            -fx-border-radius: 16;
            -fx-border-width:1;
        """);

        Label title = new Label("Iniciar sesión");
        title.setStyle("-fx-text-fill:#e5e7eb; -fx-font-size:20px; -fx-font-weight:700;");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);

        TextField txtCorreo = new TextField();
        PasswordField txtPass = new PasswordField();
        styleField(txtCorreo, "correo@ejemplo.com");
        styleField(txtPass, "Contraseña");

        form.add(coloredLabel("Correo"), 0, 0); form.add(txtCorreo, 1, 0);
        form.add(coloredLabel("Contraseña"), 0, 1); form.add(txtPass, 1, 1);

        HBox actions = new HBox(10);
        Button btnLogin = new Button("Entrar");
        Button btnVolver = new Button("Volver");
        stylePrimary(btnLogin);
        styleGhost(btnVolver);

        Label msg = new Label();
        msg.setStyle("-fx-text-fill:#ef4444; -fx-font-size:12px;");

        btnLogin.setDefaultButton(true);
        btnLogin.setOnAction(e -> {
            var reg = RegistroUsuarios.getInstance();
            reg.autenticar(txtCorreo.getText(), txtPass.getText()).ifPresentOrElse(
                usuario -> VentanaInicio.go(btnLogin, new VentanaPrincipal(usuario).getView(), "Principal"),
                () -> msg.setText("❌ Credenciales inválidas")
            );
        });
        btnVolver.setOnAction(e -> VentanaInicio.go(btnVolver, new VentanaInicio().getView(), "Inicio"));

        actions.getChildren().addAll(btnLogin, btnVolver);

        card.getChildren().addAll(title, form, msg, actions);
        bg.getChildren().add(card);
        return bg;
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
            -fx-prompt-text-fill: #6b7280;
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
    private static void stylePrimary(Button b){ baseButton(b); b.setStyle(b.getStyle()+"-fx-background-color:#06b6d4; -fx-text-fill:#0b1020;"); }
    private static void styleGhost(Button b){ baseButton(b); b.setStyle(b.getStyle()+"-fx-background-color:transparent; -fx-text-fill:#60a5fa; -fx-border-color:#60a5fa; -fx-border-radius:10;"); }
}
