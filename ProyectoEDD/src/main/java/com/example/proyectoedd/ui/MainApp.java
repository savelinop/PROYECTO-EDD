package com.example.proyectoedd.ui;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.domain.Vuelo;
import com.example.proyectoedd.grapham.Vertex;
import com.example.proyectoedd.service.GrafoVuelosService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class MainApp extends Application {

    private final GrafoVuelosService service = new GrafoVuelosService();
    private final Pane canvas = new Pane();
    private final Map<String, javafx.scene.Node> airportNodes = new HashMap<>();
    private final Map<String, Line> edgeLines = new HashMap<>();

    private ComboBox<Aeropuerto> cbOrigen;
    private ComboBox<Aeropuerto> cbDestino;
    private Label lblInfo;

    @Override
    public void start(Stage stage) {
        // Datos iniciales
        service.cargarDemoPKX();

        BorderPane root = new BorderPane();
        root.setTop(buildToolbar());
        root.setCenter(buildCanvas());
        root.setBottom(buildStatusBar());

        drawGraph();

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Gestión de Vuelos - PKX");
        stage.setScene(scene);
        stage.show();
    }

    private HBox buildToolbar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(10));
        bar.setAlignment(Pos.CENTER_LEFT);

        cbOrigen  = new ComboBox<>();
        cbDestino = new ComboBox<>();
        refreshCombos();

        Button btnRuta = new Button("Ruta más corta (Dijkstra)");
        btnRuta.setOnAction(e -> buscarRuta());

        Button btnAgregarAero = new Button("Agregar Aeropuerto");
        btnAgregarAero.setOnAction(e -> agregarAeropuertoDialog());

        Button btnAgregarVuelo = new Button("Agregar Vuelo");
        btnAgregarVuelo.setOnAction(e -> agregarVueloDialog());

        Button btnEliminarVuelo = new Button("Eliminar Vuelo");
        btnEliminarVuelo.setOnAction(e -> eliminarVueloDialog());

        Button btnRedibujar = new Button("Redibujar");
        btnRedibujar.setOnAction(e -> drawGraph());

        bar.getChildren().addAll(
                new Label("Origen:"), cbOrigen,
                new Label("Destino:"), cbDestino,
                btnRuta, new Separator(),
                btnAgregarAero, btnAgregarVuelo, btnEliminarVuelo,
                new Separator(), btnRedibujar
        );

        return bar;
    }

    private Pane buildCanvas() {
        canvas.setStyle("-fx-background-color: linear-gradient(to bottom, #0e1726, #1f2940);");
        return canvas;
    }

    private HBox buildStatusBar() {
        lblInfo = new Label("Listo.");
        HBox hb = new HBox(lblInfo);
        hb.setPadding(new Insets(8));
        hb.setStyle("-fx-background-color: #0d1321; -fx-text-fill: white;");
        return hb;
    }

    private void refreshCombos() {
        var aeropuertos = service.listarAeropuertos();
        cbOrigen.getItems().setAll(aeropuertos);
        cbDestino.getItems().setAll(aeropuertos);
        aeropuertos.stream().filter(a -> "PKX".equals(a.getCodigo())).findFirst()
                .ifPresent(a -> cbOrigen.getSelectionModel().select(a));
        if (!aeropuertos.isEmpty()) cbDestino.getSelectionModel().select(aeropuertos.get(0));
    }

    // ---------- Render del grafo ----------
    private void drawGraph() {
        canvas.getChildren().clear();
        airportNodes.clear();
        edgeLines.clear();

        var grafo = service.getGrafo();
        var vertices = grafo.getVertices();
        if (vertices.isEmpty()) return;

        double w = canvas.getWidth() > 0 ? canvas.getWidth() : 1000;
        double h = canvas.getHeight() > 0 ? canvas.getHeight() : 650;
        double cx = w / 2.0, cy = h / 2.0;
        double radius = Math.min(w, h) * 0.35;

        // PKX al centro
        List<Vertex<Aeropuerto, Vuelo>> others = new ArrayList<>();
        Vertex<Aeropuerto, Vuelo> pkx = null;
        for (var v : vertices) {
            if ("PKX".equals(v.getContent().getCodigo())) pkx = v;
            else others.add(v);
        }
        if (pkx == null && !vertices.isEmpty()) {
            pkx = vertices.get(0);
            others = vertices.stream().skip(1).collect(Collectors.toList());
        }

        placeAirportNode(pkx.getContent(), cx, cy, true);
        int n = others.size();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / Math.max(n, 1);
            double x = cx + radius * Math.cos(angle);
            double y = cy + radius * Math.sin(angle);
            placeAirportNode(others.get(i).getContent(), x, y, false);
        }

        // Aristas
        for (var v : vertices) {
            var fromA = v.getContent();
            var fromNode = airportNodes.get(fromA.getCodigo());
            if (fromNode == null) continue;

            double x1 = fromNode.getLayoutX();
            double y1 = fromNode.getLayoutY();

            for (var e : v.getEdges()) {
                var toA = e.getTarget().getContent();
                var toNode = airportNodes.get(toA.getCodigo());
                if (toNode == null) continue;

                double x2 = toNode.getLayoutX();
                double y2 = toNode.getLayoutY();

                Line line = new Line(x1, y1, x2, y2);
                line.setStrokeWidth(1.8);
                line.setStyle("-fx-stroke: #a1b2ff;");
                canvas.getChildren().add(0, line);

                int wgt = e.getWeight();
                if (wgt >= 0) {
                    Text t = new Text((x1 + x2) / 2, (y1 + y2) / 2, wgt + " min");
                    t.setStyle("-fx-fill: #e2e8f0; -fx-font-size: 11px;");
                    canvas.getChildren().add(t);
                }

                edgeLines.put(fromA.getCodigo() + "->" + toA.getCodigo(), line);
            }
        }

        service.masConectadoSalida().ifPresent(a ->
                lblInfo.setText("Más conectado (salidas): " + a.getCodigo())
        );
    }

    private void placeAirportNode(Aeropuerto a, double x, double y, boolean central) {
        double r = central ? 18 : 14;

        StackPane dot = new StackPane();
        Region circle = new Region();
        circle.setMinSize(r*2, r*2);
        circle.setMaxSize(r*2, r*2);
        circle.setStyle("-fx-background-radius: " + r + "px; -fx-background-color: " + (central ? "#22d3ee" : "#60a5fa") + ";"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 8, 0.2, 0, 2);");

        Label code = new Label(a.getCodigo());
        code.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: " + (central ? "12" : "11") + "px;");

        dot.getChildren().addAll(circle, code);
        dot.setLayoutX(x);
        dot.setLayoutY(y);
        dot.setTranslateX(-r);
        dot.setTranslateY(-r);

        Tooltip.install(dot, new Tooltip(
                a.getCodigo() + (a.getNombre() != null ? " - " + a.getNombre() : "")
        ));

        canvas.getChildren().add(dot);
        airportNodes.put(a.getCodigo(), dot);
    }

    // ---------- Acciones ----------
    private void buscarRuta() {
        var o = cbOrigen.getValue();
        var d = cbDestino.getValue();
        if (o == null || d == null) { lblInfo.setText("Selecciona origen y destino."); return; }

        var res = service.rutaMasCorta(o.getCodigo(), d.getCodigo());
        if (res == null) {
            lblInfo.setText("No hay ruta entre " + o.getCodigo() + " y " + d.getCodigo());
            clearHighlights();
            return;
        }

        clearHighlights();
        highlightPath(res.path);
        lblInfo.setText("Ruta más corta: " + res.path.stream().map(Aeropuerto::getCodigo).collect(Collectors.joining(" -> "))
                + " (total " + res.distance + " min)");
    }

    private void clearHighlights() {
        edgeLines.values().forEach(line -> {
            line.setStrokeWidth(1.8);
            line.setStyle("-fx-stroke: #a1b2ff;");
        });
    }

    private void highlightPath(List<Aeropuerto> path) {
        for (int i = 0; i + 1 < path.size(); i++) {
            String k = path.get(i).getCodigo() + "->" + path.get(i+1).getCodigo();
            Line line = edgeLines.get(k);
            if (line != null) {
                line.setStrokeWidth(4.0);
                line.setStyle("-fx-stroke: #fbbf24;");
            }
        }
    }

    private void agregarAeropuertoDialog() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Agregar Aeropuerto");
        d.setHeaderText("Código IATA (ej: LAX)");
        d.setContentText("Código:");
        var r = d.showAndWait();
        if (r.isEmpty()) return;

        String code = r.get().trim().toUpperCase();
        if (code.isEmpty()) return;

        Dialog<Map<String,String>> form = new Dialog<>();
        form.setTitle("Datos del Aeropuerto " + code);
        var grid = new GridPane(); grid.setHgap(8); grid.setVgap(6); grid.setPadding(new Insets(10));
        TextField tfNombre = new TextField(); TextField tfCiudad = new TextField(); TextField tfPais = new TextField();
        grid.addRow(0, new Label("Nombre:"), tfNombre);
        grid.addRow(1, new Label("Ciudad:"), tfCiudad);
        grid.addRow(2, new Label("País:"), tfPais);
        form.getDialogPane().setContent(grid);
        form.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        form.setResultConverter(b -> {
            if (b == ButtonType.OK) {
                Map<String,String> m = new HashMap<>();
                m.put("nombre", tfNombre.getText());
                m.put("ciudad", tfCiudad.getText());
                m.put("pais", tfPais.getText());
                return m;
            }
            return null;
        });
        var res = form.showAndWait();
        if (res.isEmpty()) return;

        var a = new Aeropuerto(code, res.get().get("nombre"), res.get().get("ciudad"), res.get().get("pais"));
        if (service.agregarAeropuerto(a)) {
            refreshCombos();
            drawGraph();
            lblInfo.setText("Aeropuerto " + code + " agregado.");
        } else lblInfo.setText("No se pudo agregar (repetido o error).");
    }

    private void agregarVueloDialog() {
        var aeropuertos = service.listarAeropuertos();
        if (aeropuertos.size() < 2) { lblInfo.setText("Necesitas al menos 2 aeropuertos."); return; }

        Dialog<Boolean> dlg = new Dialog<>();
        dlg.setTitle("Agregar Vuelo");
        var grid = new GridPane(); grid.setHgap(8); grid.setVgap(6); grid.setPadding(new Insets(10));

        ComboBox<Aeropuerto> cO = new ComboBox<>(); cO.getItems().setAll(aeropuertos);
        ComboBox<Aeropuerto> cD = new ComboBox<>(); cD.getItems().setAll(aeropuertos);
        TextField tfAero = new TextField();
        TextField tfDur = new TextField();

        grid.addRow(0, new Label("Origen:"), cO);
        grid.addRow(1, new Label("Destino:"), cD);
        grid.addRow(2, new Label("Aerolínea:"), tfAero);
        grid.addRow(3, new Label("Duración (min):"), tfDur);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int dur = Integer.parseInt(tfDur.getText().trim());
                    boolean ok = service.agregarVuelo(
                            cO.getValue().getCodigo(),
                            cD.getValue().getCodigo(),
                            tfAero.getText().trim(),
                            dur
                    );
                    return ok;
                } catch (Exception ex) { return false; }
            }
            return false;
        });

        var ok = dlg.showAndWait().orElse(false);
        if (ok) { drawGraph(); lblInfo.setText("Vuelo agregado."); }
        else lblInfo.setText("No se agregó el vuelo.");
    }

    private void eliminarVueloDialog() {
        var aeropuertos = service.listarAeropuertos();
        if (aeropuertos.size() < 2) { lblInfo.setText("Necesitas al menos 2 aeropuertos."); return; }

        Dialog<Boolean> dlg = new Dialog<>();
        dlg.setTitle("Eliminar Vuelo");
        var grid = new GridPane(); grid.setHgap(8); grid.setVgap(6); grid.setPadding(new Insets(10));

        ComboBox<Aeropuerto> cO = new ComboBox<>(); cO.getItems().setAll(aeropuertos);
        ComboBox<Aeropuerto> cD = new ComboBox<>(); cD.getItems().setAll(aeropuertos);

        grid.addRow(0, new Label("Origen:"), cO);
        grid.addRow(1, new Label("Destino:"), cD);

        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> btn == ButtonType.OK &&
                service.eliminarVuelo(cO.getValue().getCodigo(), cD.getValue().getCodigo()));

        var ok = dlg.showAndWait().orElse(false);
        if (ok) { drawGraph(); lblInfo.setText("Vuelo eliminado."); }
        else lblInfo.setText("No se eliminó el vuelo.");
    }

    public static void main(String[] args) { launch(args); }
}
