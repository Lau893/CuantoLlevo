package com.cuantollevo;

/** Una cuenta de tiempo. Todos los instantes en milisegundos desde epoch. */
public class Cuenta {
    public final String id;
    public String nombre;
    public long inicio;
    public final long creada;
    public int reinicios;
    public String color;
    /** Tiempo total que estuvo detenida desde el último inicio. */
    public long pausado;
    /** Cuándo se detuvo; 0 si está corriendo. */
    public long detenidaEn;

    public Cuenta(String id, String nombre, long inicio, long creada, int reinicios, String color,
                  long pausado, long detenidaEn) {
        this.id = id;
        this.nombre = nombre;
        this.inicio = inicio;
        this.creada = creada;
        this.reinicios = reinicios;
        this.color = color == null ? "" : color;
        this.pausado = pausado;
        this.detenidaEn = detenidaEn;
    }

    public boolean detenida() {
        return detenidaEn > 0;
    }

    /** Inicio corrido por el tiempo detenida: contar desde acá hasta {@link #fin} da lo que lleva. */
    public long desde() {
        return inicio + pausado;
    }

    /** Hasta dónde contar: ahora, o el momento en que se detuvo. */
    public long fin(long ahora) {
        return detenida() ? detenidaEn : ahora;
    }
}
