package com.curso.asistenciaqr.fragments.alumno;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.AuthRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardAlumnoFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_alumno, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthRepository auth = new AuthRepository();
        requireActivity().setTitle("Dashboard");

        TextView tvSaludo          = view.findViewById(R.id.tv_saludo_alumno);
        TextView tvUltimaAsistencia = view.findViewById(R.id.tv_ultima_asistencia);

        String alumnoId = auth.getUsuarioActual().getUid();

        // Saludo con nombre real
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(alumnoId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String nombre = doc.getString("nombre");
                    if (nombre != null) tvSaludo.setText("Hola, " + nombre);
                });

        // Última asistencia del alumno
        FirebaseFirestore.getInstance()
                .collection("asistencias")
                .whereEqualTo("alumnoId", alumnoId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    if (!snapshots.isEmpty()) {
                        // Obtener sesionId para buscar el nombre de la materia
                        String sesionId = snapshots.getDocuments().get(0).getString("sesionId");
                        long timestamp  = snapshots.getDocuments().get(0).getLong("timestamp");

                        String hora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(new Date(timestamp * 1000));

                        // Buscar nombre de la materia en sesiones
                        FirebaseFirestore.getInstance()
                                .collection("sesiones")
                                .document(sesionId)
                                .get()
                                .addOnSuccessListener(sesionDoc -> {
                                    if (!isAdded()) return;
                                    String materia = sesionDoc.getString("materia");
                                    String grupo   = sesionDoc.getString("grupo");
                                    if (materia != null) {
                                        tvUltimaAsistencia.setText(
                                                materia + " · " + grupo + "\n" + hora);
                                    } else {
                                        tvUltimaAsistencia.setText("Registrada el " + hora);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        tvUltimaAsistencia.setText("Registrada el " + hora));
                    }
                    // Si está vacío queda el texto por defecto "Sin registros aún"
                });

        // Card Escanear QR — igual que antes
        view.findViewById(R.id.card_escanear).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_alumno_to_scanner));
    }
}