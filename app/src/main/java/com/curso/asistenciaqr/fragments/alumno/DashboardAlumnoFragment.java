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
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.AuthRepository;

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

        // Saludo con nombre real
        TextView tvSaludo = view.findViewById(R.id.tv_saludo_alumno);
        if (auth.getUsuarioActual() != null) {
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(auth.getUsuarioActual().getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!isAdded()) return;
                        String nombre = doc.getString("nombre");
                        if (nombre != null)
                            tvSaludo.setText("Hola, " + nombre);
                    });
        }

        // Card Escanear QR
        view.findViewById(R.id.card_escanear).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_alumno_to_scanner));
    }
}