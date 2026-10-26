package com.cuantollevo;

/** Los únicos colores de la app. Cada uno tiene un tono para modo claro y otro para modo oscuro. */
public enum ColorCuenta {
    ROJO("rojo", "Rojo", 0xFFD93A3A, 0xFFFF6B6B),
    NARANJA("naranja", "Naranja", 0xFFDB7110, 0xFFFFA24D),
    AMARILLO("amarillo", "Amarillo", 0xFFB08A00, 0xFFFFD84D),
    VERDE("verde", "Verde", 0xFF2E9E4F, 0xFF5BD37E),
    TURQUESA("turquesa", "Turquesa", 0xFF0D93A0, 0xFF4FD6E0),
    AZUL("azul", "Azul", 0xFF2F6BE0, 0xFF7FA4FF),
    VIOLETA("violeta", "Violeta", 0xFF7B4AE0, 0xFFB394FF),
    ROSA("rosa", "Rosa", 0xFFD13E8A, 0xFFFF7EBF);

    public final String clave;
    public final String nombre;
    private final int claro;
    private final int oscuro;

    ColorCuenta(String clave, String nombre, int claro, int oscuro) {
        this.clave = clave;
        this.nombre = nombre;
        this.claro = claro;
        this.oscuro = oscuro;
    }

    public int valor(boolean modoOscuro) {
        return modoOscuro ? oscuro : claro;
    }

    public static ColorCuenta desde(String clave) {
        for (ColorCuenta c : values()) if (c.clave.equals(clave)) return c;
        return null;
    }

    /** El primer color que todavía no usa ninguna cuenta; si están todos usados, va rotando. */
    public static ColorCuenta sugerido(java.util.List<String> usados, int cantidad) {
        for (ColorCuenta c : values()) if (!usados.contains(c.clave)) return c;
        return values()[cantidad % values().length];
    }
}
