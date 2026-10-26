package com.cuantollevo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Pantalla de una cuenta: desglose completo, reinicios, color, reiniciar y borrar. */
public class DetalleActivity extends Activity {

    public static final String EXTRA_ID = "id";

    private static final String[] ETIQUETAS = {"AÑOS", "MESES", "DÍAS", "HORAS", "MINUTOS", "SEGUNDOS"};

    private Paleta p;
    private CuentasStore store;
    private Cuenta cuenta;

    private View franja, punto;
    private TextView nombre, desde, numReinicios, palabraReinicios, notaReinicios;
    private TextView totalDias, totalHorasMin, totalSegundos, textoConfirmar;
    private final TextView[] celdas = new TextView[6];
    private LinearLayout acciones, confirmar;
    private Button botonSi;
    private String pendiente; // "reiniciar" | "borrar" | null

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tic = new Runnable() {
        @Override
        public void run() {
            long ahora = System.currentTimeMillis();
            mostrar(ahora);
            handler.postDelayed(this, 1000 - ahora % 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Paleta.aplicarTema(this);
        super.onCreate(savedInstanceState);
        p = Paleta.de(this);
        store = CuentasStore.de(this);
        cuenta = store.buscar(getIntent().getStringExtra(EXTRA_ID));
        if (cuenta == null) {
            finish();
            return;
        }

        LinearLayout raiz = Ui.columna(this);
        Ui.prepararVentana(this, p, raiz);

        // Franja del color de la cuenta
        franja = new View(this);
        raiz.addView(franja, Ui.lp(Ui.MATCH, Ui.dp(this, 4)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout c = Ui.columna(this);
        int g = Ui.dp(this, 16);
        c.setPadding(g, Ui.dp(this, 20), g, Ui.dp(this, 24));

        // Título
        LinearLayout cabecera = Ui.fila(this);
        punto = Ui.punto(this, p.texto, 16);
        cabecera.addView(punto);
        nombre = Ui.texto(this, "", 26, p.texto, true);
        cabecera.addView(nombre, Ui.conMargenes(Ui.peso(1), this, 10, 0, 0, 0));
        c.addView(cabecera);
        desde = Ui.texto(this, "", 14, p.gris, false);
        c.addView(desde, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 4, 0, 16));

        // Grilla de 3 x 2
        for (int f = 0; f < 2; f++) {
            LinearLayout fila = Ui.fila(this);
            for (int i = 0; i < 3; i++) {
                int k = f * 3 + i;
                LinearLayout celda = Ui.columna(this);
                celda.setGravity(Gravity.CENTER_HORIZONTAL);
                celda.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
                celda.setBackground(Ui.forma(this, p.celda, 0, 0, 10));
                celdas[k] = Ui.mono(this, "0", 22, p.texto, true);
                celdas[k].setGravity(Gravity.CENTER_HORIZONTAL);
                celda.addView(celdas[k]);
                TextView etiqueta = Ui.texto(this, ETIQUETAS[k], 10, p.gris, false);
                etiqueta.setLetterSpacing(.08f);
                etiqueta.setGravity(Gravity.CENTER_HORIZONTAL);
                celda.addView(etiqueta);
                fila.addView(celda, Ui.conMargenes(Ui.peso(1), this, i == 0 ? 0 : 4, 0, i == 2 ? 0 : 4, 0));
            }
            c.addView(fila, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 8));
        }

        // Reinicios
        c.addView(Ui.separador(this, p), Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.dp(this, 1)), this, 0, 8, 0, 0));
        LinearLayout filaReinicios = Ui.fila(this);
        filaReinicios.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
        numReinicios = Ui.mono(this, "0", 22, p.texto, true);
        filaReinicios.addView(numReinicios);
        palabraReinicios = Ui.texto(this, "", 15, p.texto, true);
        filaReinicios.addView(palabraReinicios, Ui.conMargenes(Ui.lp(Ui.WRAP, Ui.WRAP), this, 8, 0, 8, 0));
        notaReinicios = Ui.texto(this, "", 12, p.gris, false);
        notaReinicios.setGravity(Gravity.END);
        filaReinicios.addView(notaReinicios, Ui.peso(1));
        c.addView(filaReinicios);
        c.addView(Ui.separador(this, p), Ui.lp(Ui.MATCH, Ui.dp(this, 1)));

        // Totales
        totalDias = Ui.mono(this, "", 12, p.gris, false);
        totalHorasMin = Ui.mono(this, "", 12, p.gris, false);
        totalSegundos = Ui.mono(this, "", 12, p.gris, false);
        c.addView(totalDias, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 14, 0, 2));
        c.addView(totalHorasMin, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 2));
        c.addView(totalSegundos, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 20));

        // Color
        SelectorColor selector = new SelectorColor(this, p, "Color de la cuenta", cuenta.color, clave -> {
            store.cambiarColor(cuenta.id, clave);
            mostrar(System.currentTimeMillis());
        });
        c.addView(selector, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 14));

        // Reiniciar / Borrar
        acciones = Ui.fila(this);
        Button reiniciar = Ui.boton(this, p, "Reiniciar a cero", false);
        reiniciar.setOnClickListener(v -> preguntar("reiniciar"));
        acciones.addView(reiniciar, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        Button borrar = Ui.boton(this, p, "Borrar", false);
        borrar.setBackground(Ui.conToque(Ui.forma(this, 0x00000000, p.texto, 1, 999)));
        borrar.setOnClickListener(v -> preguntar("borrar"));
        acciones.addView(borrar, Ui.peso(1));
        c.addView(acciones, Ui.lp(Ui.MATCH, Ui.WRAP));

        // Confirmación (reemplaza a los botones mientras se muestra)
        confirmar = Ui.columna(this);
        confirmar.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        confirmar.setBackground(Ui.forma(this, 0x00000000, p.texto, 1, 12));
        textoConfirmar = Ui.texto(this, "", 15, p.texto, false);
        confirmar.addView(textoConfirmar, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 12));
        LinearLayout siNo = Ui.fila(this);
        Button no = Ui.boton(this, p, "No", false);
        no.setOnClickListener(v -> cancelarPregunta());
        siNo.addView(no, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        botonSi = Ui.boton(this, p, "", true);
        botonSi.setOnClickListener(v -> confirmarAccion());
        siNo.addView(botonSi, Ui.peso(1));
        confirmar.addView(siNo, Ui.lp(Ui.MATCH, Ui.WRAP));
        confirmar.setVisibility(View.GONE);
        c.addView(confirmar, Ui.lp(Ui.MATCH, Ui.WRAP));

        // Listo
        Button listo = Ui.boton(this, p, "Listo", true);
        listo.setOnClickListener(v -> finish());
        c.addView(listo, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 16, 0, 0));

        scroll.addView(c);
        raiz.addView(scroll, Ui.lp(Ui.MATCH, Ui.MATCH));
        setContentView(raiz);
        mostrar(System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cuenta != null) handler.post(tic);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tic);
    }

    private void mostrar(long ahora) {
        int color = p.de(cuenta.color);
        franja.setBackgroundColor(color);
        Ui.pintarPunto(punto, color);
        nombre.setText(cuenta.nombre);
        desde.setText("Desde el " + Tiempo.fechaYHora(cuenta.inicio));

        long[] d = Tiempo.desglose(cuenta.inicio, ahora);
        for (int i = 0; i < 6; i++) celdas[i].setText(String.valueOf(d[i]));

        numReinicios.setText(String.valueOf(cuenta.reinicios));
        numReinicios.setTextColor(color);
        palabraReinicios.setText(Tiempo.plural(cuenta.reinicios, "reinicio", "reinicios"));
        notaReinicios.setText(cuenta.reinicios > 0
                ? "Creada el " + Tiempo.fecha(cuenta.creada)
                : "Nunca la reiniciaste");

        long sec = Tiempo.segundos(cuenta.inicio, ahora);
        totalDias.setText("En total: " + Tiempo.numero(sec / 86_400) + " días");
        totalHorasMin.setText(Tiempo.numero(sec / 3_600) + " horas · " + Tiempo.numero(sec / 60) + " minutos");
        totalSegundos.setText(Tiempo.numero(sec) + " segundos");
    }

    private void preguntar(String accion) {
        pendiente = accion;
        if (accion.equals("reiniciar")) {
            textoConfirmar.setText("La cuenta vuelve a cero y empieza a correr desde ahora. Va a ser el reinicio n.º "
                    + (cuenta.reinicios + 1) + ". ¿Seguro?");
            botonSi.setText("Sí, reiniciar");
        } else {
            textoConfirmar.setText("Se borra esta cuenta para siempre. ¿Seguro?");
            botonSi.setText("Sí, borrar");
        }
        acciones.setVisibility(View.GONE);
        confirmar.setVisibility(View.VISIBLE);
    }

    private void cancelarPregunta() {
        pendiente = null;
        confirmar.setVisibility(View.GONE);
        acciones.setVisibility(View.VISIBLE);
    }

    private void confirmarAccion() {
        if ("borrar".equals(pendiente)) {
            store.borrar(cuenta.id);
            finish();
            return;
        }
        if ("reiniciar".equals(pendiente)) {
            store.reiniciar(cuenta.id);
            mostrar(System.currentTimeMillis());
        }
        cancelarPregunta();
    }
}
