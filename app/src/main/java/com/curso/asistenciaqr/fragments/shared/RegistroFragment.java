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

public class RegistroFragment extends Fragment {

    private TextInputLayout   tilNombre, tilEmail, tilContrasena, tilTelefono;
    private TextInputEditText etNombre, etEmail, etContrasena, etTelefono;
    private MaterialButton    btnRegistrar, btnRolMaestro, btnRolAlumno;
    private ProgressBar       progressBar;
    private AuthRepository    authRepository;
    private String            rolSeleccionado = "alumno";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = new AuthRepository();
        requireActivity().setTitle("Crear Cuenta");

        tilNombre     = view.findViewById(R.id.til_nombre);
        tilEmail      = view.findViewById(R.id.til_email);
        tilContrasena = view.findViewById(R.id.til_contrasena);
        tilTelefono   = view.findViewById(R.id.til_telefono);
        etNombre      = view.findViewById(R.id.et_nombre);
        etEmail       = view.findViewById(R.id.et_email);
        etContrasena  = view.findViewById(R.id.et_contrasena);
        etTelefono    = view.findViewById(R.id.et_telefono);
        btnRegistrar  = view.findViewById(R.id.btn_registrar);
        btnRolMaestro = view.findViewById(R.id.btn_rol_maestro);
        btnRolAlumno  = view.findViewById(R.id.btn_rol_alumno);
        progressBar   = view.findViewById(R.id.progress_bar);

        // Selector de rol
        seleccionarRol("alumno");
        btnRolMaestro.setOnClickListener(v -> seleccionarRol("maestro"));
        btnRolAlumno.setOnClickListener(v  -> seleccionarRol("alumno"));

        // TextWatchers
        etNombre.addTextChangedListener(simpleWatcher(() -> tilNombre.setError(null)));
        etEmail.addTextChangedListener(simpleWatcher(() -> tilEmail.setError(null)));
        etContrasena.addTextChangedListener(simpleWatcher(() -> tilContrasena.setError(null)));

        // Botón cancelar
        view.findViewById(R.id.btn_cancelar).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        // Botón crear cuenta
        btnRegistrar.setOnClickListener(v -> intentarRegistro(view));
    }

    private void seleccionarRol(String rol) {
        rolSeleccionado = rol;
        int colorActivo   = requireContext().getColor(R.color.cyan);
        int colorInactivo = 0x00000000;
        if ("maestro".equals(rol)) {
            btnRolMaestro.setBackgroundColor(colorActivo);
            btnRolAlumno.setBackgroundColor(colorInactivo);
        } else {
            btnRolAlumno.setBackgroundColor(colorActivo);
            btnRolMaestro.setBackgroundColor(colorInactivo);
        }
    }

    private void intentarRegistro(View view) {
        tilNombre.setError(null);
        tilEmail.setError(null);
        tilContrasena.setError(null);

        String nombre     = etNombre.getText().toString().trim();
        String email      = etEmail.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String telefono   = etTelefono.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre es obligatorio");
            return;
        }
        if (TextUtils.isEmpty(email) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo válido");
            return;
        }
        if (TextUtils.isEmpty(contrasena) || contrasena.length() < 6) {
            tilContrasena.setError("Mínimo 6 caracteres");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistrar.setEnabled(false);

        authRepository.registrar(nombre, email, contrasena,
                rolSeleccionado, telefono,
                new AuthRepository.OnAuthListener() {
                    @Override
                    public void onSuccess(com.google.firebase.auth.FirebaseUser user,
                                          String rol, String nombre) {
                        if (!isAdded() || getView() == null) return;
                        progressBar.setVisibility(View.GONE);

                        // Configurar menú según rol y navegar al dashboard
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).configurarMenuPorRol(rol);
                        }
                        if ("maestro".equals(rol)) {
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_login_to_maestro);
                        } else {
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_login_to_alumno);
                        }
                    }
                    @Override
                    public void onError(String mensaje) {
                        if (!isAdded() || getView() == null) return;
                        progressBar.setVisibility(View.GONE);
                        btnRegistrar.setEnabled(true);
                        Snackbar.make(requireView(), mensaje, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private android.text.TextWatcher simpleWatcher(Runnable accion) {
        return new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { accion.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
    }
}