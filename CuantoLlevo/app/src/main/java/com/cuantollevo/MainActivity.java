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

    private static final String ESTADO_ORDENANDO = "ordenando";

    private Paleta p;
    private CuentasStore store;
    private LinearLayout lista;
    private TextView botonOrdenar;
    private Button nueva;
    /** Mientras se ordena, cada tarjeta muestra ↑ ↓ en lugar de abrir el detalle. */
    private boolean ordenando;

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
                f.titular.setText(Tiempo.titular(f.cuenta.desde(), f.cuenta.fin(ahora)));
                f.subtitulo.setText(subtitulo(f.cuenta, ahora));
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
        if (savedInstanceState != null) ordenando = savedInstanceState.getBoolean(ESTADO_ORDENANDO);

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
        contenido.addView(sub, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 6, 0, 12));

        // Ordenar / Listo, a la derecha encima de la lista
        LinearLayout barra = Ui.fila(this);
        barra.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        botonOrdenar = Ui.texto(this, "", 13, p.texto, true);
        botonOrdenar.setPadding(Ui.dp(this, 12), Ui.dp(this, 7), Ui.dp(this, 12), Ui.dp(this, 7));
        botonOrdenar.setClickable(true);
        botonOrdenar.setFocusable(true);
        botonOrdenar.setOnClickListener(v -> {
            ordenando = !ordenando;
            dibujarLista();
        });
        barra.addView(botonOrdenar);
        contenido.addView(barra, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 8));

        lista = Ui.columna(this);
        contenido.addView(lista, Ui.lp(Ui.MATCH, Ui.WRAP));
        scroll.addView(contenido);
        raiz.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        nueva = Ui.boton(this, p, "+ Nueva cuenta", true);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ESTADO_ORDENANDO, ordenando);
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
        if (cuentas.size() < 2) ordenando = false;

        botonOrdenar.setVisibility(cuentas.size() < 2 ? View.GONE : View.VISIBLE);
        botonOrdenar.setText(ordenando ? "Listo" : "↑↓  Ordenar");
        botonOrdenar.setTextColor(ordenando ? p.sobreTexto : p.texto);
        botonOrdenar.setBackground(Ui.conToque(ordenando
                ? Ui.forma(this, p.texto, 0, 0, 999)
                : Ui.forma(this, 0x00000000, p.linea, 1, 999)));
        nueva.setVisibility(ordenando ? View.GONE : View.VISIBLE);

        if (cuentas.isEmpty()) {
            lista.addView(vacio(this), Ui.lp(Ui.MATCH, Ui.WRAP));
            return;
        }
        long ahora = System.currentTimeMillis();
        for (int i = 0; i < cuentas.size(); i++) {
            lista.addView(tarjeta(cuentas.get(i), ahora, i == 0, i == cuentas.size() - 1),
                    Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 12));
        }
    }

    private View tarjeta(Cuenta c, long ahora, boolean primera, boolean ultima) {
        int color = p.de(c.color);
        LinearLayout t = Ui.columna(this);
        t.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        if (ordenando) {
            t.setBackground(Ui.forma(this, p.superficie, p.linea, 1, 14));
        } else {
            t.setBackground(Ui.conToque(Ui.forma(this, p.superficie, p.linea, 1, 14)));
            t.setClickable(true);
            t.setFocusable(true);
            t.setOnClickListener(v -> startActivity(
                    new Intent(this, DetalleActivity.class).putExtra(DetalleActivity.EXTRA_ID, c.id)));
        }

        LinearLayout cabecera = Ui.fila(this);
        cabecera.addView(Ui.punto(this, color, 12));
        TextView nombre = Ui.texto(this, c.nombre, 17, p.texto, true);
        cabecera.addView(nombre, Ui.conMargenes(Ui.peso(1), this, 8, 0, 8, 0));
        if (ordenando) {
            cabecera.addView(flecha("↑", "Subir " + c.nombre, !primera, () -> mover(c, -1)));
            View abajo = flecha("↓", "Bajar " + c.nombre, !ultima, () -> mover(c, 1));
            cabecera.addView(abajo, Ui.conMargenes(Ui.lp(Ui.dp(this, 40), Ui.dp(this, 40)), this, 8, 0, 0, 0));
        } else {
            cabecera.addView(Ui.chip(this, p, c.reinicios, color));
        }
        t.addView(cabecera);

        TextView titular = Ui.mono(this, Tiempo.titular(c.desde(), c.fin(ahora)), 24, color, true);
        if (c.detenida()) titular.setAlpha(.45f);
        t.addView(titular, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 8, 0, 4));
        TextView subtitulo = Ui.mono(this, subtitulo(c, ahora), 12, p.gris, false);
        t.addView(subtitulo);

        filas.add(new Fila(c, titular, subtitulo));
        return t;
    }

    private static String subtitulo(Cuenta c, long ahora) {
        String s = Tiempo.subtitulo(c.desde(), c.fin(ahora));
        return c.detenida() ? "Detenida · " + s : s;
    }

    /** Botón redondo ↑ o ↓ del modo ordenar; atenuado si la cuenta ya está en el borde. */
    private TextView flecha(String simbolo, String descripcion, boolean activa, Runnable accion) {
        TextView b = Ui.texto(this, simbolo, 18, p.texto, true);
        b.setGravity(Gravity.CENTER);
        b.setLayoutParams(Ui.lp(Ui.dp(this, 40), Ui.dp(this, 40)));
        b.setBackground(Ui.conToque(Ui.forma(this, 0x00000000, p.linea, 1, 999)));
        b.setContentDescription(descripcion);
        b.setEnabled(activa);
        b.setAlpha(activa ? 1f : .3f);
        if (activa) b.setOnClickListener(v -> accion.run());
        return b;
    }

    private void mover(Cuenta c, int direccion) {
        store.mover(c.id, direccion);
        dibujarLista();
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
