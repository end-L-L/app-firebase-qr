package com.curso.asistenciaqr.fragments.maestro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.models.Sesion;
import com.curso.asistenciaqr.repository.AuthRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardMaestroFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_maestro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthRepository auth = new AuthRepository();
        requireActivity().setTitle("Dashboard");

        TextView         tvSaludo         = view.findViewById(R.id.tv_saludo);
        MaterialCardView cardSesionActiva  = view.findViewById(R.id.card_sesion_activa);
        TextView         tvNombreSesion   = view.findViewById(R.id.tv_nombre_sesion_activa);
        TextView         tvTiempo         = view.findViewById(R.id.tv_tiempo_restante);
        LinearLayout     containerHistorial = view.findViewById(R.id.container_historial);
        TextView         tvSinSesiones    = view.findViewById(R.id.tv_sin_sesiones);

        String maestroId = auth.getUsuarioActual().getUid();

        // Nombre del maestro (Consulta única - está bien con .get())
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(maestroId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String nombre = doc.getString("nombre");
                    if (nombre != null) tvSaludo.setText("Hola, " + nombre);
                });

        // ── 🔄 Sesión activa vigente (EN TIEMPO REAL) ──────────────────────────────
        FirebaseFirestore.getInstance()
                .collection("sesiones")
                .whereEqualTo("maestroId", maestroId)
                .whereEqualTo("activa", true)
                .whereGreaterThan("expira", System.currentTimeMillis() / 1000)
                .orderBy("expira", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || !isAdded()) return;

                    if (snapshots != null && !snapshots.isEmpty()) {
                        Sesion sesion = snapshots.getDocuments().get(0).toObject(Sesion.class);
                        if (sesion != null) {
                            sesion.setId(snapshots.getDocuments().get(0).getId());

                            cardSesionActiva.setVisibility(View.VISIBLE);
                            tvNombreSesion.setText(sesion.getMateria() + " · " + sesion.getGrupo());

                            long seg = sesion.getExpira() - (System.currentTimeMillis() / 1000);
                            tvTiempo.setText(seg > 0 ?
                                    String.format("%02d:%02d", seg / 60, seg % 60) : "00:00");

                            String sesionId = sesion.getId();
                            String token    = sesion.getToken();

                            view.findViewById(R.id.btn_ver_qr).setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putString("sesionId", sesionId);
                                args.putString("token", token);
                                Navigation.findNavController(v)
                                        .navigate(R.id.action_dashboard_maestro_to_qr, args);
                            });

                            view.findViewById(R.id.btn_ver_lista).setOnClickListener(v -> {
                                Bundle args = new Bundle();
                                args.putString("sesionId", sesionId);
                                Navigation.findNavController(v)
                                        .navigate(R.id.action_dashboard_maestro_to_lista, args);
                            });
                        }
                    } else {
                        // ✅ SI SE ELIMINA O EXPIRA, OCULTAMOS LA CARD AUTOMÁTICAMENTE
                        cardSesionActiva.setVisibility(View.GONE);
                    }
                });

        // ── 🔄 Historial: TODAS las sesiones (EN TIEMPO REAL) ─────────
        FirebaseFirestore.getInstance()
                .collection("sesiones")
                .whereEqualTo("maestroId", maestroId)
                .orderBy("expira", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || !isAdded()) return;

                    // ⚠️ CRUCIAL: Limpiar el contenedor antes de re-dibujar,
                    // de lo contrario las sesiones se duplicarán cada vez que cambie algo.
                    containerHistorial.removeAllViews();

                    if (snapshots == null || snapshots.isEmpty()) {
                        tvSinSesiones.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvSinSesiones.setVisibility(View.GONE);
                    long ahora = System.currentTimeMillis() / 1000;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Sesion sesion = doc.toObject(Sesion.class);
                        sesion.setId(doc.getId());

                        // Inflar item del historial
                        View item = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_sesion_historial, containerHistorial, false);

                        TextView tvMateria  = item.findViewById(R.id.tv_materia_historial);
                        TextView tvGrupoFecha = item.findViewById(R.id.tv_grupo_fecha);
                        View     indicador  = item.findViewById(R.id.indicador_estado);

                        tvMateria.setText(sesion.getMateria());

                        // Fecha legible desde timestamp de expiración
                        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(new Date(sesion.getExpira() * 1000));
                        tvGrupoFecha.setText(sesion.getGrupo() + " · " + fecha);

                        // Color del indicador según si expiró o no
                        boolean expirada = sesion.getExpira() < ahora;
                        indicador.setBackgroundResource(
                                expirada ? R.drawable.circle_red : R.drawable.circle_green);

                        // Botón lista
                        String sesionId = sesion.getId();
                        item.findViewById(R.id.btn_historial_lista).setOnClickListener(v -> {
                            Bundle args = new Bundle();
                            args.putString("sesionId", sesionId);
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_dashboard_maestro_to_lista, args);
                        });

                        // Long click para eliminar
                        item.setOnLongClickListener(v -> {
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Eliminar sesión")
                                    .setMessage("¿Eliminar la sesión de " + sesion.getMateria()
                                            + " · " + sesion.getGrupo() + "?\n\n"
                                            + "También se eliminarán todos los registros de asistencia asociados.")
                                    .setPositiveButton("Eliminar", (d, w) ->
                                            eliminarSesion(sesion.getId(), sesion.getMateria()))
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                            return true;
                        });

                        containerHistorial.addView(item);
                    }
                });

        // Card Nueva Sesión
        view.findViewById(R.id.card_nueva_sesion).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_maestro_to_crear_sesion));
    }

    private void eliminarSesion(String sesionId, String materia) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Buscar y eliminar todos los documentos de asistencia de esa sesión
        db.collection("asistencias")
                .whereEqualTo("sesionId", sesionId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    // Eliminar en batch para mayor eficiencia
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        batch.delete(doc.getReference());
                    }

                    // 2. Eliminar también el documento de la sesión
                    batch.delete(db.collection("sesiones").document(sesionId));

                    // 3. Ejecutar el batch
                    batch.commit()
                            .addOnSuccessListener(v -> {
                                if (!isAdded() || getView() == null) return;
                                Snackbar.make(requireView(),
                                        "Sesión \"" + materia + "\" eliminada",
                                        Snackbar.LENGTH_SHORT).show();
                                // El onSnapshot ya actualiza la lista automáticamente
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded() || getView() == null) return;
                                Snackbar.make(requireView(),
                                        "Error al eliminar: " + e.getMessage(),
                                        Snackbar.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getView() == null) return;
                    Snackbar.make(requireView(),
                            "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }
}