package com.curso.asistenciaqr.fragments.shared;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.PreferenciasRepository;
import java.util.HashMap;
import java.util.Map;

public class PerfilFragment extends Fragment {

    private TextInputLayout   tilNombre, tilTelefono;
    private TextInputEditText etNombre, etTelefono;
    private TextView          tvEmail, tvAvatar, tvRol;
    private MaterialButton    btnGuardar, btnEliminar;
    private ProgressBar       progressBar;

    private FirebaseAuth      auth;
    private FirebaseFirestore db;
    private FirebaseUser      usuario;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        usuario = auth.getCurrentUser();

        requireActivity().setTitle("Mi perfil");

        // Referencias
        tilNombre    = view.findViewById(R.id.til_nombre);
        tilTelefono  = view.findViewById(R.id.til_telefono);
        etNombre     = view.findViewById(R.id.et_nombre);
        etTelefono   = view.findViewById(R.id.et_telefono);
        tvEmail      = view.findViewById(R.id.tv_email);
        tvAvatar     = view.findViewById(R.id.tv_avatar);
        tvRol        = view.findViewById(R.id.tv_rol);
        btnGuardar   = view.findViewById(R.id.btn_guardar);
        btnEliminar  = view.findViewById(R.id.btn_eliminar_cuenta);
        progressBar  = view.findViewById(R.id.progress_bar);

        cargarPerfil();

        // TextWatchers
        etNombre.addTextChangedListener(simpleWatcher(() -> tilNombre.setError(null)));

        btnGuardar.setOnClickListener(v -> guardarCambios(view));
        btnEliminar.setOnClickListener(v -> confirmarEliminarCuenta(view));
    }

    // ─── Cargar datos desde Firestore ────────────────────────
    private void cargarPerfil() {
        if (usuario == null) return;

        // Email (solo lectura)
        tvEmail.setText(usuario.getEmail());

        // Avatar: primera letra del nombre
        db.collection("usuarios").document(usuario.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null) return;

                    String nombre   = doc.getString("nombre");
                    String telefono = doc.getString("telefono");
                    String rol      = doc.getString("rol");

                    if (nombre != null) {
                        etNombre.setText(nombre);
                        tvAvatar.setText(String.valueOf(nombre.charAt(0)).toUpperCase());
                    }
                    if (telefono != null) etTelefono.setText(telefono);
                    if (rol != null) {
                        tvRol.setText(rol.substring(0, 1).toUpperCase() + rol.substring(1));
                    }
                });
    }

    // ─── Guardar cambios en Firestore ────────────────────────
    private void guardarCambios(View view) {
        tilNombre.setError(null);

        String nombre   = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre no puede estar vacío");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre",   nombre);
        datos.put("telefono", telefono);

        db.collection("usuarios").document(usuario.getUid())
                .update(datos)
                .addOnSuccessListener(v -> {
                    if (!isAdded() || getView() == null) return;
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);

                    // Actualizar avatar con el nuevo nombre
                    tvAvatar.setText(String.valueOf(nombre.charAt(0)).toUpperCase());

                    Snackbar.make(requireView(), "Perfil actualizado",
                            Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getView() == null) return;
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    Snackbar.make(requireView(), "Error: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    // ─── Confirmar eliminación de cuenta ─────────────────────
    private void confirmarEliminarCuenta(View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cuenta")
                .setMessage("¿Estás seguro? Esta acción eliminará permanentemente tu perfil " +
                        "y todos tus registros. No se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (d, w) -> eliminarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ─── Eliminar cuenta ──────────────────────────────────────
    private void eliminarCuenta() {
        if (usuario == null) return;

        progressBar.setVisibility(View.VISIBLE);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(false);

        String uid = usuario.getUid();

        // 1. Borrar documento de Firestore
        db.collection("usuarios").document(uid)
                .delete()
                .addOnSuccessListener(v -> {
                    // 2. Borrar cuenta de Firebase Auth
                    usuario.delete()
                            .addOnSuccessListener(v2 -> {
                                if (!isAdded()) return;

                                // 3. Limpiar preferencias locales
                                new PreferenciasRepository(requireContext()).limpiarRol();

                                // 4. Navegar al login
                                Navigation.findNavController(requireView())
                                        .navigate(R.id.action_global_to_login);
                            })
                            .addOnFailureListener(e -> manejarErrorEliminar(e.getMessage()));
                })
                .addOnFailureListener(e -> manejarErrorEliminar(e.getMessage()));
    }

    private void manejarErrorEliminar(String mensaje) {
        if (!isAdded() || getView() == null) return;
        progressBar.setVisibility(View.GONE);
        btnEliminar.setEnabled(true);
        btnGuardar.setEnabled(true);

        // Firebase Auth requiere reautenticación reciente para eliminar cuenta
        if (mensaje != null && mensaje.contains("REQUIRES_RECENT_LOGIN")) {
            Snackbar.make(requireView(),
                    "Por seguridad, cierra sesión e inicia sesión de nuevo antes de eliminar",
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(requireView(), "Error: " + mensaje,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private android.text.TextWatcher simpleWatcher(Runnable accion) {
        return new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { accion.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
    }
}
