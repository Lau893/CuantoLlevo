package com.cuantollevo;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Piezas de interfaz armadas en código, compartidas por todas las pantallas. */
public final class Ui {

    private Ui() {}

    public static final int MATCH = LinearLayout.LayoutParams.MATCH_PARENT;
    public static final int WRAP = LinearLayout.LayoutParams.WRAP_CONTENT;

    public static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    /** Fondo en degradé de grises y barras del sistema del mismo tono. */
    public static void prepararVentana(Activity a, Paleta p, View raiz) {
        raiz.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {p.fondoArriba, p.fondoAbajo}));
        Window w = a.getWindow();
        w.setStatusBarColor(p.fondoArriba);
        w.setNavigationBarColor(p.fondoAbajo);
        View decor = w.getDecorView();
        int flags = 0;
        if (!p.oscuro) {
            flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    public static GradientDrawable forma(Context c, int relleno, int borde, float bordeDp, float radioDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(relleno);
        if (bordeDp > 0) g.setStroke(dp(c, bordeDp), borde);
        g.setCornerRadius(dp(c, radioDp));
        return g;
    }

    public static Drawable conToque(Drawable contenido) {
        return new RippleDrawable(ColorStateList.valueOf(0x33888888), contenido, null);
    }

    public static TextView texto(Context c, String s, float sp, int color, boolean negrita) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        if (negrita) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    /** Texto con números de ancho fijo, para que el reloj no "baile". */
    public static TextView mono(Context c, String s, float sp, int color, boolean negrita) {
        TextView tv = texto(c, s, sp, color, false);
        tv.setTypeface(Typeface.create(Typeface.MONOSPACE, negrita ? Typeface.BOLD : Typeface.NORMAL));
        return tv;
    }

    /** Botón redondeado: relleno (principal) o con borde. */
    public static Button boton(Context c, Paleta p, String s, boolean principal) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setStateListAnimator(null);
        b.setMinHeight(dp(c, 52));
        b.setPadding(dp(c, 18), 0, dp(c, 18), 0);
        if (principal) {
            b.setTextColor(p.sobreTexto);
            b.setBackground(conToque(forma(c, p.texto, 0, 0, 999)));
        } else {
            b.setTextColor(p.texto);
            b.setBackground(conToque(forma(c, 0x00000000, p.linea, 1, 999)));
        }
        return b;
    }

    /** Círculo de color de una cuenta. */
    public static View punto(Context c, int color, int tamDp) {
        View v = new View(c);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        v.setBackground(g);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(c, tamDp), dp(c, tamDp)));
        return v;
    }

    public static void pintarPunto(View punto, int color) {
        ((GradientDrawable) punto.getBackground()).setColor(color);
    }

    /** Etiqueta "3 reinicios" con borde; se pinta del color de la cuenta cuando hay reinicios. */
    public static TextView chip(Context c, Paleta p, int reinicios, int color) {
        TextView tv = texto(c, "", 12, p.gris, true);
        tv.setPadding(dp(c, 9), dp(c, 4), dp(c, 9), dp(c, 4));
        pintarChip(c, p, tv, reinicios, color);
        return tv;
    }

    public static void pintarChip(Context c, Paleta p, TextView chip, int reinicios, int color) {
        int tono = reinicios > 0 ? color : p.gris;
        chip.setText(Tiempo.reinicios(reinicios));
        chip.setTextColor(tono);
        chip.setBackground(forma(c, 0x00000000, reinicios > 0 ? color : p.linea, 1, 999));
    }

    public static LinearLayout fila(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout columna(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout.LayoutParams lp(int ancho, int alto) {
        return new LinearLayout.LayoutParams(ancho, alto);
    }

    /** Ocupa el espacio que sobra en una fila. */
    public static LinearLayout.LayoutParams peso(float peso) {
        return new LinearLayout.LayoutParams(0, WRAP, peso);
    }

    public static LinearLayout.LayoutParams conMargenes(LinearLayout.LayoutParams lp, Context c,
                                                        float izq, float arriba, float der, float abajo) {
        lp.setMargins(dp(c, izq), dp(c, arriba), dp(c, der), dp(c, abajo));
        return lp;
    }

    public static View separador(Context c, Paleta p) {
        View v = new View(c);
        v.setBackgroundColor(p.linea);
        v.setLayoutParams(lp(MATCH, dp(c, 1)));
        return v;
    }
}
