package com.curso.asistenciaqr.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenciasRepository {

    private static final String PREFS_NAME = "ajustes_app";
    private static final String KEY_TEMA   = "tema";
    private static final String KEY_ROL    = "rol"; // ← nuevo
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

    public void guardarRol(String rol) {        // ← nuevo
        prefs.edit().putString(KEY_ROL, rol).apply();
    }

    public String obtenerRol() {                // ← nuevo
        return prefs.getString(KEY_ROL, null);
    }

    public void limpiarRol() {                  // ← para el logout
        prefs.edit().remove(KEY_ROL).apply();
    }
}