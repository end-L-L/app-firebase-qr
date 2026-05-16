package com.curso.asistenciaqr.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.curso.asistenciaqr.models.Sesion;
import java.util.UUID;

public class SesionRepository {

    private final FirebaseFirestore db;
    private static final String COLECCION = "sesiones";

    public SesionRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ─── Crear sesión con token UUID ──────────────────────────
    public void crearSesion(String maestroId, String materia,
                            String grupo, int duracionMinutos,
                            OnSesionListener listener) {
        String token   = UUID.randomUUID().toString();
        long   ahora   = System.currentTimeMillis() / 1000;
        long   expira  = ahora + (duracionMinutos * 60L);

        Sesion sesion = new Sesion(maestroId, materia, grupo, token, expira);

        db.collection(COLECCION)
                .add(sesion)
                .addOnSuccessListener(ref -> {
                    sesion.setId(ref.getId());
                    listener.onSuccess(sesion);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ─── Crear sesión con duración en segundos (modo demo) ────
    public void crearSesionSegundos(String maestroId, String materia,
                                    String grupo, int duracionSegundos,
                                    OnSesionListener listener) {
        String token  = UUID.randomUUID().toString();
        long   ahora  = System.currentTimeMillis() / 1000;
        long   expira = ahora + duracionSegundos;

        Sesion sesion = new Sesion(maestroId, materia, grupo, token, expira);

        db.collection(COLECCION)
                .add(sesion)
                .addOnSuccessListener(ref -> {
                    sesion.setId(ref.getId());
                    listener.onSuccess(sesion);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ─── Obtener sesión por ID ────────────────────────────────
    public void obtenerSesion(String sesionId, OnSesionListener listener) {
        db.collection(COLECCION).document(sesionId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        listener.onError("Sesión no encontrada");
                        return;
                    }
                    Sesion sesion = doc.toObject(Sesion.class);
                    if (sesion != null) sesion.setId(doc.getId());
                    listener.onSuccess(sesion);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ─── Validar QR: token vigente y sesión activa ────────────
    public void validarQR(String sesionId, String token, OnValidacionListener listener) {
        db.collection(COLECCION).document(sesionId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        listener.onInvalido("Sesión no encontrada");
                        return;
                    }
                    Sesion sesion = doc.toObject(Sesion.class);
                    if (sesion == null) {
                        listener.onInvalido("Error al leer sesión");
                        return;
                    }

                    long ahora = System.currentTimeMillis() / 1000;

                    if (!sesion.isActiva()) {
                        listener.onInvalido("La sesión ya no está activa");
                    } else if (ahora > sesion.getExpira()) {
                        listener.onInvalido("El código QR ha expirado");
                    } else if (!token.equals(sesion.getToken())) {
                        listener.onInvalido("Código QR inválido");
                    } else {
                        sesion.setId(doc.getId());
                        listener.onValido(sesion);
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ─── Interfaces ───────────────────────────────────────────
    public interface OnSesionListener {
        void onSuccess(Sesion sesion);
        void onError(String mensaje);
    }

    public interface OnValidacionListener {
        void onValido(Sesion sesion);
        void onInvalido(String razon);
        void onError(String mensaje);
    }
}