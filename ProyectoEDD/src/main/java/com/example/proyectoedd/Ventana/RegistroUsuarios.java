package com.example.proyectoedd.Ventana;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RegistroUsuarios {
    private static final String ARCHIVO = "usuarios.dat";

    // Guardar usuario
    public static void guardarUsuario(Usuario usuario) {
        List<Usuario> usuarios = cargarUsuarios();
        usuarios.add(usuario);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {
            oos.writeObject(usuarios);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cargar lista de usuarios
    @SuppressWarnings("unchecked")
    public static List<Usuario> cargarUsuarios() {
        File file = new File(ARCHIVO);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO))) {
            return (List<Usuario>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
    public static boolean validarLogin(String correo, String password) {
        List<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getCorreo().equals(correo) && u.getPassword().equals(password)) {
                return true; // credenciales válidas
            }
        }
        return false; // no encontrado o credenciales incorrectas
    }
}
