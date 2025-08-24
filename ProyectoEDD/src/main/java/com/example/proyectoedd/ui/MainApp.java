package com.example.proyectoedd.ui;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.domain.Vuelo;
import com.example.proyectoedd.grapham.Vertex;
import com.example.proyectoedd.service.GrafoVuelosService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainApp extends Application {

    private final GrafoVuelosService service = new GrafoVuelosService();

    private final Pane  canvas  = new Pane();
    private final Group gEdges  = new Group();
    private final Group gLabels = new Group();
    private final Group gNodes  = new Group();
    private final Group gRoot   = new Group(gEdges, gLabels, gNodes);

    private final Map<String, javafx.scene.Node> airportNodes = new HashMap<>();
    private final Map<String, EdgeView> edgeViews = new HashMap<>();

    private ToolbarView toolbar;
    private Label lblInfo;

    private double zoom = 1.0;
    private double panStartX, panStartY;
    private double panTX = 0, panTY = 0;

    private static class EdgeView {
        Line line;
        Polygon arrow;
        Text label;
        String from;
        String to;
        Color baseColor;
    }

    // Rutas por defecto para persistencia
    private static final File DEFAULT_AIRPORTS = new File("data/aeropuertos.csv");
    private static final File DEFAULT_FLIGHTS  = new File("data/vuelos.csv");

    @Override
    public void start(Stage stage) {
        // AUTOCARGA
        ensureDataDir();
        boolean loaded = tryAutoLoad();
        if (!loaded && service.listarAeropuertos().isEmpty()) {
            service.cargarDemoPKX();
            setInfoSafe("Datos de demostración cargados (no se encontraron CSV en /data).");
        }

        toolbar = new ToolbarView(service, new UiActions() {
            @Override public void onBuscarRuta()  {
                buscarRuta();
            }
            @Override public void onAgregarAeropuerto(){
                agregarAeropuertoDialog();
            }
            @Override public void onAgregarVuelo() {
                agregarVueloDialog();
            }
            @Override public void onEditarVuelo(){
                editarVueloDialog();
            }
            @Override public void onEliminarVuelo() {
                eliminarVueloDialog();
            }
            @Override public void onCargarCSV() {
                cargarCSVDialog();
            }
            @Override public void onGuardarCSV()  {
                guardarCSVDialog();
            }
            @Override public void onConectarAleatorio(){
                conectarAleatorioDialog();
            }
            @Override public void onModoRealista() {
                service.aplicarModoRealista(); drawGraph(); setInfo("Modo realista aplicado.");
            }
            @Override public void onQuitarDirecto() {
                var o = toolbar.getCbOrigen().getValue();
                var d = toolbar.getCbDestino().getValue();
                if (o == null || d == null) {
                    warn("Selecciona Origen y Destino.");
                    return;
                }
                boolean ok = service.eliminarDirecto(o.getCodigo(), d.getCodigo());
                drawGraph();
                setInfo(ok ? ("Eliminado directo " + o.getCodigo() + " -> " + d.getCodigo())
                        : ("No existía directo " + o.getCodigo() + " -> " + d.getCodigo()));
            }
            @Override public void onRedibujar()   {
                drawGraph();
            }
            @Override public void onVerEstadisticas()  {
                showEstadisticasDialog();
            }
        });

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(buildCanvas());
        root.setBottom(buildStatusBar());

        refreshCombos();
        drawGraph();

        Scene scene = new Scene(root, 1100, 750);
        stage.setTitle("Gestión de Vuelos - PKX");
        stage.setScene(scene);
        stage.show();
    }

    //AUTOGUARDADO al salir
    @Override
    public void stop() {
        try {
            ensureDataDir();
            service.guardarCSV(DEFAULT_AIRPORTS, DEFAULT_FLIGHTS);
            System.out.println("Datos guardados en: " + DEFAULT_AIRPORTS.getPath() + " y " + DEFAULT_FLIGHTS.getPath());
        } catch (Exception ex) {
            System.err.println("Error guardando al salir: " + ex.getMessage());
        }
    }

    //Helpers de persistencia
    private void ensureDataDir() {
        File dir = new File("data");
        if (!dir.exists()){
            dir.mkdirs();
        }
    }

    private boolean tryAutoLoad() {
        try {
            if (DEFAULT_AIRPORTS.exists() && DEFAULT_FLIGHTS.exists()) {
                service.cargarDesdeCSV(DEFAULT_AIRPORTS, DEFAULT_FLIGHTS);
                setInfoSafe("Datos cargados automáticamente desde /data.");
                return true;
            }
        } catch (Exception ex) {
            System.err.println("No se pudo cargar CSV inicial: " + ex.getMessage());
        }
        return false;
    }

    private Pane buildCanvas() {
        canvas.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #0b1020, #18233a);
                -fx-border-color: #111827; -fx-border-width: 1;
                """);
        canvas.getChildren().setAll(gRoot);

        canvas.addEventFilter(ScrollEvent.SCROLL, ev -> {
            double factor = (ev.getDeltaY() > 0) ? 1.1 : 0.9;
            zoom = clamp(zoom * factor, 0.4, 2.5);
            gRoot.setScaleX(zoom);
            gRoot.setScaleY(zoom);
            ev.consume();
        });

        canvas.setOnMousePressed(ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                panStartX = ev.getSceneX() - panTX;
                panStartY = ev.getSceneY() - panTY;
            }
        });
        canvas.setOnMouseDragged(ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                panTX = ev.getSceneX() - panStartX;
                panTY = ev.getSceneY() - panStartY;
                gRoot.setTranslateX(panTX);
                gRoot.setTranslateY(panTY);
            }
        });
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
        toolbar.getCbOrigen().getItems().setAll(aeropuertos);
        toolbar.getCbDestino().getItems().setAll(aeropuertos);
        aeropuertos.stream().filter(a -> "PKX".equalsIgnoreCase(a.getCodigo())).findFirst()
                .ifPresent(a -> toolbar.getCbOrigen().getSelectionModel().select(a));
        if (!aeropuertos.isEmpty()){
            toolbar.getCbDestino().getSelectionModel().select(aeropuertos.get(0));
        }
    }

    private String currentUnit() {
        String sel = toolbar.getCbMetrica().getValue();
        if (sel == null) {
            return "min";
        }
        if (sel.startsWith("Tiempo")) {
            return "min";
        }
        if (sel.startsWith("Distancia")) {
            return "km";
        }
        if (sel.startsWith("Costo")) {
            return "$";
        }
        return "min";
    }

    private void drawGraph() {
        gEdges.getChildren().clear();
        gLabels.getChildren().clear();
        gNodes.getChildren().clear();
        airportNodes.clear();
        edgeViews.clear();

        var vertices = service.getGrafo().getVertices();
        if (vertices.isEmpty()) {
            return;
        }

        double w = canvas.getWidth() > 0 ? canvas.getWidth() : 1100;
        double h = canvas.getHeight() > 0 ? canvas.getHeight() : 720;
        double cx = w / 2.0, cy = h / 2.0;
        double radius = Math.min(w, h) * 0.37;

        Vertex<Aeropuerto, Vuelo> pkx = null;
        List<Vertex<Aeropuerto, Vuelo>> others = new ArrayList<>();
        for (Vertex<Aeropuerto, Vuelo> v : vertices) {
            if ("PKX".equalsIgnoreCase(v.getContent().getCodigo())) {
                pkx = v;
            }
            else others.add(v);
        }
        if (pkx == null) {
            pkx = vertices.get(0);
            others.clear();
            for (int i = 1; i < vertices.size(); i++){
                others.add(vertices.get(i));
            }
        }

        placeAirportNode(pkx.getContent(), cx, cy, true);

        int n = others.size();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / Math.max(n, 1);
            double x = cx + radius * Math.cos(angle);
            double y = cy + radius * Math.sin(angle);
            placeAirportNode(others.get(i).getContent(), x, y, false);
        }

        int minW = Integer.MAX_VALUE, maxW = Integer.MIN_VALUE;
        for (var v : vertices) {
            for (var e : v.getEdges()) {
                minW = Math.min(minW, e.getWeight());
                maxW = Math.max(maxW, e.getWeight());
            }
        }
        if (minW == Integer.MAX_VALUE) {
            minW = 0; maxW = 1;
        }

        for (var v : vertices) {
            var fromA = v.getContent();
            var fromNode = airportNodes.get(fromA.getCodigo());
            if (fromNode == null){
                continue;
            }

            double x1 = fromNode.getLayoutX();
            double y1 = fromNode.getLayoutY();

            for (var e : v.getEdges()) {
                var toA = e.getTarget().getContent();
                var toNode = airportNodes.get(toA.getCodigo());
                if (toNode == null) {
                    continue;
                }

                double x2 = toNode.getLayoutX();
                double y2 = toNode.getLayoutY();

                int wgt = e.getWeight();
                Color c = lerpColor(Color.web("#34d399"), Color.web("#ef4444"),
                        normalize(wgt, minW, maxW));

                EdgeView ev = drawDirectedEdge(x1, y1, x2, y2, c, 1.8,
                        wgt + " " + currentUnit(),
                        fromA.getCodigo(), toA.getCodigo());

                edgeViews.put(key(fromA.getCodigo(), toA.getCodigo()), ev);
            }
        }

        service.masConectadoSalida().ifPresent(a ->
                setInfo("Más conectado (salidas): " + a.getCodigo()));
    }

    private static String key(String from, String to) {
        return from + "->" + to;
    }

    private void placeAirportNode(Aeropuerto a, double x, double y, boolean central) {
        double r = central ? 20 : 15;

        StackPane dot = new StackPane();
        Region circle = new Region();
        circle.setMinSize(r*2, r*2);
        circle.setMaxSize(r*2, r*2);
        circle.setStyle("-fx-background-radius: " + r + "px; -fx-background-color: " +
                (central ? "#06b6d4" : "#60a5fa") + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 10, 0.2, 0, 3);");

        Label code = new Label(a.getCodigo());
        code.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: " + (central ? "13" : "12") + "px;");

        dot.getChildren().addAll(circle, code);
        dot.setLayoutX(x);
        dot.setLayoutY(y);
        dot.setTranslateX(-r);
        dot.setTranslateY(-r);

        Tooltip.install(dot, new Tooltip(a.getCodigo() +
                (a.getNombre() != null && !a.getNombre().isBlank() ? " - " + a.getNombre() : "")));

        dot.setOnMouseEntered(ev -> highlightIncident(a.getCodigo(), true));
        dot.setOnMouseExited (ev -> highlightIncident(a.getCodigo(), false));
        dot.setOnMouseClicked(ev -> { if (ev.getButton() == MouseButton.PRIMARY) showAirportFlights(a); });

        makeDraggable(dot, a.getCodigo(), r);

        gNodes.getChildren().add(dot);
        airportNodes.put(a.getCodigo(), dot);
    }

    private void makeDraggable(StackPane dot, String code, double r) {
        final double[] start = new double[2];
        dot.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            start[0] = e.getSceneX(); start[1] = e.getSceneY();
        });
        dot.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            double dx = (e.getSceneX() - start[0]) / zoom;
            double dy = (e.getSceneY() - start[1]) / zoom;
            dot.setLayoutX(dot.getLayoutX() + dx);
            dot.setLayoutY(dot.getLayoutY() + dy);
            start[0] = e.getSceneX(); start[1] = e.getSceneY();
            updateEdgesFor(code, r);
        });
    }

    private void updateEdgesFor(String movedCode, double r) {
        edgeViews.entrySet().stream()
                .filter(e -> e.getKey().startsWith(movedCode + "->"))
                .forEach(e -> repositionEdge(e.getValue(), r));

        edgeViews.entrySet().stream()
                .filter(e -> e.getKey().endsWith("->" + movedCode))
                .forEach(e -> repositionEdge(e.getValue(), r));
    }

    private void repositionEdge(EdgeView ev, double r) {
        var from = airportNodes.get(ev.from);
        var to   = airportNodes.get(ev.to);
        if (from == null || to == null) {
            return;
        }

        double x1 = from.getLayoutX(), y1 = from.getLayoutY();
        double x2 = to.getLayoutX(),   y2 = to.getLayoutY();

        double[] trimmed = trimToCircle(x1, y1, x2, y2, r);
        double sx = trimmed[0], sy = trimmed[1], tx = trimmed[2], ty = trimmed[3];

        ev.line.setStartX(sx); ev.line.setStartY(sy);
        ev.line.setEndX(tx);   ev.line.setEndY(ty);
        placeArrow(ev.arrow, sx, sy, tx, ty);

        double mx = (sx + tx) / 2.0, my = (sy + ty) / 2.0;
        ev.label.setLayoutX(mx + 6);
        ev.label.setLayoutY(my - 6);
    }

    private EdgeView drawDirectedEdge(double x1, double y1, double x2, double y2,
                                      Color color, double width, String labelText,
                                      String fromCode, String toCode) {
        EdgeView ev = new EdgeView();

        double[] trimmed = trimToCircle(x1, y1, x2, y2, 17);
        double sx = trimmed[0], sy = trimmed[1], tx = trimmed[2], ty = trimmed[3];

        Line line = new Line(sx, sy, tx, ty);
        line.setStroke(color);
        line.setStrokeWidth(width);
        line.setOpacity(0.95);

        Polygon arrow = new Polygon();
        arrow.setFill(color);
        placeArrow(arrow, sx, sy, tx, ty);

        Text label = new Text(labelText);
        label.setFill(Color.web("#e5e7eb"));
        label.setStyle("-fx-font-size: 11px;");
        double mx = (sx + tx) / 2.0, my = (sy + ty) / 2.0;
        label.setLayoutX(mx + 6);
        label.setLayoutY(my - 6);

        gEdges.getChildren().addAll(line, arrow);
        gLabels.getChildren().add(label);

        ev.line = line;
        ev.arrow = arrow;
        ev.label = label;
        ev.from = fromCode;
        ev.to   = toCode;
        ev.baseColor = color;
        return ev;
    }

    private void placeArrow(Polygon arrow, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double size = 8 + 2 * zoom;
        double sin = Math.sin(angle), cos = Math.cos(angle);

        double ex = x2, ey = y2;
        double xA = ex - cos * size + -sin * size * 0.6;
        double yA = ey - sin * size +  cos * size * 0.6;
        double xB = ex - cos * size - -sin * size * 0.6;
        double yB = ey - sin * size -  cos * size * 0.6;

        arrow.getPoints().setAll(ex, ey, xA, yA, xB, yB);
    }

    private double[] trimToCircle(double x1, double y1, double x2, double y2, double r) {
        double dx = x2 - x1, dy = y2 - y1, len = Math.hypot(dx, dy);
        if (len == 0) {
            len = 1;
        }
        double ux = dx / len, uy = dy / len;
        double sx = x1 + ux * r, sy = y1 + uy * r;
        double tx = x2 - ux * r, ty = y2 - uy * r;
        return new double[]{sx, sy, tx, ty};
    }

    private void highlightIncident(String code, boolean on) {
        edgeViews.forEach((k, ev) -> {
            boolean incident = ev.from.equals(code) || ev.to.equals(code);
            if (incident) {
                ev.line.setStrokeWidth(on ? 3.6 : 1.8);
                ev.arrow.setOpacity(on ? 1.0 : 0.95);
                ev.label.setStyle(on
                        ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #ffffff;"
                        : "-fx-font-size: 11px; -fx-fill: #e5e7eb;");
            }
        });
    }

    private static final Color ROUTE_HIGHLIGHT_COLOR = Color.web("#fbbf24");
    private static final double ROUTE_HIGHLIGHT_WIDTH = 7.0;
    private static final double DIM_OPACITY = 0.22;

    private void dimAllEdges() {
        edgeViews.values().forEach(ev -> {
            ev.line.setStroke(ev.baseColor);
            ev.line.setStrokeWidth(1.8);
            ev.line.setOpacity(DIM_OPACITY);
            ev.line.setEffect(null);
            ev.arrow.setOpacity(DIM_OPACITY);
            ev.label.setOpacity(DIM_OPACITY);
            ev.label.setStyle("-fx-font-size: 11px; -fx-fill: #e5e7eb;");
        });
    }

    private void undimAllEdges() {
        edgeViews.values().forEach(ev -> {
            ev.line.setStroke(ev.baseColor);
            ev.line.setStrokeWidth(1.8);
            ev.line.setOpacity(0.95);
            ev.line.setEffect(null);
            ev.arrow.setOpacity(1.0);
            ev.label.setOpacity(1.0);
            ev.label.setStyle("-fx-font-size: 11px; -fx-fill: #e5e7eb;");
        });
    }

    private void highlightEdgeStrong(EdgeView ev) {
        if (ev == null) return;
        ev.line.setStroke(ROUTE_HIGHLIGHT_COLOR);
        ev.line.setStrokeWidth(ROUTE_HIGHLIGHT_WIDTH);
        ev.line.setOpacity(1.0);

        DropShadow glow = new DropShadow();
        glow.setColor(ROUTE_HIGHLIGHT_COLOR);
        glow.setRadius(18);
        glow.setSpread(0.35);
        ev.line.setEffect(glow);

        ev.arrow.setOpacity(1.0);
        ev.label.setOpacity(1.0);
        ev.label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: white;");
    }

    private void buscarRuta() {
        var o = toolbar.getCbOrigen().getValue();
        var d = toolbar.getCbDestino().getValue();
        if (o == null || d == null) { setInfo("Selecciona origen y destino."); return; }

        var res = service.rutaMasCorta(o.getCodigo(), d.getCodigo());
        if (res == null) {
            setInfo("No hay ruta entre " + o.getCodigo() + " y " + d.getCodigo());
            clearHighlights();
            return;
        }

        clearHighlights();
        dimAllEdges();
        for (int i = 0; i + 1 < res.path.size(); i++) {
            var a = res.path.get(i).getCodigo();
            var b = res.path.get(i+1).getCodigo();
            EdgeView ev = edgeViews.get(key(a, b));
            highlightEdgeStrong(ev);
        }
        setInfo("Ruta más corta: " +
                res.path.stream().map(Aeropuerto::getCodigo).collect(Collectors.joining(" -> ")) +
                " (total " + res.distance + " " + currentUnit() + ")");
    }

    private void clearHighlights() {
        undimAllEdges();
    }

    private Optional<Vertex<Aeropuerto, Vuelo>> findVertexByCode(String code) {
        return service.getGrafo().getVertices().stream()
                .filter(v -> v.getContent().getCodigo().equalsIgnoreCase(code))
                .findFirst();
    }

    private void agregarAeropuertoDialog() {
        Dialog<Aeropuerto> dlg = new Dialog<>();
        dlg.setTitle("Agregar aeropuerto");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfCodigo = new TextField(); tfCodigo.setPromptText("Código IATA (3 letras)");
        TextField tfNombre = new TextField(); tfNombre.setPromptText("Nombre (opcional)");
        TextField tfCiudad = new TextField(); tfCiudad.setPromptText("Ciudad (opcional)");
        TextField tfPais   = new TextField(); tfPais.setPromptText("País (opcional)");

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("Código:"), tfCodigo);
        gp.addRow(1, new Label("Nombre:"), tfNombre);
        gp.addRow(2, new Label("Ciudad:"), tfCiudad);
        gp.addRow(3, new Label("País:"), tfPais);
        dlg.getDialogPane().setContent(gp);

        dlg.setResultConverter(bt -> bt == ButtonType.OK
                ? new Aeropuerto(tfCodigo.getText().trim().toUpperCase(),
                tfNombre.getText().trim(),
                tfCiudad.getText().trim(),
                tfPais.getText().trim())
                : null);

        var res = dlg.showAndWait(); if (res.isEmpty()) return;

        Aeropuerto a = res.get();
        if (a.getCodigo() == null || a.getCodigo().length() != 3) {
            warn("El código debe tener 3 letras."); return;
        }
        if (service.buscarAeropuerto(a.getCodigo()) != null) {
            warn("Ya existe un aeropuerto con ese código."); return;
        }

        service.agregarAeropuerto(a);
        refreshCombos();
        drawGraph();
        setInfo("Aeropuerto agregado: " + a.getCodigo());
    }

    private void agregarVueloDialog() {
        var aeropuertos = service.listarAeropuertos();
        if (aeropuertos.size() < 2) {
            warn("No hay suficientes aeropuertos."); return;
        }

        ChoiceDialog<Aeropuerto> chO = new ChoiceDialog<>(toolbar.getCbOrigen().getValue(), aeropuertos);
        chO.setTitle("Agregar vuelo"); chO.setHeaderText("Seleccione ORIGEN");
        var o = chO.showAndWait(); if (o.isEmpty()) return;

        ChoiceDialog<Aeropuerto> chD = new ChoiceDialog<>(toolbar.getCbDestino().getValue(), aeropuertos);
        chD.setTitle("Agregar vuelo"); chD.setHeaderText("Seleccione DESTINO");
        var d = chD.showAndWait(); if (d.isEmpty()) return;

        Dialog<int[]> dDatos = new Dialog<>();
        dDatos.setTitle("Datos de vuelo");
        dDatos.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfAerolinea = new TextField(); tfAerolinea.setPromptText("Aerolínea");
        Spinner<Integer> spDur   = new Spinner<>(10, 2000, 60);
        Spinner<Integer> spDist  = new Spinner<>(0, 20000, 0);
        Spinner<Integer> spCosto = new Spinner<>(0, 5000, 0);

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("Aerolínea:"), tfAerolinea);
        gp.addRow(1, new Label("Duración (min):"), spDur);
        gp.addRow(2, new Label("Distancia (km):"), spDist);
        gp.addRow(3, new Label("Costo (USD):"), spCosto);
        dDatos.getDialogPane().setContent(gp);

        dDatos.setResultConverter(bt -> bt == ButtonType.OK ? new int[]{spDur.getValue(), spDist.getValue(), spCosto.getValue()} : null);

        var r = dDatos.showAndWait(); if (r.isEmpty()) return;
        String aerolinea = tfAerolinea.getText().trim();
        if (aerolinea.isBlank()) {
            warn("Aerolínea requerida."); return;
        }

        var A = o.get(); var B = d.get();
        if (A.getCodigo().equalsIgnoreCase(B.getCodigo())) {
            warn("Origen y destino no pueden ser iguales."); return;
        }

        int dur = r.get()[0], dist = r.get()[1], cost = r.get()[2];
        boolean ok = service.agregarVuelo(A.getCodigo(), B.getCodigo(), aerolinea, dur,
                dist == 0 ? null : dist, cost == 0 ? null : cost);
        if (!ok) {
            error("No se pudo agregar el vuelo.");
            return;
        }

        drawGraph();
        setInfo("Vuelo agregado: " + A.getCodigo() + " -> " + B.getCodigo());
    }

    private void editarVueloDialog() {
        var aeropuertos = service.listarAeropuertos();
        if (aeropuertos.size() < 2){
            return;
        }

        ChoiceDialog<Aeropuerto> chO = new ChoiceDialog<>(toolbar.getCbOrigen().getValue(), aeropuertos);
        chO.setTitle("Editar vuelo"); chO.setHeaderText("Seleccione ORIGEN");
        var oSel = chO.showAndWait(); if (oSel.isEmpty()) {
            return;
        }

        ChoiceDialog<Aeropuerto> chD = new ChoiceDialog<>(toolbar.getCbDestino().getValue(), aeropuertos);
        chD.setTitle("Editar vuelo"); chD.setHeaderText("Seleccione DESTINO");
        var dSel = chD.showAndWait(); if (dSel.isEmpty()){
            return;
        }

        Dialog<Object[]> dDatos = new Dialog<>();
        dDatos.setTitle("Editar Vuelo");
        dDatos.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfAerolinea = new TextField(); tfAerolinea.setPromptText("Nueva aerolínea (vacío = mantener)");
        Spinner<Integer> spDur = new Spinner<>(0, 2000, 0);

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("Aerolínea:"), tfAerolinea);
        gp.addRow(1, new Label("Duración (min):"), spDur);
        dDatos.getDialogPane().setContent(gp);

        dDatos.setResultConverter(bt -> bt == ButtonType.OK ? new Object[]{tfAerolinea.getText(), spDur.getValue()} : null);
        var r = dDatos.showAndWait(); if (r.isEmpty()) return;

        String nuevaAer = ((String) r.get()[0]).trim();
        Integer nuevaDur = ((Integer) r.get()[1]);
        if (nuevaDur != null && nuevaDur == 0) nuevaDur = null;
        if (nuevaAer.isBlank()) nuevaAer = null;

        var A = oSel.get(); var B = dSel.get();
        boolean ok = service.editarVuelo(A.getCodigo(), B.getCodigo(), nuevaAer, nuevaDur);
        if (!ok) {
            warn("No existe ese vuelo.");
            return;
        }

        drawGraph();
        setInfo("Vuelo editado: " + A.getCodigo() + " -> " + B.getCodigo());
    }

    private void eliminarVueloDialog() {
        var aeropuertos = service.listarAeropuertos();
        if (aeropuertos.size() < 2) {
            return;
        }

        ChoiceDialog<Aeropuerto> chO = new ChoiceDialog<>(toolbar.getCbOrigen().getValue(), aeropuertos);
        chO.setTitle("Eliminar vuelo"); chO.setHeaderText("Seleccione ORIGEN");
        var oSel = chO.showAndWait();
        if (oSel.isEmpty()) {
            return;
        }

        ChoiceDialog<Aeropuerto> chD = new ChoiceDialog<>(toolbar.getCbDestino().getValue(), aeropuertos);
        chD.setTitle("Eliminar vuelo"); chD.setHeaderText("Seleccione DESTINO");
        var dSel = chD.showAndWait();
        if (dSel.isEmpty()){
            return;
        }

        var A = oSel.get(); var B = dSel.get();
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar vuelo " + A.getCodigo() + " -> " + B.getCodigo() + "?", ButtonType.OK, ButtonType.CANCEL);
        conf.setHeaderText(null);
        if (conf.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean ok = service.eliminarVuelo(A.getCodigo(), B.getCodigo());
        if (!ok) {
            warn("No existe ese vuelo.");
            return;
        }

        drawGraph();
        setInfo("Vuelo eliminado: " + A.getCodigo() + " -> " + B.getCodigo());
    }

    private void cargarCSVDialog() {
        FileChooser fc1 = new FileChooser();
        fc1.setTitle("Selecciona CSV de Aeropuertos");
        fc1.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File fAirports = fc1.showOpenDialog(canvas.getScene().getWindow());
        if (fAirports == null){
            return;
        }

        FileChooser fc2 = new FileChooser();
        fc2.setTitle("Selecciona CSV de Vuelos");
        fc2.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File fFlights = fc2.showOpenDialog(canvas.getScene().getWindow());
        if (fFlights == null){
            return;
        }

        try {
            service.cargarDesdeCSV(fAirports, fFlights);
            refreshCombos();
            drawGraph();
            setInfo("CSV cargado.");
        } catch (Exception ex) {
            error("Error al cargar CSV:\n" + ex.getMessage());
        }
    }

    private void guardarCSVDialog() {
        FileChooser fc1 = new FileChooser();
        fc1.setTitle("Guardar Aeropuertos CSV");
        fc1.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File fAirports = fc1.showSaveDialog(canvas.getScene().getWindow());
        if (fAirports == null) {
            return;
        }

        FileChooser fc2 = new FileChooser();
        fc2.setTitle("Guardar Vuelos CSV");
        fc2.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File fFlights = fc2.showSaveDialog(canvas.getScene().getWindow());
        if (fFlights == null) {
            return;
        }

        try {
            service.guardarCSV(fAirports, fFlights);
            setInfo("CSV guardado.");
        } catch (Exception ex) {
            error("Error al guardar CSV:\n" + ex.getMessage());
        }
    }

    private void conectarAleatorioDialog() {
        Dialog<Object[]> dlg = new Dialog<>();
        dlg.setTitle("Conectar aleatorio");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Slider slProb = new Slider(0.0, 1.0, 0.3);
        slProb.setShowTickMarks(true);
        slProb.setShowTickLabels(true);
        slProb.setMajorTickUnit(0.25);

        Spinner<Integer> spMin = new Spinner<>(10, 2000, 60);
        Spinner<Integer> spMax = new Spinner<>(10, 3000, 900);

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("Probabilidad de crear vuelo [0..1]:"), slProb);
        gp.addRow(1, new Label("Duración mínima (min):"), spMin);
        gp.addRow(2, new Label("Duración máxima (min):"), spMax);
        dlg.getDialogPane().setContent(gp);

        dlg.setResultConverter(bt -> bt == ButtonType.OK ? new Object[]{slProb.getValue(), spMin.getValue(), spMax.getValue()} : null);

        var r = dlg.showAndWait();
        if (r.isEmpty()){
            return;
        }

        double prob = (double) r.get()[0];
        int dmin = (int) r.get()[1];
        int dmax = (int) r.get()[2];
        if (dmin > dmax) {
            int t = dmin; dmin = dmax; dmax = t;
        }

        int added = service.conectarAleatorio(prob, dmin, dmax);
        drawGraph();
        setInfo("Conexiones aleatorias generadas: " + added);
    }

    private void showAirportFlights(Aeropuerto a) {
        var vOpt = findVertexByCode(a.getCodigo());
        if (vOpt.isEmpty()) {
            return;
        }

        Vertex<Aeropuerto, Vuelo> v = vOpt.get();

        StringBuilder sb = new StringBuilder();
        sb.append("Vuelos desde ").append(a.getCodigo()).append(":\n");
        if (v.getEdges().isEmpty()) {
            sb.append("  (sin vuelos)");
        } else {
            for (var e : v.getEdges()) {
                var to = e.getTarget().getContent().getCodigo();
                sb.append("  — ").append(a.getCodigo()).append(" -> ").append(to)
                        .append(" : ").append(e.getWeight()).append(' ').append(currentUnit()).append('\n');
            }
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        info.setHeaderText(null);
        info.setTitle("Vuelos");
        info.showAndWait();
    }


    //  Estadisticas
    private void showEstadisticasDialog() {
        var salidas = service.gradoSalida();
        var entradas = service.gradoEntrada();

        record Row(String codigo, String nombre, int out, int in, int total) {}

        List<Row> filas = new ArrayList<>();
        Set<Aeropuerto> all = new HashSet<>();
        all.addAll(salidas.keySet());
        all.addAll(entradas.keySet());

        for (var a : all) {
            int out = salidas.getOrDefault(a, 0);
            int in  = entradas.getOrDefault(a, 0);
            filas.add(new Row(
                    a.getCodigo(),
                    a.getNombre() == null || a.getNombre().isBlank() ? "(sin nombre)" : a.getNombre(),
                    out, in, out + in
            ));
        }

        filas.sort(Comparator.comparingInt(Row::total).reversed());

        TableView<Row> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Row, String> c1 = new TableColumn<>("Código");
        c1.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().codigo()));
        TableColumn<Row, String> c2 = new TableColumn<>("Nombre");
        c2.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().nombre()));
        TableColumn<Row, String> c3 = new TableColumn<>("Salidas");
        c3.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().out())));
        TableColumn<Row, String> c4 = new TableColumn<>("Entradas");
        c4.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().in())));
        TableColumn<Row, String> c5 = new TableColumn<>("Total");
        c5.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().total())));

        table.getColumns().addAll(c1, c2, c3, c4, c5);
        table.getItems().addAll(filas);

        Optional<Row> max = filas.stream().max(Comparator.comparingInt(Row::total));
        Optional<Row> min = filas.stream().min(Comparator.comparingInt(Row::total));

        String resumen =
                "Más conectado (total): " + max.map(r -> r.codigo() + " (" + r.total() + ")").orElse("-") + "\n" +
                        "Menos conectado (total): " + min.map(r -> r.codigo() + " (" + r.total() + ")").orElse("-") + "\n\n" +
                        "Tip: Usa el botón 'Ruta más corta' para calcular Dijkstra entre Origen/Destino actuales.";

        TextArea ta = new TextArea(resumen);
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefRowCount(3);

        VBox box = new VBox(10, table, ta);
        box.setPadding(new Insets(10));

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Estadísticas del grafo");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setContent(box);
        dlg.showAndWait();
    }
    private void setInfo(String s) {
        lblInfo.setText(s);
    }
    private void setInfoSafe(String s) {
        System.out.println(s);
    } // antes de construir la status bar
    private void warn(String s) {
        new Alert(Alert.AlertType.WARNING, s).showAndWait();
    }
    private void error(String s) {
        new Alert(Alert.AlertType.ERROR, s).showAndWait();
    }

    private static double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }
    private static double normalize(double v, double min, double max) {
        if (max <= min) {
            return 0.0;
        } return (v - min) / (max - min);
    }
    private static Color lerpColor(Color a, Color b, double t) {
        t = clamp(t, 0, 1);
        return new Color(a.getRed() + (b.getRed()-a.getRed())*t,
                a.getGreen() + (b.getGreen()-a.getGreen())*t,
                a.getBlue() + (b.getBlue()-a.getBlue())*t, 1.0);
    }
}
