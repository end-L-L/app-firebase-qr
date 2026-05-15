package com.curso.asistenciaqr.fragments.maestro;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.SesionRepository;

public class QrDisplayFragment extends Fragment {

    private CountDownTimer countDownTimer;
    private SesionRepository sesionRepository;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sesionRepository = new SesionRepository();
        requireActivity().setTitle("Código QR");

        Bundle args    = getArguments();
        String sesionId = args != null ? args.getString("sesionId") : "";
        String token    = args != null ? args.getString("token")    : "";

        ImageView imgQr        = view.findViewById(R.id.img_qr);
        TextView  tvCountdown  = view.findViewById(R.id.tv_countdown);

        // Generar bitmap del QR con ZXing
        // El contenido del QR es "sesionId|token" para que el alumno pueda parsear ambos
        String contenidoQR = sesionId + "|" + token;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(contenidoQR,
                    BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            imgQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Obtener tiempo restante desde Firestore para la cuenta regresiva
        sesionRepository.obtenerSesion(sesionId, new SesionRepository.OnSesionListener() {
            @Override
            public void onSuccess(com.curso.asistenciaqr.models.Sesion sesion) {
                if (!isAdded() || getView() == null) return;

                long ahora         = System.currentTimeMillis() / 1000;
                long segundosRestantes = sesion.getExpira() - ahora;

                if (segundosRestantes <= 0) {
                    tvCountdown.setText("00:00");
                    return;
                }

                countDownTimer = new CountDownTimer(segundosRestantes * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seg = millisUntilFinished / 1000;
                        long min = seg / 60;
                        long sec = seg % 60;
                        tvCountdown.setText(
                                String.format("%02d:%02d", min, sec));
                    }
                    @Override
                    public void onFinish() {
                        if (isAdded()) tvCountdown.setText("00:00");
                    }
                }.start();
            }
            @Override
            public void onError(String mensaje) {}
        });

        // Botón → lista de asistencia
        view.findViewById(R.id.btn_ver_asistencia).setOnClickListener(v -> {
            Bundle nextArgs = new Bundle();
            nextArgs.putString("sesionId", sesionId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_qr_to_lista_asistencia, nextArgs);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}