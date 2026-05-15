package com.curso.asistenciaqr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.curso.asistenciaqr.R;
import com.curso.asistenciaqr.repository.PreferenciasRepository;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PreferenciasRepository(this).aplicarTema();
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btn_entrar).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}