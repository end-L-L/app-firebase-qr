package com.curso.asistenciaqr.fragments.maestro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.models.Sesion;
import com.curso.asistenciaqr.repository.AuthRepository;

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

        TextView         tvSaludo        = view.findViewById(R.id.tv_saludo);
        MaterialCardView cardSesionActiva = view.findViewById(R.id.card_sesion_activa);
        TextView         tvNombreSesion  = view.findViewById(R.id.tv_nombre_sesion_activa);
        TextView         tvTiempo        = view.findViewById(R.id.tv_tiempo_restante);

        String maestroId = auth.getUsuarioActual().getUid();

        // Obtener nombre desde Firestore — igual que tu versión original
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(maestroId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String nombre = doc.getString("nombre");
                    if (nombre != null) tvSaludo.setText("Hola, " + nombre);
                });

        // Buscar sesión activa del maestro
        long ahora = System.currentTimeMillis() / 1000;
        FirebaseFirestore.getInstance()
                .collection("sesiones")
                .whereEqualTo("maestroId", maestroId)
                .whereEqualTo("activa", true)
                .whereGreaterThan("expira", ahora)
                .orderBy("expira", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    if (!snapshots.isEmpty()) {
                        Sesion sesion = snapshots.getDocuments().get(0).toObject(Sesion.class);
                        sesion.setId(snapshots.getDocuments().get(0).getId());

                        // Mostrar card de sesión activa
                        cardSesionActiva.setVisibility(View.VISIBLE);
                        tvNombreSesion.setText(sesion.getMateria() + " · " + sesion.getGrupo());

                        // Tiempo restante
                        long seg = sesion.getExpira() - (System.currentTimeMillis() / 1000);
                        if (seg > 0) {
                            tvTiempo.setText(String.format("%02d:%02d", seg / 60, seg % 60));
                        } else {
                            tvTiempo.setText("00:00");
                        }

                        // Botón Ver QR
                        String sesionId = sesion.getId();
                        String token    = sesion.getToken();
                        view.findViewById(R.id.btn_ver_qr).setOnClickListener(v -> {
                            Bundle args = new Bundle();
                            args.putString("sesionId", sesionId);
                            args.putString("token", token);
                            Navigation.findNavController(v)
                                    .navigate(R.id.action_dashboard_maestro_to_qr, args);
                        });

                        // Botón Ver Lista
                        view.findViewById(R.id.btn_ver_lista).setOnClickListener(v -> {
                            Bundle args = new Bundle();
                            args.putString("sesionId", sesionId);
                            Navigation.findNavController(v)
                                    .navigate(R.id.action_dashboard_maestro_to_lista, args);
                        });
                    }
                });

        // Card Nueva Sesión — igual que tu versión original
        view.findViewById(R.id.card_nueva_sesion).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_maestro_to_crear_sesion));
    }
}