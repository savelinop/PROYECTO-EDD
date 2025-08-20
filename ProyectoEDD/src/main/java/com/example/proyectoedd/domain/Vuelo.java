package com.example.proyectoedd.domain;

public class Vuelo {
    private String aerolinea;
    private int duracionMin;  // este valor será el "peso"

    public Vuelo(String aerolinea, int duracionMin) {
        this.aerolinea = aerolinea;
        this.duracionMin = duracionMin;
    }

    public String getAerolinea() {
        return aerolinea;
    }
    public int getDuracionMin() {
        return duracionMin;
    }

    public void setAerolinea(String aerolinea) {
        this.aerolinea = aerolinea;
    }
    public void setDuracionMin(int duracionMin) {
        this.duracionMin = duracionMin;
    }

    @Override
    public String toString() {
        return aerolinea + " (" + duracionMin + " min)";
    }
}
