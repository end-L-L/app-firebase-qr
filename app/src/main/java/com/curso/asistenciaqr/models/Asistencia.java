package com.curso.asistenciaqr.models;
public class Asistencia {
    private String alumnoId;
    private String alumnoNombre;
    private String alumnoEmail;
    private String sesionId;
    private long   timestamp;

    public Asistencia() {}

    public Asistencia(String alumnoId, String alumnoNombre,
                      String alumnoEmail, String sesionId, long timestamp) {
        this.alumnoId     = alumnoId;
        this.alumnoNombre = alumnoNombre;
        this.alumnoEmail  = alumnoEmail;
        this.sesionId     = sesionId;
        this.timestamp    = timestamp;
    }

    public String getAlumnoId()     { return alumnoId; }
    public String getAlumnoNombre() { return alumnoNombre; }
    public String getAlumnoEmail()  { return alumnoEmail; }
    public String getSesionId()     { return sesionId; }
    public long   getTimestamp()    { return timestamp; }
}