package com.curso.asistenciaqr.models;


public class Usuario {
    private String uid;
    private String nombre;
    private String email;
    private String rol; // "maestro" o "alumno"
    private String telefono;

    public Usuario() {}

    public Usuario(String uid, String nombre, String email, String rol, String telefono) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.telefono = telefono;
    }

    public String getUid()      { return uid; }
    public String getNombre()   { return nombre; }
    public String getEmail()    { return email; }
    public String getRol()      { return rol; }
    public String getTelefono() { return telefono; }

    public void setUid(String uid)          { this.uid = uid; }
    public void setNombre(String nombre)    { this.nombre = nombre; }
    public void setEmail(String email)      { this.email = email; }
    public void setRol(String rol)          { this.rol = rol; }
    public void setTelefono(String tel)     { this.telefono = tel; }
}