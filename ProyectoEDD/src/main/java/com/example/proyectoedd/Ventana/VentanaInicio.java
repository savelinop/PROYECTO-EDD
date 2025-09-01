package com.example.proyectoedd.Ventana;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class VentanaInicio {

    public Parent getView() {
        // Lienzo fondo gradiente
        StackPane bg = new StackPane();
        bg.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #0b1020, #18233a);
        """);
        bg.setPadding(new Insets(24));

        // Tarjeta central
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(460);
        card.setStyle("""
            -fx-background-color: rgba(17,24,39,0.9);
            -fx-background-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 24, 0.25, 0, 8);
            -fx-border-color: rgba(255,255,255,0.06);
            -fx-border-radius: 16;
            -fx-border-width: 1;
        """);

        Label title = new Label("Gestión de rutas de vuelos");
        title.setStyle("-fx-text-fill:#e5e7eb; -fx-font-size:22px; -fx-font-weight:700;");

        Label subtitle = new Label("Proyecto-EDD");
        subtitle.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13px;");

        Button btnLogin = new Button("Iniciar sesión");
        Button btnRegistro = new Button("Crear cuenta");

        stylePrimary(btnLogin);            // cian
        styleSecondary(btnRegistro);       // azul

        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnRegistro.setMaxWidth(Double.MAX_VALUE);

        btnLogin.setOnAction(e -> go(btnLogin, new VentanaLogin().getView(), "Login"));
        btnRegistro.setOnAction(e -> go(btnRegistro, new VentanaRegistro().getView(), "Registro"));

        card.getChildren().addAll(title, subtitle, spacer(6), btnLogin, btnRegistro);
        VBox.setVgrow(btnLogin, Priority.NEVER);
        VBox.setVgrow(btnRegistro, Priority.NEVER);

        bg.getChildren().add(card);
        return bg;
    }

    /* ---------- helpers de estilo ---------- */
    private static Region spacer(double h) { Region r = new Region(); r.setMinHeight(h); return r; }

    private static void baseButton(Button b) {
        b.setStyle("""
            -fx-background-radius: 10;
            -fx-padding: 10 16;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-text-fill: #0b1020;
        """);
        b.setOnMouseEntered(e -> b.setOpacity(0.95));
        b.setOnMouseExited (e -> b.setOpacity(1.0));
        b.setOnMousePressed(e -> b.setScaleX(0.99));
        b.setOnMouseReleased(e -> b.setScaleX(1.0));
    }

    private static void stylePrimary(Button b) { // #06b6d4
        baseButton(b);
        b.setStyle(b.getStyle() + "-fx-background-color:#06b6d4;");
    }
    private static void styleSecondary(Button b) { // #60a5fa
        baseButton(b);
        b.setStyle(b.getStyle() + "-fx-background-color:#60a5fa;");
    }

    /** Cambia la raíz de la escena conservando Stage. */
    static void go(javafx.scene.Node anyNode, Parent nuevoRoot, String titulo) {
        Stage stage = (Stage) anyNode.getScene().getWindow();
        stage.setTitle(titulo);
        Scene scene = stage.getScene();
        scene.setRoot(nuevoRoot);
        stage.centerOnScreen();
    }
}
