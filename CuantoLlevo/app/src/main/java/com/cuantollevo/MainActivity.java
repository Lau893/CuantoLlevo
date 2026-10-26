package com.cuantollevo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Pantalla principal: la lista de cuentas con su contador en vivo. */
public class MainActivity extends Activity {

    private Paleta p;
    private CuentasStore store;
    private LinearLayout lista;

    /** Lo que hay que actualizar cada segundo. */
    private static class Fila {
        final Cuenta cuenta;
        final TextView titular;
        final TextView subtitulo;

        Fila(Cuenta cuenta, TextView titular, TextView subtitulo) {
            this.cuenta = cuenta;
            this.titular = titular;
            this.subtitulo = subtitulo;
        }
    }

    private final List<Fila> filas = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tic = new Runnable() {
        @Override
        public void run() {
            long ahora = System.currentTimeMillis();
            for (Fila f : filas) {
                f.titular.setText(Tiempo.titular(f.cuenta.inicio, ahora));
                f.subtitulo.setText(Tiempo.subtitulo(f.cuenta.inicio, ahora));
            }
            handler.postDelayed(this, 1000 - ahora % 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Paleta.aplicarTema(this);
        super.onCreate(savedInstanceState);
        p = Paleta.de(this);
        store = CuentasStore.de(this);

        FrameLayout raiz = new FrameLayout(this);
        Ui.prepararVentana(this, p, raiz);

        ScrollView scroll = new ScrollView(this);
        LinearLayout contenido = Ui.columna(this);
        contenido.setPadding(Ui.dp(this, 16), Ui.dp(this, 24), Ui.dp(this, 16), Ui.dp(this, 110));

        LinearLayout cabecera = Ui.fila(this);
        cabecera.addView(Ui.texto(this, "Cuánto llevo", 36, p.texto, true), Ui.peso(1));
        cabecera.addView(botonModo());
        contenido.addView(cabecera, Ui.lp(Ui.MATCH, Ui.WRAP));
        TextView sub = Ui.texto(this, "Cada cuenta empieza a correr en el momento en que la creás.", 15, p.gris, false);
        contenido.addView(sub, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 6, 0, 20));

        lista = Ui.columna(this);
        contenido.addView(lista, Ui.lp(Ui.MATCH, Ui.WRAP));
        scroll.addView(contenido);
        raiz.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        Button nueva = Ui.boton(this, p, "+ Nueva cuenta", true);
        nueva.setElevation(Ui.dp(this, 6));
        nueva.setOnClickListener(v -> startActivity(new Intent(this, NuevaCuentaActivity.class)));
        FrameLayout.LayoutParams lpBoton = new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 56), Gravity.BOTTOM);
        int m = Ui.dp(this, 16);
        lpBoton.setMargins(m, m, m, m);
        raiz.addView(nueva, lpBoton);

        setContentView(raiz);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dibujarLista();
        handler.post(tic);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tic);
    }

    /** Botón que alterna Sistema → Claro → Oscuro y redibuja la pantalla. */
    private View botonModo() {
        String modo = Paleta.modo(this);
        String nombre = Paleta.CLARO.equals(modo) ? "Claro" : Paleta.OSCURO.equals(modo) ? "Oscuro" : "Sistema";
        String simbolo = Paleta.CLARO.equals(modo) ? "○" : Paleta.OSCURO.equals(modo) ? "●" : "◐";
        TextView b = Ui.texto(this, simbolo + "  " + nombre, 13, p.texto, true);
        b.setPadding(Ui.dp(this, 12), Ui.dp(this, 7), Ui.dp(this, 12), Ui.dp(this, 7));
        b.setBackground(Ui.conToque(Ui.forma(this, 0x00000000, p.linea, 1, 999)));
        b.setContentDescription("Modo de color: " + nombre);
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(v -> {
            String siguiente = Paleta.CLARO.equals(modo) ? Paleta.OSCURO
                    : Paleta.OSCURO.equals(modo) ? Paleta.SISTEMA : Paleta.CLARO;
            Paleta.guardarModo(this, siguiente);
            recreate();
        });
        return b;
    }

    private void dibujarLista() {
        lista.removeAllViews();
        filas.clear();
        List<Cuenta> cuentas = store.todas();
        if (cuentas.isEmpty()) {
            lista.addView(vacio(this), Ui.lp(Ui.MATCH, Ui.WRAP));
            return;
        }
        long ahora = System.currentTimeMillis();
        for (Cuenta c : cuentas) {
            lista.addView(tarjeta(c, ahora), Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 12));
        }
    }

    private View tarjeta(Cuenta c, long ahora) {
        int color = p.de(c.color);
        LinearLayout t = Ui.columna(this);
        t.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        t.setBackground(Ui.conToque(Ui.forma(this, p.superficie, p.linea, 1, 14)));
        t.setClickable(true);
        t.setFocusable(true);
        t.setOnClickListener(v -> startActivity(
                new Intent(this, DetalleActivity.class).putExtra(DetalleActivity.EXTRA_ID, c.id)));

        LinearLayout cabecera = Ui.fila(this);
        cabecera.addView(Ui.punto(this, color, 12));
        TextView nombre = Ui.texto(this, c.nombre, 17, p.texto, true);
        cabecera.addView(nombre, Ui.conMargenes(Ui.peso(1), this, 8, 0, 8, 0));
        cabecera.addView(Ui.chip(this, p, c.reinicios, color));
        t.addView(cabecera);

        TextView titular = Ui.mono(this, Tiempo.titular(c.inicio, ahora), 24, color, true);
        t.addView(titular, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 8, 0, 4));
        TextView subtitulo = Ui.mono(this, Tiempo.subtitulo(c.inicio, ahora), 12, p.gris, false);
        t.addView(subtitulo);

        filas.add(new Fila(c, titular, subtitulo));
        return t;
    }

    private View vacio(Context c) {
        LinearLayout v = Ui.columna(c);
        v.setGravity(Gravity.CENTER_HORIZONTAL);
        v.setPadding(Ui.dp(c, 20), Ui.dp(c, 32), Ui.dp(c, 20), Ui.dp(c, 32));
        GradientDrawable borde = Ui.forma(c, 0x00000000, p.linea, 0, 14);
        borde.setStroke(Ui.dp(c, 1.5f), p.linea, Ui.dp(c, 6), Ui.dp(c, 4));
        v.setBackground(borde);

        TextView reloj = Ui.mono(c, "00:00:00", 26, p.texto, true);
        reloj.setAlpha(.35f);
        v.addView(reloj);
        TextView titulo = Ui.texto(c, "Todavía no tenés cuentas", 20, p.texto, true);
        v.addView(titulo, Ui.conMargenes(Ui.lp(Ui.WRAP, Ui.WRAP), c, 0, 8, 0, 8));
        TextView ayuda = Ui.texto(c, "Tocá “Nueva cuenta”, ponele un nombre y el reloj arranca en ese mismo segundo.", 15, p.gris, false);
        ayuda.setGravity(Gravity.CENTER_HORIZONTAL);
        v.addView(ayuda);
        return v;
    }
}
