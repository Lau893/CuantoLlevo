package com.cuantollevo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

/** Blanco, negro y grises de la interfaz, según el modo claro u oscuro del teléfono. */
public final class Paleta {

    public final boolean oscuro;
    public final int fondoArriba, fondoAbajo, superficie, texto, gris, linea, celda, sobreTexto;

    /** Modo elegido por el usuario: seguir al teléfono, siempre claro o siempre oscuro. */
    public static final String SISTEMA = "sistema", CLARO = "claro", OSCURO = "oscuro";

    private static final String PREFS = "cuanto_llevo";
    private static final String KEY_MODO = "modo";

    public static String modo(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MODO, SISTEMA);
    }

    public static void guardarModo(Context context, String modo) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_MODO, modo).apply();
    }

    private static boolean esOscuro(Context context) {
        String modo = modo(context);
        if (CLARO.equals(modo)) return false;
        if (OSCURO.equals(modo)) return true;
        int sistema = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return sistema == Configuration.UI_MODE_NIGHT_YES;
    }

    /** Llamar antes de super.onCreate, para que los diálogos del sistema usen el mismo modo. */
    public static void aplicarTema(Activity a) {
        a.setTheme(esOscuro(a) ? R.style.Theme_CuantoLlevo_Oscuro : R.style.Theme_CuantoLlevo_Claro);
    }

    public static Paleta de(Context context) {
        return new Paleta(esOscuro(context));
    }

    private Paleta(boolean oscuro) {
        this.oscuro = oscuro;
        if (oscuro) {
            fondoArriba = 0xFF1A1A1A;
            fondoAbajo = 0xFF0A0A0A;
            superficie = 0xFF171717;
            texto = 0xFFF2F2F2;
            gris = 0xFF9A9A9A;
            linea = 0xFF2E2E2E;
            celda = 0xFF222222;
            sobreTexto = 0xFF0C0C0C;
        } else {
            fondoArriba = 0xFFFFFFFF;
            fondoAbajo = 0xFFEDEDED;
            superficie = 0xFFFFFFFF;
            texto = 0xFF111111;
            gris = 0xFF666666;
            linea = 0xFFD6D6D6;
            celda = 0xFFEDEDED;
            sobreTexto = 0xFFFFFFFF;
        }
    }

    /** Color de una cuenta; sin color elegido usa el negro/blanco del texto. */
    public int de(String clave) {
        ColorCuenta c = ColorCuenta.desde(clave);
        return c == null ? texto : c.valor(oscuro);
    }
}
