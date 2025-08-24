package com.example.proyectoedd.domain;

public class Vuelo {
    private String aerolinea;
    private int duracionMin;     // minutos "peso"

    private Integer distanciaKm;
    private Integer costoUsd;

    public Vuelo(String aerolinea, int duracionMin) {
        setAerolinea(aerolinea);
        setDuracionMin(duracionMin);
    }

    public String getAerolinea(){
        return aerolinea;
    }
    public int getDuracionMin(){
        return duracionMin;
    }
    public Integer getDistanciaKm(){
        return distanciaKm;
    }
    public Integer getCostoUsd() {
        return costoUsd;
    }

    public void setAerolinea(String aerolinea) {
        if (aerolinea == null || aerolinea.isBlank())
            throw new IllegalArgumentException("Aerolinea requerida");
        this.aerolinea = aerolinea.trim();
    }

    public void setDuracionMin(int duracionMin) {
        if (duracionMin <= 0)
            throw new IllegalArgumentException("La duracion debe ser > 0");
        this.duracionMin = duracionMin;
    }

    public void setDistanciaKm(Integer distanciaKm) {
        if (distanciaKm != null && distanciaKm <= 0)
            throw new IllegalArgumentException("Distancia debe ser > 0");
        this.distanciaKm = distanciaKm;
    }

    public void setCostoUsd(Integer costoUsd) {
        if (costoUsd != null && costoUsd <= 0)
            throw new IllegalArgumentException("Costo debe ser > 0");
        this.costoUsd = costoUsd;
    }

    @Override
    public String toString() {
        return aerolinea + " (" + duracionMin + " min"
                + (distanciaKm != null ? ", " + distanciaKm + " km" : "")
                + (costoUsd != null ? ", $" + costoUsd : "")
                + ")";
    }
}
