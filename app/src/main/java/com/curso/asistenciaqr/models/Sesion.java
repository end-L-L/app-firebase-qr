package com.curso.asistenciaqr.models;

public class Sesion {
    private String id;
    private String maestroId;
    private String materia;
    private String grupo;
    private String token;
    private long expira;   // timestamp Unix en segundos
    private boolean activa;

    public Sesion() {}

    public Sesion(String maestroId, String materia, String grupo,
                  String token, long expira) {
        this.maestroId = maestroId;
        this.materia   = materia;
        this.grupo     = grupo;
        this.token     = token;
        this.expira    = expira;
        this.activa    = true;
    }

    public String getId()        { return id; }
    public String getMaestroId() { return maestroId; }
    public String getMateria()   { return materia; }
    public String getGrupo()     { return grupo; }
    public String getToken()     { return token; }
    public long   getExpira()    { return expira; }
    public boolean isActiva()    { return activa; }

    public void setId(String id)          { this.id = id; }
    public void setActiva(boolean activa) { this.activa = activa; }
}