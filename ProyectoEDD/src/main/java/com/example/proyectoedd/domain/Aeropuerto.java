package com.example.proyectoedd.domain;

import java.io.Serializable;
import java.util.Objects;

public final class Aeropuerto implements Comparable<Aeropuerto>, Serializable {
    private final String codigo;   // identidad inmutable
    private String nombre;
    private String ciudad;
    private String pais;

    public Aeropuerto(String codigo, String nombre, String ciudad, String pais) {
        this.codigo = validarCodigo(codigo);
        this.nombre = norm(nombre);
        this.ciudad = norm(ciudad);
        this.pais   = norm(pais);
    }

    private static String validarCodigo(String c) {
        String s = Objects.requireNonNull(c, "codigo").trim().toUpperCase();
        if (!s.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Codigo IATA invalido (debe ser 3 letras, ejemplo: PKX)");
        }
        return s;
    }

    private static String norm(String s) { return s == null ? null : s.trim(); }

    // Getters
    public String getCodigo() {
        return codigo;
    }
    public String getNombre() {
        return nombre;
    }
    public String getCiudad() {
        return ciudad;
    }
    public String getPais()   {
        return pais;
    }

    // Setters
    public void setNombre(String nombre){
        this.nombre = norm(nombre);
    }
    public void setCiudad(String ciudad){
        this.ciudad = norm(ciudad);
    }
    public void setPais(String pais){
        this.pais   = norm(pais);
    }

    // comparaciones
    @Override public int compareTo(Aeropuerto o) {
        return this.codigo.compareTo(o.codigo);
    }

    // guardado en MAYÚSCULAS
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aeropuerto a)) return false;
        return codigo.equals(a.codigo);
    }

    @Override public int hashCode() {
        return codigo.hashCode();
    }

    @Override public String toString() {
        return codigo;
    }
}
