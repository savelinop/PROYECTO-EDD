package com.example.proyectoedd.service;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.domain.Vuelo;
import com.example.proyectoedd.grapham.GraphAL;
import com.example.proyectoedd.index.FlightIndex;
import com.example.proyectoedd.persistence.CsvDataStore;
import com.example.proyectoedd.repo.FlightGraphRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class GrafoVuelosService {

    //Colaboradores
    private final FlightGraphRepository repo = new FlightGraphRepository();
    private final FlightIndex index = new FlightIndex();
    private WeightStrategy strategy = WeightStrategy.of(MetricaPeso.TIEMPO);
    private MetricaPeso metrica = MetricaPeso.TIEMPO;

    //API pública (compatible con tu UI)
    public GraphAL<Aeropuerto, Vuelo> getGrafo() { return repo.graph(); }

    public void setMetrica(MetricaPeso m) {
        if (m == null){
            return;
        }
        this.metrica = m;
        this.strategy = WeightStrategy.of(m);

        repo.graph().getVertices().forEach(v ->
                v.getEdges().forEach(e -> e.setWeight(strategy.weightOf(e.getData())))
        );
    }
    public String unidadActual() {
        return strategy.unit();
    }

    // Aeropuertos
    public boolean agregarAeropuerto(Aeropuerto a) {
        return repo.addAirport(a);
    }
    public boolean eliminarAeropuertoPorCodigo(String codigo) {
        return repo.removeAirportByCode(codigo);
    }
    public Aeropuerto buscarAeropuerto(String codigo) {
        return repo.findAirport(codigo);
    }
    public List<Aeropuerto> listarAeropuertos() {
        return repo.listAirports();
    }

    // Vuelos
    public boolean agregarVuelo(String codOrigen, String codDestino, String aerolinea, int duracionMin) {
        Vuelo v;
        try { v = new Vuelo(aerolinea, duracionMin); } catch (IllegalArgumentException ex) { return false; }
        boolean ok = repo.addFlight(codOrigen, codDestino, v, strategy.weightOf(v));
        if (ok) {
            index.index(new FlightIndex.FlightRef(codOrigen, codDestino, v.getAerolinea(), v.getDuracionMin()));
        }
        return ok;
    }
    public boolean agregarVuelo(String codOrigen, String codDestino, String aerolinea, int duracionMin, Integer distanciaKm, Integer costoUsd) {
        Vuelo v;
        try { v = new Vuelo(aerolinea, duracionMin); } catch (IllegalArgumentException ex) { return false; }
        v.setDistanciaKm(distanciaKm);
        v.setCostoUsd(costoUsd);
        boolean ok = repo.addFlight(codOrigen, codDestino, v, strategy.weightOf(v));
        if (ok) {
            index.index(new FlightIndex.FlightRef(codOrigen, codDestino, v.getAerolinea(), v.getDuracionMin()));
        }
        return ok;
    }
    public boolean eliminarVuelo(String codOrigen, String codDestino) {
        Vuelo data = repo.getFlightData(codOrigen, codDestino);
        boolean ok = repo.removeFlight(codOrigen, codDestino);
        if (ok && data != null){
            index.remove(new FlightIndex.FlightRef(codOrigen, codDestino, data.getAerolinea(), data.getDuracionMin()));
        }
        return ok;
    }
    public boolean editarVuelo(String o, String d, String nuevaAerolinea, Integer nuevaDuracionMin) {
        Vuelo prev = repo.getFlightData(o, d);
        if (prev == null){
            return false;
        }
        if (!eliminarVuelo(o, d)){
            return false;
        }
        String aer = (nuevaAerolinea != null && !nuevaAerolinea.isBlank()) ? nuevaAerolinea.trim() : prev.getAerolinea();
        int dur    = (nuevaDuracionMin != null && nuevaDuracionMin > 0) ? nuevaDuracionMin : prev.getDuracionMin();
        return agregarVuelo(o, d, aer, dur, prev.getDistanciaKm(), prev.getCostoUsd());
    }

    // Rutas
    public GraphAL.PathResult<Aeropuerto> rutaMasCorta(String codOrigen, String codDestino) {
        Aeropuerto o = repo.findAirport(codOrigen);
        Aeropuerto d = repo.findAirport(codDestino);
        if (o == null || d == null) {
            return null;
        }
        return repo.graph().dijkstra(o, d);
    }

    // Estadisticas
    public Map<Aeropuerto, Integer> gradoSalida() {
        Map<Aeropuerto, Integer> m = new HashMap<>();
        repo.graph().getVertices().forEach(v -> m.put(v.getContent(), v.getEdges().size()));
        return m;
    }
    public Optional<Aeropuerto> masConectadoSalida() {
        return repo.graph().getVertices().stream()
                .max(Comparator.comparingInt(v -> v.getEdges().size()))
                .map(v -> v.getContent());
    }
    public Map<Aeropuerto, Integer> gradoEntrada() {
        Map<Aeropuerto, Integer> m = new HashMap<>();
        var verts = repo.graph().getVertices();
        for (var v : verts){
            m.put(v.getContent(), 0);
        }
        for (var v : verts) {
            for (var e : v.getEdges()) {
                var to = e.getTarget().getContent();
                m.put(to, m.get(to) + 1);
            }
        }
        return m;
    }
    public Optional<Aeropuerto> masConectadoEntrada() {
        var in = gradoEntrada();
        return in.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }
    public int gradoSalida(String code) {
        var a = repo.findAirport(code);
        if (a == null) {
            return 0;
        }
        return repo.graph().getVertices().stream()
                .filter(v -> v.getContent().equals(a))
                .findFirst().map(v -> v.getEdges().size()).orElse(0);
    }
    public int gradoEntrada(String code) {
        var a = repo.findAirport(code);
        if (a == null){
            return 0;
        }
        int c = 0;
        for (var v : repo.graph().getVertices()) {
            for (var e : v.getEdges()) {
                if (e.getTarget().getContent().equals(a)) {
                    c++;
                }
            }
        }
        return c;
    }

    // informacion
    public void cargarDemoPKX() {
        agregarAeropuerto(new Aeropuerto("PKX", "Daxing", "Beijing", "China"));
        agregarAeropuerto(new Aeropuerto("LHR", "Heathrow", "Londres", "UK"));
        agregarAeropuerto(new Aeropuerto("CDG", "Charles de Gaulle", "París", "Francia"));
        agregarAeropuerto(new Aeropuerto("JFK", "John F. Kennedy", "New York", "USA"));
        agregarAeropuerto(new Aeropuerto("HND", "Haneda", "Tokyo", "Japón"));
        agregarAeropuerto(new Aeropuerto("SIN", "Changi", "Singapur", "Singapur"));
        agregarAeropuerto(new Aeropuerto("FRA", "Frankfurt", "Frankfurt", "Alemania"));
        agregarAeropuerto(new Aeropuerto("DXB", "Dubai", "Dubai", "EAU"));
        agregarAeropuerto(new Aeropuerto("LAX", "Los Angeles", "Los Angeles", "USA"));
        agregarAeropuerto(new Aeropuerto("SYD", "Sydney", "Sydney", "Australia"));

        agregarVuelo("PKX","LHR","AirChina", 650, 8150, 780);
        agregarVuelo("PKX","CDG","AirChina", 630, 7990, 760);
        agregarVuelo("CDG","LHR","BA",       70,  340,   90);
        agregarVuelo("PKX","HND","ANA",     190, 2100,  250);
        agregarVuelo("HND","LHR","JAL",     720, 9590,  850);
        agregarVuelo("PKX","SIN","SQ",      340, 4450,  420);
        agregarVuelo("SIN","LHR","SQ",      780,10880,  900);
        agregarVuelo("LHR","JFK","BA",      420, 5560,  520);
        agregarVuelo("PKX","FRA","CA",      620, 7770,  740);
        agregarVuelo("PKX","DXB","CA",      420, 5840,  520);
        agregarVuelo("PKX","LAX","CA",      720,10000,  880);
        agregarVuelo("PKX","SYD","CA",      600, 8950,  840);
    }

    // Conexiones
    public int conectarAleatorio(double prob, int durMin, int durMax) {
        var rnd = new Random();
        var as = listarAeropuertos();
        if (as.size() < 2) {
            return 0;
        }

        int distMin = 200, distMax = 12000;
        int costMin = 80,  costMax = 1200;

        int added = 0;
        for (int i = 0; i < as.size(); i++) {
            for (int j = 0; j < as.size(); j++) {
                if (i == j) {
                    continue;
                }
                String o = as.get(i).getCodigo(), d = as.get(j).getCodigo();
                if (repo.existsFlight(o, d)) {
                    continue;
                }
                if (rnd.nextDouble() <= prob) {
                    String aer = new String[]{"CA","BA","SQ","JAL","ANA","AF","LH","EK","AA","DL","KLM","IB"}[rnd.nextInt(12)];
                    int dur   = rndInt(rnd, durMin, durMax);
                    int dist  = rndInt(rnd, distMin, distMax);
                    int cost  = rndInt(rnd, costMin, costMax);
                    if (agregarVuelo(o, d, aer, dur, dist, cost)) {
                        added++;
                    }
                }
            }
        }
        return added;
    }
    public int conectarCompletoAleatorio(int durMin, int durMax) {
        return conectarAleatorio(1.0, durMin, durMax);
    }
    private static int rndInt(Random r, int a, int b) {
        if (b < a) { int t = a; a = b; b = t; }
        return a + r.nextInt(Math.max(1, b - a + 1));
    }

    // Persistencia
    public void cargarDesdeCSV(File aeropuertosCsv, File vuelosCsv) throws IOException {
        var airports = CsvDataStore.readAirports(aeropuertosCsv);
        var flights  = CsvDataStore.readFlights(vuelosCsv);

        // reset
        repo.clear();
        index.clear();

        // insertar
        for (var a : airports) {
            repo.addAirport(a);
        }
        for (var fr : flights) {
            var v = fr.vuelo();
            repo.addFlight(fr.origen(), fr.destino(), v, strategy.weightOf(v));
            index.index(new FlightIndex.FlightRef(fr.origen(), fr.destino(), v.getAerolinea(), v.getDuracionMin()));
        }
    }
    public void guardarCSV(File aeropuertosCsv, File vuelosCsv) throws IOException {
        CsvDataStore.writeAirports(aeropuertosCsv, listarAeropuertos());
        CsvDataStore.writeFlights(vuelosCsv, repo);
    }
    // Poda conexiones para un grafo "realista": pocos directos, favorece hubs y escalas
    public void aplicarModoRealista() {
        // Ajusta esta lista si quieres otros hubs
        Set<String> HUBS = new HashSet<>(Arrays.asList("PKX","LHR","CDG","LAX","DXB","HND","SIN"));

        var g = repo.graph();

        // Recorremos todos los vértices (aeropuertos)
        for (var v : g.getVertices()) {
            String from = v.getContent().getCodigo();

            // Copia de las aristas salientes y ordenadas por peso (mejor primero)
            var edges = new ArrayList<>(v.getEdges());
            edges.sort(Comparator.comparingInt(e -> e.getWeight()));

            // Decidir qué destinos conservar
            Set<String> keep = new LinkedHashSet<>();

            if (HUBS.contains(from)) {
                // Si es HUB: mantener hasta 4 mejores salidas, priorizando otros hubs
                for (var e : edges) {
                    String to = e.getTarget().getContent().getCodigo();
                    if (HUBS.contains(to)){
                        keep.add(to);
                    }
                    if (keep.size() >= 4){
                        break;
                    }
                }
                // Si aún hay slots libres, completa con no-hubs cercanos
                if (keep.size() < 4) {
                    for (var e : edges) {
                        String to = e.getTarget().getContent().getCodigo();
                        if (!keep.contains(to)){
                            keep.add(to);
                        }
                        if (keep.size() >= 4){
                            break;
                        }
                    }
                }
            } else {
                // Si NO es HUB: solo 1–2 hubs más cercanos
                for (var e : edges) {
                    String to = e.getTarget().getContent().getCodigo();
                    if (HUBS.contains(to)){
                        keep.add(to);
                    }
                    if (keep.size() >= 2){
                        break;
                    }
                }
            }

            // Eliminar los vuelos que no quedaron en 'keep'
            for (var e : edges) {
                String to = e.getTarget().getContent().getCodigo();
                if (!keep.contains(to)) {
                    repo.removeFlight(from, to);
                }
            }
        }
    }

    // Quita el vuelo directo A->B (si existe)
    public boolean eliminarDirecto(String codOrigen, String codDestino) {
        return eliminarVuelo(codOrigen, codDestino);
    }



}
