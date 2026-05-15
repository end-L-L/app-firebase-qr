package com.curso.asistenciaqr.repository;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.curso.asistenciaqr.R;

public class PreferenciasRepository {

    private static final String PREFS_NAME = "ajustes_app";
    private static final String KEY_TEMA   = "tema";
    private final Context context;
    private final SharedPreferences prefs;

    public PreferenciasRepository(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void guardarTema(String tema) {
        prefs.edit().putString(KEY_TEMA, tema).apply();
    }

    public String obtenerTema() {
        return prefs.getString(KEY_TEMA, "dark"); // oscuro por defecto
    }

    public void aplicarTema() {
        switch (obtenerTema()) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                context.getApplicationContext().setTheme(R.style.Theme_AsistenciaQR_Light);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default: // dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                context.getApplicationContext().setTheme(R.style.Theme_AsistenciaQR_Dark);
                break;
        }
    }
}