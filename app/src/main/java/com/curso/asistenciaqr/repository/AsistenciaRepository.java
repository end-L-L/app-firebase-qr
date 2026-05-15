package com.curso.asistenciaqr.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.curso.asistenciaqr.models.Asistencia;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaRepository {

    private final FirebaseFirestore db;
    private static final String COLECCION = "asistencias";

    public AsistenciaRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ─── Registrar asistencia (ID compuesto anti-duplicado) ───
    public void registrar(String sesionId, String alumnoId,
                          String alumnoNombre, String alumnoEmail,
                          OnResultListener listener) {
        // ID compuesto: si el alumno escanea dos veces,
        // Firestore simplemente sobreescribe el mismo documento
        String docId = sesionId + "_" + alumnoId;

        long timestamp = System.currentTimeMillis() / 1000;
        Asistencia asistencia = new Asistencia(
                alumnoId, alumnoNombre, alumnoEmail, sesionId, timestamp);

        db.collection(COLECCION).document(docId)
                .set(asistencia)
                .addOnSuccessListener(v -> listener.onSuccess("Asistencia registrada"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ─── Escuchar asistencias en tiempo real ──────────────────
    public void escucharAsistencias(String sesionId,
                                    MutableLiveData<List<Asistencia>> liveData) {
        db.collection(COLECCION)
                .whereEqualTo("sesionId", sesionId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Asistencia> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Asistencia a = doc.toObject(Asistencia.class);
                        if (a != null) lista.add(a);
                    }
                    liveData.postValue(lista);
                });
    }

    // ─── Interfaz ─────────────────────────────────────────────
    public interface OnResultListener {
        void onSuccess(String mensaje);
        void onError(String mensaje);
    }
}