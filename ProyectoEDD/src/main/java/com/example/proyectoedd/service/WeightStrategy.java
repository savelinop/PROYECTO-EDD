package com.example.proyectoedd.service;

import com.example.proyectoedd.domain.Vuelo;

/** Estrategia de peso (Patrón Strategy) para tiempo/distancia/costo. */
public interface WeightStrategy {
    int weightOf(Vuelo v);
    String unit();

    static WeightStrategy of(MetricaPeso m) {
        return switch (m) {
            case TIEMPO    -> new TimeWeight();
            case DISTANCIA -> new DistanceWeight();
            case COSTO     -> new CostWeight();
        };
    }

    /** Peso por minutos. */
    class TimeWeight implements WeightStrategy {
        @Override public int weightOf(Vuelo v) { return v.getDuracionMin(); }
        @Override public String unit() {
            return "min";
        }
    }

    /** Peso por kilómetros (fallback a minutos si falta distancia). */
    class DistanceWeight implements WeightStrategy {
        @Override public int weightOf(Vuelo v) {
            return v.getDistanciaKm() != null ? v.getDistanciaKm() : v.getDuracionMin();
        }
        @Override public String unit() { return "km"; }
    }

    /** Peso por USD (fallback a minutos si falta costo). */
    class CostWeight implements WeightStrategy {
        @Override public int weightOf(Vuelo v) {
            return v.getCostoUsd() != null ? v.getCostoUsd() : v.getDuracionMin();
        }
        @Override public String unit() { return "$"; }
    }
}
