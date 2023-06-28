package com.example.tfg_covid19alert.pojo;

public class UsersPojo {

    private String dni;
    private String nombre;
    private boolean cuarent;
    private boolean contag;
    private String crono;

    public UsersPojo() { }

    //Getters y Setters
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    public boolean isContag() { return contag; }
    public void setContag(boolean contag) { this.contag = contag; }

    public boolean isCuarent() { return cuarent; }
    public void setCuarent(boolean cuarent) { this.cuarent = cuarent; }

    public String getCrono() {
        return crono;
    }
    public void setCrono(String crono) {
        this.crono = crono;
    }
}
