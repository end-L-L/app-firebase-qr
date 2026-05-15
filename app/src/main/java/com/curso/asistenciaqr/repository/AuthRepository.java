package com.curso.asistenciaqr.repository;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.curso.asistenciaqr.models.Usuario;

public class AuthRepository {

    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
    }

    // ─── Estado de sesión ─────────────────────────────────────
    public boolean haySesionActiva() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getUsuarioActual() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }

    // ─── LOGIN ────────────────────────────────────────────────
    public void login(String email, String contrasena, OnAuthListener listener) {
        auth.signInWithEmailAndPassword(email, contrasena)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) { listener.onError("Error al obtener usuario"); return; }
                    // Obtener rol desde Firestore
                    db.collection("usuarios").document(user.getUid()).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) { listener.onError("Usuario sin perfil"); return; }
                                String rol = doc.getString("rol");
                                String nombre = doc.getString("nombre");
                                listener.onSuccess(user, rol, nombre);
                            })
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError(traducirError(e.getMessage())));
    }

    // ─── REGISTRO ─────────────────────────────────────────────
    public void registrar(String nombre, String email, String contrasena,
                          String rol, String telefono, OnAuthListener listener) {
        auth.createUserWithEmailAndPassword(email, contrasena)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) { listener.onError("Error al crear usuario"); return; }

                    Usuario usuario = new Usuario(
                            user.getUid(), nombre, email, rol, telefono);

                    db.collection("usuarios").document(user.getUid())
                            .set(usuario)
                            .addOnSuccessListener(v -> listener.onSuccess(user, rol, nombre))
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError(traducirError(e.getMessage())));
    }

    // ─── Obtener rol del usuario actual ───────────────────────
    public void obtenerRolActual(OnRolListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { listener.onRol(null, null); return; }

        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        listener.onRol(doc.getString("rol"), doc.getString("nombre"));
                    } else {
                        listener.onRol(null, null);
                    }
                })
                .addOnFailureListener(e -> listener.onRol(null, null));
    }

    // ─── Traducción de errores ────────────────────────────────
    private String traducirError(String error) {
        if (error == null) return "Error desconocido";
        if (error.contains("no user record"))
            return "No existe una cuenta con ese correo";
        if (error.contains("password is invalid") ||
                error.contains("INVALID_LOGIN_CREDENTIALS"))
            return "Contraseña incorrecta";
        if (error.contains("email address is already"))
            return "Ese correo ya está registrado";
        if (error.contains("badly formatted"))
            return "Formato de correo inválido";
        if (error.contains("network"))
            return "Sin conexión a internet";
        return "Error: " + error;
    }

    // ─── Interfaces ───────────────────────────────────────────
    public interface OnAuthListener {
        void onSuccess(FirebaseUser user, String rol, String nombre);
        void onError(String mensaje);
    }

    public interface OnRolListener {
        void onRol(String rol, String nombre);
    }

    public boolean hayConexion(Context context) {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;
        android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && (
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}