package com.example.proyectoedd.repo;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.domain.Vuelo;
import com.example.proyectoedd.grapham.GraphAL;

import java.util.*;
import java.util.stream.Collectors;

/** Repositorio: encapsula el grafo y sus operaciones CRUD básicas. */
public class FlightGraphRepository {
    private final GraphAL<Aeropuerto, Vuelo> graph =
            new GraphAL<>(true, Comparator.comparing(Aeropuerto::getCodigo));

    public GraphAL<Aeropuerto, Vuelo> graph() { return graph; }

    //Aeropuertos
    public boolean addAirport(Aeropuerto a) {
        if (a == null || a.getCodigo() == null || a.getCodigo().isBlank()) return false;
        return graph.addVertex(a);
    }

    public boolean removeAirportByCode(String code) {
        Aeropuerto a = findAirport(code);
        return a != null && graph.removeVertex(a);
    }

    public Aeropuerto findAirport(String code) {
        if (code == null) return null;
        String k = code.trim();
        return graph.getVertices().stream()
                .map(v -> v.getContent())
                .filter(a -> k.equalsIgnoreCase(a.getCodigo()))
                .findFirst().orElse(null);
    }

    public List<Aeropuerto> listAirports() {
        return graph.getVertices().stream()
                .map(v -> v.getContent())
                .sorted(Comparator.comparing(Aeropuerto::getCodigo))
                .collect(Collectors.toList());
    }

    //Vuelos
    public boolean existsFlight(String oCode, String dCode) {
        return getFlightData(oCode, dCode) != null;
    }

    /** Crea arista  */
    public boolean addFlight(String oCode, String dCode, Vuelo data, int weight) {
        var o = findAirport(oCode);
        var d = findAirport(dCode);
        if (o == null || d == null || o.equals(d)){
            return false;
        }
        if (existsFlight(oCode, dCode)) {
            return false;
        }
        return graph.connect(o, d, weight, data);
    }

    public boolean removeFlight(String oCode, String dCode) {
        var o = findAirport(oCode);
        var d = findAirport(dCode);
        if (o == null || d == null) {
            return false;
        }
        return graph.disconnect(o, d);
    }

    /** Devuelve el Vuelo (data) de la arista  */
    public Vuelo getFlightData(String oCode, String dCode) {
        var o = findAirport(oCode);
        var d = findAirport(dCode);
        if (o == null || d == null){
            return null;
        }

        return graph.getVertices().stream()
                .filter(v -> v.getContent().equals(o))
                .flatMap(v -> v.getEdges().stream())
                .filter(e -> e.getTarget().getContent().equals(d))
                .map(e -> e.getData())
                .findFirst().orElse(null);
    }

    /** Limpia totalmente el grafo. */
    public void clear() {
        var copy = new ArrayList<>(graph.getVertices());
        for (var v : copy){
            graph.removeVertex(v.getContent());
        }
    }
}
