package com.curso.asistenciaqr.fragments.alumno;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.AsistenciaRepository;
import com.curso.asistenciaqr.repository.SesionRepository;

public class ScannerQrFragment extends Fragment {

    private AsistenciaRepository asistenciaRepository;
    private SesionRepository     sesionRepository;

    // Launcher para el escáner de ZXing
    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    // Usuario canceló el escaneo
                    Navigation.findNavController(requireView()).popBackStack();
                    return;
                }
                procesarQR(result.getContents());
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        asistenciaRepository = new AsistenciaRepository();
        sesionRepository     = new SesionRepository();
        requireActivity().setTitle("Escanear QR");

        // Abrir escáner automáticamente al llegar al fragment
        abrirEscaner();
    }

    private void abrirEscaner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Apunta al código QR del maestro");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        scanLauncher.launch(options);
    }

    private void procesarQR(String contenido) {
        // Formato esperado: "sesionId|token"
        String[] partes = contenido.split("\\|");
        if (partes.length != 2) {
            navegarConfirmacion(false, "Código QR no válido");
            return;
        }

        String sesionId = partes[0];
        String token    = partes[1];

        sesionRepository.validarQR(sesionId, token,
                new SesionRepository.OnValidacionListener() {
                    @Override
                    public void onValido(com.curso.asistenciaqr.models.Sesion sesion) {
                        if (!isAdded() || getView() == null) return;
                        registrarAsistencia(sesionId);
                    }
                    @Override
                    public void onInvalido(String razon) {
                        if (!isAdded() || getView() == null) return;
                        navegarConfirmacion(false, razon);
                    }
                    @Override
                    public void onError(String mensaje) {
                        if (!isAdded() || getView() == null) return;
                        navegarConfirmacion(false, "Error: " + mensaje);
                    }
                });
    }

    private void registrarAsistencia(String sesionId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navegarConfirmacion(false, "No hay sesión de usuario");
            return;
        }

        // Obtener nombre y email del alumno desde Firestore
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || getView() == null) return;

                    String nombre = doc.getString("nombre") != null ?
                            doc.getString("nombre") : "Alumno";
                    String email  = user.getEmail() != null ?
                            user.getEmail() : "";

                    asistenciaRepository.registrar(sesionId, user.getUid(),
                            nombre, email,
                            new AsistenciaRepository.OnResultListener() {
                                @Override
                                public void onSuccess(String mensaje) {
                                    if (!isAdded() || getView() == null) return;
                                    navegarConfirmacion(true,
                                            "Tu asistencia fue registrada exitosamente.");
                                }
                                @Override
                                public void onError(String mensaje) {
                                    if (!isAdded() || getView() == null) return;
                                    navegarConfirmacion(false, "Error: " + mensaje);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getView() == null) return;
                    navegarConfirmacion(false, "Error al obtener datos del alumno");
                });
    }

    private void navegarConfirmacion(boolean exito, String mensaje) {
        Bundle args = new Bundle();
        args.putBoolean("exito",   exito);
        args.putString("mensaje",  mensaje);
        Navigation.findNavController(requireView())
                .navigate(R.id.confirmacionFragment, args);
    }
}