package com.example.proyectoedd.grapham;

import java.util.LinkedList;

public class Vertex<V, E> {
    private V content;
    private LinkedList<Edge<E, V>> edges;

    public Vertex(V content) {
        this.content = content;
        this.edges = new LinkedList<>();
    }

    public V getContent() {
        return content;
    }
    public LinkedList<Edge<E, V>> getEdges() {
        return edges;
    }
}
