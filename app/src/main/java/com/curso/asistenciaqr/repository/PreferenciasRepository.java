package com.curso.asistenciaqr.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenciasRepository {

    private static final String PREFS_NAME = "ajustes_app";
    private static final String KEY_TEMA   = "tema";
    private final SharedPreferences prefs;

    public PreferenciasRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void guardarTema(String tema) {
        prefs.edit().putString(KEY_TEMA, tema).apply();
    }

    public String obtenerTema() {
        return prefs.getString(KEY_TEMA, "dark");
    }
}