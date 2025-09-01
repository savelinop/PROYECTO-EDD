package com.example.proyectoedd.index;

import java.util.*;

/**
 * Índice basado en árboles (TreeMap/TreeSet) para consultar vuelos
 * ordenados por aerolínea y por duración.
 */
public class FlightIndex {

    /** (ruta + datos mínimos) */
    public static class FlightRef {
        public final String origen;
        public final String destino;
        public final String aerolinea;
        public final int duracion;

        public FlightRef(String origen, String destino, String aerolinea, int duracion) {
            this.origen = Objects.requireNonNull(origen).trim().toUpperCase();
            this.destino = Objects.requireNonNull(destino).trim().toUpperCase();
            this.aerolinea = Objects.requireNonNull(aerolinea).trim();
            if (duracion <= 0) {
                throw new IllegalArgumentException("Duración debe ser > 0");
            }
            this.duracion = duracion;
        }

        @Override public String toString() {
            return origen + "->" + destino + " " + aerolinea + " (" + duracion + "m)";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FlightRef)) return false;
            FlightRef f = (FlightRef) o;
            return duracion == f.duracion &&
                    origen.equals(f.origen) &&
                    destino.equals(f.destino) &&
                    aerolinea.equals(f.aerolinea);
        }
        @Override public int hashCode() {
            return Objects.hash(origen, destino, aerolinea, duracion);
        }
    }

    // Arboles / Estructuras
    private final TreeMap<String, TreeSet<FlightRef>> byAirline = new TreeMap<>();
    private final TreeMap<Integer, TreeSet<FlightRef>> byDuration = new TreeMap<>();

    //VUELOS POR DURACION (origen -> destino -> aerolinea)
    private static final Comparator<FlightRef> BY_DURATION_THEN_ROUTE =
            Comparator.comparingInt((FlightRef f) -> f.duracion)
                    .thenComparing(f -> f.origen)
                    .thenComparing(f -> f.destino)
                    .thenComparing(f -> f.aerolinea);

    //VUELOS POR ORIGEN (DESTINO -> aerolinea -> duracion)
    private static final Comparator<FlightRef> BY_ROUTE_THEN_AIRLINE =
            Comparator.comparing((FlightRef f) -> f.origen)
                    .thenComparing(f -> f.destino)
                    .thenComparing(f -> f.aerolinea)
                    .thenComparingInt(f -> f.duracion);

    //Operaciones de indice
    public void index(FlightRef f) {
        byAirline.computeIfAbsent(f.aerolinea, k -> new TreeSet<>(BY_DURATION_THEN_ROUTE))
                .add(f);
        byDuration.computeIfAbsent(f.duracion, k -> new TreeSet<>(BY_ROUTE_THEN_AIRLINE))
                .add(f);
    }

    public void remove(FlightRef f) {
        var s1 = byAirline.get(f.aerolinea);
        if (s1 != null) {
            s1.remove(f); if (s1.isEmpty()) byAirline.remove(f.aerolinea);
        }
        var s2 = byDuration.get(f.duracion);
        if (s2 != null) {
            s2.remove(f); if (s2.isEmpty()) byDuration.remove(f.duracion);
        }
    }

    /** Limpia el indice. */
    public void clear() {
        byAirline.clear();
        byDuration.clear();
    }

    // Consultas
    public NavigableSet<FlightRef> getByAirline(String aerolinea) {
        var set = byAirline.get(aerolinea);
        if (set == null) {
            return new TreeSet<>(BY_DURATION_THEN_ROUTE);
        }
        return Collections.unmodifiableNavigableSet(set);
    }

    public List<FlightRef> getByAirlinePrefix(String prefix) {
        if (prefix == null){
            prefix = "";
        }
        String start = prefix;
        String end = prefix + "\uffff";
        var sub = byAirline.subMap(start, true, end, false);

        ArrayList<FlightRef> out = new ArrayList<>();
        for (var set : sub.values()){
            out.addAll(set);
        }
        return out;
    }

    public List<FlightRef> topKFastest(int k) {
        if (k <= 0) {
            return List.of();
        }
        ArrayList<FlightRef> out = new ArrayList<>(k);
        outer:
        for (var entry : byDuration.entrySet()) {
            for (var f : entry.getValue()) {
                out.add(f);
                if (out.size() == k){
                    break outer;
                }
            }
        }
        return out;
    }
}
