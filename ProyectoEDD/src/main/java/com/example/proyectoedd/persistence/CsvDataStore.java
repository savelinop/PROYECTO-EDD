package com.example.proyectoedd.persistence;

import com.example.proyectoedd.domain.Aeropuerto;
import com.example.proyectoedd.domain.Vuelo;
import com.example.proyectoedd.grapham.GraphAL;
import com.example.proyectoedd.grapham.Vertex;
import com.example.proyectoedd.repo.FlightGraphRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia simple en CSV.
 *
 * Formato aeropuertos.csv
 * codigo,nombre,ciudad,pais
 * PKX,Daxing,Beijing,China
 * LHR,Heathrow,Londres,UK
 *
 * Formato vuelos.csv (con encabezado):
 * origen,destino,aerolinea,duracionMin,distanciaKm,costoUsd
 * PKX,LHR,AirChina,650,8150,780
 * CDG,LHR,BA,70,340,90
 */
public final class CsvDataStore {

    private CsvDataStore() {}

    // Tipo auxiliar para readFlights
    public static final class FlightRow {
        private final String origen;
        private final String destino;
        private final Vuelo vuelo;
        public FlightRow(String origen, String destino, Vuelo vuelo) {
            this.origen = origen;
            this.destino = destino;
            this.vuelo = vuelo;
        }
        public String origen(){
            return origen;
        }
        public String destino(){
            return destino;
        }
        public Vuelo vuelo(){
            return vuelo;
        }
    }

    // Aeropuertos
    public static List<Aeropuerto> readAirports(File f) throws IOException {
        List<Aeropuerto> out = new ArrayList<>();
        if (f == null || !f.exists()) {
            return out;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()){
                    continue;
                }
                if (first){
                    { first = false; if (line.toLowerCase().startsWith("codigo")) continue; } // encabezado
                }
                String[] p = splitCsv(line, 4);
                String codigo = safe(p,0);
                String nombre = safe(p,1);
                String ciudad = safe(p,2);
                String pais   = safe(p,3);
                if (codigo == null || codigo.length() != 3){
                    continue;
                }
                out.add(new Aeropuerto(codigo.toUpperCase(), nombre, ciudad, pais));
            }
        }
        return out;
    }

    public static void writeAirports(File f, List<Aeropuerto> airports) throws IOException {
        if (f == null) {
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            bw.write("codigo,nombre,ciudad,pais");
            bw.newLine();
            for (Aeropuerto a : airports) {
                bw.write(csv(a.getCodigo()) + "," +
                        csv(z(a.getNombre())) + "," +
                        csv(z(a.getCiudad())) + "," +
                        csv(z(a.getPais())));
                bw.newLine();
            }
        }
    }

    // ===== Vuelos =====
    public static List<FlightRow> readFlights(File f) throws IOException {
        List<FlightRow> out = new ArrayList<>();
        if (f == null || !f.exists()){
            return out;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (first) {
                    first = false; if (line.toLowerCase().startsWith("origen")) continue;
                }
                String[] p = splitCsv(line, 6);
                String origen   = safe(p,0);
                String destino  = safe(p,1);
                String aerol    = safe(p,2);
                Integer durMin  = parseIntOrNull(safe(p,3));
                Integer distKm  = parseIntOrNull(safe(p,4));
                Integer costo   = parseIntOrNull(safe(p,5));

                if (origen == null || destino == null || aerol == null || durMin == null){
                    continue;
                }

                Vuelo v = new Vuelo(aerol.trim(), durMin);
                v.setDistanciaKm(distKm);
                v.setCostoUsd(costo);
                out.add(new FlightRow(origen.trim().toUpperCase(), destino.trim().toUpperCase(), v));
            }
        }
        return out;
    }

    public static void writeFlights(File f, FlightGraphRepository repo) throws IOException {
        if (f == null){
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            bw.write("origen,destino,aerolinea,duracionMin,distanciaKm,costoUsd");
            bw.newLine();

            GraphAL<Aeropuerto, Vuelo> g = repo.graph();
            for (Vertex<Aeropuerto, Vuelo> v : g.getVertices()) {
                String from = v.getContent().getCodigo();
                v.getEdges().forEach(e -> {
                    String to = e.getTarget().getContent().getCodigo();
                    Vuelo data = e.getData();
                    try {
                        bw.write(csv(from) + "," + csv(to) + "," + csv(z(data.getAerolinea())) + "," +
                                csv(String.valueOf(data.getDuracionMin())) + "," +
                                csv(data.getDistanciaKm() == null ? "" : String.valueOf(data.getDistanciaKm())) + "," +
                                csv(data.getCostoUsd() == null ? "" : String.valueOf(data.getCostoUsd())));
                        bw.newLine();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
            }
        } catch (UncheckedIOException wrap) {
            throw wrap.getCause();
        }
    }

    //Helpers
    private static String[] splitCsv(String line, int minParts) {
        // CSV basico
        String[] p = line.split(",", -1);
        if (p.length < minParts) {
            String[] q = new String[minParts];
            System.arraycopy(p, 0, q, 0, p.length);
            return q;
        }
        return p;
    }

    private static String safe(String[] a, int i) {
        if (a == null || i < 0 || i >= a.length){
            return null;
        }
        String s = a[i];
        if (s == null) {
            return null;
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()){
            return null;
        }
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static String csv(String s) {
        if (s == null){
            s = "";
        }
        // quoted si contiene coma
        if (s.contains(",") || s.contains("\"")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private static String z(String s) {
        return s == null ? "" : s;
    }
}
