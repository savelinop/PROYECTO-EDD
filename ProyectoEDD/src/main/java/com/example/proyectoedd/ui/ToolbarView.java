package com.example.proyectoedd.ui;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.service.GrafoVuelosService;
import com.example.proyectoedd.service.MetricaPeso;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ToolbarView extends VBox {

    private final ComboBox<Aeropuerto> cbOrigen  = new ComboBox<>();
    private final ComboBox<Aeropuerto> cbDestino = new ComboBox<>();
    private final ComboBox<String> cbMetrica     = new ComboBox<>();

    public ToolbarView(GrafoVuelosService service, UiActions actions) {
        super(6);

        // Métrica
        cbMetrica.getItems().setAll("Tiempo (min)", "Distancia (km)", "Costo ($)");
        cbMetrica.setValue("Tiempo (min)");
        service.setMetrica(MetricaPeso.TIEMPO);

        cbMetrica.valueProperty().addListener((obs, o, n) -> {
            if ("Distancia (km)".equals(n))  {
                service.setMetrica(MetricaPeso.DISTANCIA);
            }
            else if ("Costo ($)".equals(n))     {
                service.setMetrica(MetricaPeso.COSTO);
            }
            else     {
                service.setMetrica(MetricaPeso.TIEMPO);
            }
            actions.onRedibujar();
        });

        // Botones
        Button btnRuta              = new Button("Ruta más corta");
        Button btnAgregarAeropuerto = new Button("Agregar Aeropuerto");
        Button btnAgregarVuelo      = new Button("Agregar Vuelo");
        Button btnEditarVuelo       = new Button("Editar Vuelo");
        Button btnEliminarVuelo     = new Button("Eliminar Vuelo");
        Button btnCargarCSV         = new Button("Cargar CSV");
        Button btnGuardarCSV        = new Button("Guardar CSV");
        Button btnConectarAleatorio = new Button("Conectar Aleatorio");
        Button btnModoRealista      = new Button("Modo realista");
        Button btnQuitarDirecto     = new Button("Quitar directo O→D");
        Button btnRedibujar         = new Button("Redibujar");
        Button btnEstadisticas      = new Button("Estadísticas"); // NUEVO

        // Handlers
        btnRuta.setOnAction(e -> actions.onBuscarRuta());
        btnAgregarAeropuerto.setOnAction(e -> actions.onAgregarAeropuerto());
        btnAgregarVuelo.setOnAction(e -> actions.onAgregarVuelo());
        btnEditarVuelo.setOnAction(e -> actions.onEditarVuelo());
        btnEliminarVuelo.setOnAction(e -> actions.onEliminarVuelo());
        btnCargarCSV.setOnAction(e -> actions.onCargarCSV());
        btnGuardarCSV.setOnAction(e -> actions.onGuardarCSV());
        btnConectarAleatorio.setOnAction(e -> actions.onConectarAleatorio());
        btnModoRealista.setOnAction(e -> actions.onModoRealista());
        btnQuitarDirecto.setOnAction(e -> actions.onQuitarDirecto());
        btnRedibujar.setOnAction(e -> actions.onRedibujar());
        btnEstadisticas.setOnAction(e -> actions.onVerEstadisticas()); // NUEVO

        // Filas
        HBox row1 = new HBox(6,
                new Label("Métrica:"), cbMetrica,
                new Label("Origen:"),  cbOrigen,
                new Label("Destino:"), cbDestino,
                btnRuta
        );
        HBox row2 = new HBox(6,
                btnAgregarAeropuerto, btnAgregarVuelo, btnEditarVuelo, btnEliminarVuelo,
                btnCargarCSV, btnGuardarCSV, btnConectarAleatorio,
                btnModoRealista, btnQuitarDirecto, btnEstadisticas, // NUEVO
                btnRedibujar
        );

        setPadding(new Insets(8));
        getChildren().addAll(row1, row2);
    }

    public ComboBox<Aeropuerto> getCbOrigen() {
        return cbOrigen;
    }
    public ComboBox<Aeropuerto> getCbDestino() {
        return cbDestino;
    }
    public ComboBox<String>     getCbMetrica() {
        return cbMetrica;
    }
}
