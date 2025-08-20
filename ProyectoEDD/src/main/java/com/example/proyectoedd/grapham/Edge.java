package com.example.proyectoedd.grapham;

public class Edge<E, V> {
    private Vertex<V, E> source;
    private Vertex<V, E> target;
    private int weight;
    private E data;

    public Edge(Vertex<V, E> source, Vertex<V, E> target, int weight, E data) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.data = data;
    }

    public Vertex<V, E> getSource() {
        return source;
    }
    public Vertex<V, E> getTarget() {
        return target;
    }
    public int getWeight() {
        return weight;
    }
    public E getData() {
        return data;
    }

    public void setWeight(int weight) { this.weight = weight; }
    public void setData(E data) { this.data = data; }
}
