package com.curso.asistenciaqr.fragments.shared;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.PreferenciasRepository;

public class AjustesFragment extends Fragment {

    private PreferenciasRepository preferencias;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajustes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencias = new PreferenciasRepository(requireContext());
        requireActivity().setTitle("Ajustes");

        RadioButton rbDark   = view.findViewById(R.id.rb_dark);
        RadioButton rbLight  = view.findViewById(R.id.rb_light);
        RadioButton rbSystem = view.findViewById(R.id.rb_system);

        // Marcar el tema actual
        switch (preferencias.obtenerTema()) {
            case "light":  rbLight.setChecked(true);  break;
            case "system": rbSystem.setChecked(true); break;
            default:       rbDark.setChecked(true);   break;
        }

        rbDark.setOnClickListener(v   -> aplicar("dark",   rbDark, rbLight, rbSystem));
        rbLight.setOnClickListener(v  -> aplicar("light",  rbDark, rbLight, rbSystem));
        rbSystem.setOnClickListener(v -> aplicar("system", rbDark, rbLight, rbSystem));
    }

    private void aplicar(String tema,
                         RadioButton rbDark,
                         RadioButton rbLight,
                         RadioButton rbSystem) {
        // 1. Guardar preferencia
        preferencias.guardarTema(tema);

        // 2. Marcar radio button
        rbDark.setChecked("dark".equals(tema));
        rbLight.setChecked("light".equals(tema));
        rbSystem.setChecked("system".equals(tema));

        // 3. Recrear — MainActivity leerá la preferencia y aplicará el tema correcto
        androidx.fragment.app.FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.recreate();
        }
    }
}