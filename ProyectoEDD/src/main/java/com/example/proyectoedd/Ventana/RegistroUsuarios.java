package com.example.proyectoedd.Ventana;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class RegistroUsuarios {
    private static final RegistroUsuarios INSTANCE = new RegistroUsuarios();

    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    private RegistroUsuarios() {
        // Seed opcional
        usuarios.add(new Usuario("Admin Demo", "admin@demo.com", "admin"));
    }

    public static RegistroUsuarios getInstance() { return INSTANCE; }

    public ObservableList<Usuario> getUsuarios() { return usuarios; }

    public boolean existeCorreo(String correo) {
        return usuarios.stream().anyMatch(u -> u.getCorreo().equalsIgnoreCase(correo));
    }

    public boolean registrar(String nombre, String correo, String password) {
        if (existeCorreo(correo)) return false;
        usuarios.add(new Usuario(nombre, correo, password));
        return true;
    }

    public Optional<Usuario> autenticar(String correo, String password) {
        return usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo) && u.getPassword().equals(password))
                .findFirst();
    }
}
