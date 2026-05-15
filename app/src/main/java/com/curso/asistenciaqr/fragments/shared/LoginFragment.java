package com.curso.asistenciaqr.fragments.shared;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.activities.MainActivity;
import com.curso.asistenciaqr.repository.AuthRepository;

public class LoginFragment extends Fragment {

    private TextInputLayout    tilEmail, tilContrasena;
    private TextInputEditText  etEmail, etContrasena;
    private MaterialButton     btnLogin, btnRolMaestro, btnRolAlumno;
    private ProgressBar        progressBar;
    private AuthRepository     authRepository;
    private String             rolSeleccionado = "alumno"; // default

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = new AuthRepository();

        // si hay sesión activa → verificar rol y redirigir
        if (authRepository.haySesionActiva()) {
            authRepository.obtenerRolActual((rol, nombre) -> {
                if (!isAdded() || getView() == null) return;
                redirigirPorRol(view, rol, nombre);

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).configurarMenuPorRol(rol);
                }

                // navegar sin popUpTo para no depender del backstack
                if ("maestro".equals(rol)) {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.dashboardMaestroFragment);
                } else {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.dashboardAlumnoFragment);
                }
            });
            return;
        }

        tilEmail       = view.findViewById(R.id.til_email);
        tilContrasena  = view.findViewById(R.id.til_contrasena);
        etEmail        = view.findViewById(R.id.et_email);
        etContrasena   = view.findViewById(R.id.et_contrasena);
        btnLogin       = view.findViewById(R.id.btn_login);
        btnRolMaestro  = view.findViewById(R.id.btn_rol_maestro);
        btnRolAlumno   = view.findViewById(R.id.btn_rol_alumno);
        progressBar    = view.findViewById(R.id.progress_bar);

        // selector de rol
        seleccionarRol("alumno", view);
        btnRolMaestro.setOnClickListener(v -> seleccionarRol("maestro", view));
        btnRolAlumno.setOnClickListener(v  -> seleccionarRol("alumno",  view));

        // Link → Registro
        view.findViewById(R.id.tv_crear_cuenta).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_login_to_registro));

        // TextWatchers para limpiar errores
        etEmail.addTextChangedListener(simpleWatcher(() -> tilEmail.setError(null)));
        etContrasena.addTextChangedListener(simpleWatcher(() -> tilContrasena.setError(null)));

        // login con tecla Done
        etContrasena.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                if (!authRepository.hayConexion(requireContext())) {
                    Snackbar.make(view, "Sin conexión a internet", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                intentarLogin(view);

            }
            return false;
        });

        btnLogin.setOnClickListener(v -> intentarLogin(view));
    }

    private void seleccionarRol(String rol, View view) {
        rolSeleccionado = rol;
        // Resaltar botón seleccionado
        int colorActivo   = requireContext().getColor(R.color.cyan);
        int colorInactivo = 0x00000000; // transparente

        if ("maestro".equals(rol)) {
            btnRolMaestro.setBackgroundColor(colorActivo);
            btnRolAlumno.setBackgroundColor(colorInactivo);
        } else {
            btnRolAlumno.setBackgroundColor(colorActivo);
            btnRolMaestro.setBackgroundColor(colorInactivo);
        }
    }

    private void intentarLogin(View view) {
        tilEmail.setError(null);
        tilContrasena.setError(null);

        String email     = etEmail.getText().toString().trim();
        String password  = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(email) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo válido");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilContrasena.setError("Mínimo 6 caracteres");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        authRepository.login(email, password, new AuthRepository.OnAuthListener() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user,
                                  String rol, String nombre) {
                if (!isAdded() || getView() == null) return;
                progressBar.setVisibility(View.GONE);
                redirigirPorRol(requireView(), rol, nombre);
            }
            @Override
            public void onError(String mensaje) {
                if (!isAdded() || getView() == null) return;
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Snackbar.make(requireView(), mensaje, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void redirigirPorRol(View view, String rol, String nombre) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).configurarMenuPorRol(rol);
        }
        if ("maestro".equals(rol)) {
            Navigation.findNavController(view)
                    .navigate(R.id.action_login_to_maestro);
        } else {
            Navigation.findNavController(view)
                    .navigate(R.id.action_login_to_alumno);
        }
    }

    // TextWatcher simplificado
    private android.text.TextWatcher simpleWatcher(Runnable accion) {
        return new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { accion.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
    }
}