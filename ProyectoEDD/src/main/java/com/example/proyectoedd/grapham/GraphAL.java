package com.example.proyectoedd.grapham;

import java.util.*;

public class GraphAL<V, E> {
    private final LinkedList<Vertex<V, E>> vertices = new LinkedList<>();
    private final boolean isDirected;
    private final Comparator<V> cmp;

    public GraphAL(boolean isDirected, Comparator<V> cmp) {
        this.isDirected = isDirected;
        this.cmp = cmp;
    }

    // --------- VÉRTICES ----------
    public boolean addVertex(V content) {
        if (content == null || findVertex(content) != null) return false;
        vertices.add(new Vertex<>(content));
        return true;
    }

    /** Elimina un vértice y todas las aristas que lo referencian */
    public boolean removeVertex(V content) {
        Vertex<V,E> v = findVertex(content);
        if (v == null) return false;
        // borrar aristas entrantes
        for (Vertex<V,E> u : vertices) {
            u.getEdges().removeIf(e -> e.getTarget() == v);
        }
        return vertices.remove(v);
    }

    public Vertex<V, E> findVertex(V content) {
        for (Vertex<V,E> v : vertices) {
            if (cmp.compare(v.getContent(), content) == 0) return v;
        }
        return null;
    }

    public List<Vertex<V,E>> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    // --------- ARISTAS ----------
    public boolean connect(V content1, V content2, int weight, E data) {
        Vertex<V, E> v1 = findVertex(content1);
        Vertex<V, E> v2 = findVertex(content2);
        if (v1 == null || v2 == null) return false;

        v1.getEdges().add(new Edge<>(v1, v2, weight, data));
        if (!isDirected) {
            v2.getEdges().add(new Edge<>(v2, v1, weight, data));
        }
        return true;
    }

    /** Elimina la arista (from -> to). Si el grafo es no dirigido, también elimina la inversa */
    public boolean disconnect(V from, V to) {
        Vertex<V,E> v1 = findVertex(from);
        Vertex<V,E> v2 = findVertex(to);
        if (v1 == null || v2 == null) return false;

        boolean removed = v1.getEdges().removeIf(e -> e.getTarget() == v2);
        if (!isDirected) {
            v2.getEdges().removeIf(e -> e.getTarget() == v1);
        }
        return removed;
    }

    // --------- DIJKSTRA ----------
    public static class PathResult<V> {
        public final int distance;
        public final List<V> path;
        public PathResult(int distance, List<V> path) { this.distance = distance; this.path = path; }
        @Override public String toString() { return "dist=" + distance + ", path=" + path; }
    }

    public PathResult<V> dijkstra(V sourceContent, V targetContent) {
        Vertex<V,E> source = findVertex(sourceContent);
        Vertex<V,E> target = findVertex(targetContent);
        if (source == null || target == null) return null;

        Map<Vertex<V,E>, Integer> dist = new HashMap<>();
        Map<Vertex<V,E>, Vertex<V,E>> prev = new HashMap<>();
        for (Vertex<V,E> v : vertices) dist.put(v, Integer.MAX_VALUE);
        dist.put(source, 0);

        PriorityQueue<Vertex<V,E>> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.add(source);

        while (!pq.isEmpty()) {
            Vertex<V,E> u = pq.poll();
            if (u == target) break;

            for (Edge<E,V> e : u.getEdges()) {
                int alt = dist.get(u) + e.getWeight();
                if (alt < dist.get(e.getTarget())) {
                    dist.put(e.getTarget(), alt);
                    prev.put(e.getTarget(), u);
                    pq.remove(e.getTarget());
                    pq.add(e.getTarget());
                }
            }
        }
        if (dist.get(target) == Integer.MAX_VALUE) return null;

        LinkedList<V> path = new LinkedList<>();
        for (Vertex<V,E> cur = target; cur != null; cur = prev.get(cur)) {
            path.addFirst(cur.getContent());
        }
        return new PathResult<>(dist.get(target), path);
    }
}
