package com.example.proyectoedd.service;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.domain.Vuelo;
import com.example.proyectoedd.grapham.GraphAL;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fachada de alto nivel para tu app:
 * - CRUD de aeropuertos y vuelos
 * - Dijkstra (ruta más corta por duración)
 * - Estadísticas simples
 * - Datos demo
 */
public class GrafoVuelosService {
    private final GraphAL<Aeropuerto, Vuelo> grafo;

    public GrafoVuelosService() {
        this.grafo = new GraphAL<>(true, Comparator.comparing(Aeropuerto::getCodigo));
    }

    public GraphAL<Aeropuerto, Vuelo> getGrafo() { return grafo; }

    // --------- AEROPUERTOS ----------
    public boolean agregarAeropuerto(Aeropuerto a) {
        return grafo.addVertex(a);
    }

    public boolean eliminarAeropuertoPorCodigo(String codigo) {
        Aeropuerto a = buscarAeropuerto(codigo);
        if (a == null) return false;
        return grafo.removeVertex(a);
    }

    public Aeropuerto buscarAeropuerto(String codigo) {
        if (codigo == null) return null;
        return grafo.getVertices().stream()
                .map(v -> v.getContent())
                .filter(a -> codigo.equalsIgnoreCase(a.getCodigo()))
                .findFirst()
                .orElse(null);
    }

    public List<Aeropuerto> listarAeropuertos() {
        return grafo.getVertices().stream()
                .map(v -> v.getContent())
                .sorted(Comparator.comparing(Aeropuerto::getCodigo))
                .collect(Collectors.toList());
    }

    // --------- VUELOS ----------
    public boolean agregarVuelo(String codOrigen, String codDestino, String aerolinea, int duracionMin) {
        Aeropuerto o = buscarAeropuerto(codOrigen);
        Aeropuerto d = buscarAeropuerto(codDestino);
        if (o == null || d == null || duracionMin < 0) return false;
        Vuelo v = new Vuelo(aerolinea, duracionMin);
        return grafo.connect(o, d, duracionMin, v);
    }

    public boolean eliminarVuelo(String codOrigen, String codDestino) {
        Aeropuerto o = buscarAeropuerto(codOrigen);
        Aeropuerto d = buscarAeropuerto(codDestino);
        if (o == null || d == null) return false;
        return grafo.disconnect(o, d);
    }

    // --------- RUTAS ----------
    public GraphAL.PathResult<Aeropuerto> rutaMasCorta(String codOrigen, String codDestino) {
        Aeropuerto o = buscarAeropuerto(codOrigen);
        Aeropuerto d = buscarAeropuerto(codDestino);
        if (o == null || d == null) return null;
        return grafo.dijkstra(o, d);
    }

    // --------- ESTADÍSTICAS ----------
    /** Grado de salida por aeropuerto (cantidad de vuelos salientes) */
    public Map<Aeropuerto, Integer> gradoSalida() {
        Map<Aeropuerto, Integer> m = new HashMap<>();
        grafo.getVertices().forEach(v -> m.put(v.getContent(), v.getEdges().size()));
        return m;
    }

    /** Aeropuerto con mayor grado de salida */
    public Optional<Aeropuerto> masConectadoSalida() {
        return grafo.getVertices().stream()
                .max(Comparator.comparingInt(v -> v.getEdges().size()))
                .map(v -> v.getContent());
    }

    // --------- DEMO INICIAL ----------
    public void cargarDemoPKX() {
        agregarAeropuerto(new Aeropuerto("PKX", "Daxing", "Beijing", "China"));
        agregarAeropuerto(new Aeropuerto("LHR", "Heathrow", "Londres", "UK"));
        agregarAeropuerto(new Aeropuerto("CDG", "Charles de Gaulle", "París", "Francia"));
        agregarAeropuerto(new Aeropuerto("JFK", "John F. Kennedy", "New York", "USA"));
        agregarAeropuerto(new Aeropuerto("HND", "Haneda", "Tokyo", "Japón"));
        agregarAeropuerto(new Aeropuerto("SIN", "Changi", "Singapur", "Singapur"));

        agregarVuelo("PKX","LHR","AirChina", 650);
        agregarVuelo("PKX","CDG","AirChina", 630);
        agregarVuelo("CDG","LHR","BA", 70);
        agregarVuelo("PKX","HND","ANA", 190);
        agregarVuelo("HND","LHR","JAL", 720);
        agregarVuelo("PKX","SIN","SQ", 340);
        agregarVuelo("SIN","LHR","SQ", 780);
        agregarVuelo("LHR","JFK","BA", 420);
    }
}
