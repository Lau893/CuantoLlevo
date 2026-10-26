package com.cuantollevo;

/** Una cuenta de tiempo. Todos los instantes en milisegundos desde epoch. */
public class Cuenta {
    public final String id;
    public String nombre;
    public long inicio;
    public final long creada;
    public int reinicios;
    public String color;

    public Cuenta(String id, String nombre, long inicio, long creada, int reinicios, String color) {
        this.id = id;
        this.nombre = nombre;
        this.inicio = inicio;
        this.creada = creada;
        this.reinicios = reinicios;
        this.color = color == null ? "" : color;
    }
}
