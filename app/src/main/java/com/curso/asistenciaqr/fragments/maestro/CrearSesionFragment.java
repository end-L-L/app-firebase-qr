package com.curso.asistenciaqr.fragments.maestro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.SesionRepository;

public class CrearSesionFragment extends Fragment {

    private TextInputLayout   tilMateria, tilGrupo, tilSegundos;
    private TextInputEditText etMateria, etGrupo, etSegundos;
    private TextView          tvDuracion;
    private Slider            sliderDuracion;
    private MaterialButton    btnGenerar;
    private MaterialSwitch    switchDemo;
    private ProgressBar       progressBar;
    private SesionRepository  sesionRepository;
    private int               duracionMinutos = 15;
    private boolean           modoDemo = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_sesion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sesionRepository = new SesionRepository();
        requireActivity().setTitle("Nueva Sesión");

        // Referencias existentes — sin cambios
        tilMateria     = view.findViewById(R.id.til_materia);
        tilGrupo       = view.findViewById(R.id.til_grupo);
        etMateria      = view.findViewById(R.id.et_materia);
        etGrupo        = view.findViewById(R.id.et_grupo);
        tvDuracion     = view.findViewById(R.id.tv_duracion_valor);
        sliderDuracion = view.findViewById(R.id.slider_duracion);
        btnGenerar     = view.findViewById(R.id.btn_generar_qr);
        progressBar    = view.findViewById(R.id.progress_bar);

        // Referencias nuevas
        switchDemo  = view.findViewById(R.id.switch_modo_demo);
        tilSegundos = view.findViewById(R.id.til_segundos);
        etSegundos  = view.findViewById(R.id.et_segundos);

        // Slider igual que antes
        sliderDuracion.addOnChangeListener((slider, value, fromUser) -> {
            duracionMinutos = (int) value;
            tvDuracion.setText(duracionMinutos + " minutos");
        });

        // Toggle modo demo
        switchDemo.setOnCheckedChangeListener((btn, isChecked) -> {
            modoDemo = isChecked;
            if (isChecked) {
                // Modo demo: ocultar slider, mostrar campo segundos
                sliderDuracion.setVisibility(View.GONE);
                tvDuracion.setVisibility(View.GONE);
                tilSegundos.setVisibility(View.VISIBLE);
            } else {
                // Modo estándar: mostrar slider, ocultar campo segundos
                sliderDuracion.setVisibility(View.VISIBLE);
                tvDuracion.setVisibility(View.VISIBLE);
                tilSegundos.setVisibility(View.GONE);
            }
        });

        btnGenerar.setOnClickListener(v -> generarSesion(view));
    }

    private void generarSesion(View view) {
        tilMateria.setError(null);
        tilGrupo.setError(null);

        String materia = etMateria.getText().toString().trim();
        String grupo   = etGrupo.getText().toString().trim();

        if (TextUtils.isEmpty(materia)) {
            tilMateria.setError("Ingresa el nombre de la materia");
            return;
        }
        if (TextUtils.isEmpty(grupo)) {
            tilGrupo.setError("Ingresa el grupo");
            return;
        }

        // ── Calcular duración según el modo ───────────────────
        int duracionSegundos;
        if (modoDemo) {
            String seg = etSegundos.getText().toString().trim();
            if (TextUtils.isEmpty(seg)) {
                tilSegundos.setError("Ingresa la duración en segundos");
                return;
            }
            int segundos = Integer.parseInt(seg);
            if (segundos < 10) {
                tilSegundos.setError("Mínimo 10 segundos");
                return;
            }
            duracionSegundos = segundos;
        } else {
            duracionSegundos = duracionMinutos * 60;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGenerar.setEnabled(false);

        String maestroId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Pasar segundos directamente al repository
        sesionRepository.crearSesionSegundos(maestroId, materia, grupo, duracionSegundos,
                new SesionRepository.OnSesionListener() {
                    @Override
                    public void onSuccess(com.curso.asistenciaqr.models.Sesion sesion) {
                        if (!isAdded() || getView() == null) return;
                        progressBar.setVisibility(View.GONE);
                        Bundle args = new Bundle();
                        args.putString("sesionId", sesion.getId());
                        args.putString("token", sesion.getToken());
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_crear_sesion_to_qr, args);
                    }
                    @Override
                    public void onError(String mensaje) {
                        if (!isAdded() || getView() == null) return;
                        progressBar.setVisibility(View.GONE);
                        btnGenerar.setEnabled(true);
                        Snackbar.make(requireView(), "Error: " + mensaje,
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}