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
import com.google.android.material.card.MaterialCardView;
import com.curso.asistenciaqr.R;

public class ConfirmacionFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args   = getArguments();
        boolean exito = args != null && args.getBoolean("exito", false);
        String mensaje = args != null ? args.getString("mensaje", "") : "";

        TextView         tvTitulo   = view.findViewById(R.id.tv_titulo_resultado);
        TextView         tvMensaje  = view.findViewById(R.id.tv_mensaje_resultado);
        MaterialCardView cardResult = view.findViewById(R.id.card_resultado);

        int colorExito = requireContext().getColor(R.color.green);
        int colorError = requireContext().getColor(R.color.red);

        if (exito) {
            tvTitulo.setText("¡Asistencia registrada!");
            tvTitulo.setTextColor(colorExito);
            cardResult.setStrokeColor(colorExito);
        } else {
            tvTitulo.setText("No se pudo registrar");
            tvTitulo.setTextColor(colorError);
            cardResult.setStrokeColor(colorError);
        }

        tvMensaje.setText(mensaje);

        // Botón volver al dashboard del alumno
        view.findViewById(R.id.btn_volver).setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.dashboardAlumnoFragment));
    }
}