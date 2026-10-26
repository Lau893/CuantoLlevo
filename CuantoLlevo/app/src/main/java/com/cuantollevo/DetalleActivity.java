package com.cuantollevo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/** Pantalla de una cuenta: desglose, reinicios, nombre, color, detener, reiniciar y borrar. */
public class DetalleActivity extends Activity {

    public static final String EXTRA_ID = "id";

    private static final String ESTADO_EDITANDO = "editando";

    private static final String[] ETIQUETAS = {"AÑOS", "MESES", "DÍAS", "HORAS", "MIN", "SEG"};

    private Paleta p;
    private CuentasStore store;
    private Cuenta cuenta;

    private View franja, punto;
    private TextView nombre, desde, numReinicios, palabraReinicios, notaReinicios;
    private TextView totalDias, totalHorasMin, totalSegundos, textoConfirmar, errorNombre, errorDesde;
    private EditText campoNombre;
    private LinearLayout cabecera, editorNombre;
    private final TextView[] celdas = new TextView[6];
    private LinearLayout acciones, confirmar, fechaHoraDesde;
    private Button botonSi, botonDetener, botonFechaDesde, botonHoraDesde;
    private String pendiente; // "reiniciar" | "desde" | "borrar" | null
    /** Fecha y hora elegidas para "Reiniciar desde fecha". */
    private long reinicioDesde;

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
        cabecera = Ui.fila(this);
        punto = Ui.punto(this, p.texto, 16);
        cabecera.addView(punto);
        nombre = Ui.texto(this, "", 26, p.texto, true);
        nombre.setOnClickListener(v -> editarNombre());
        cabecera.addView(nombre, Ui.conMargenes(Ui.peso(1), this, 10, 0, 0, 0));
        TextView editar = Ui.texto(this, "Editar", 14, p.gris, true);
        editar.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), 0, Ui.dp(this, 8));
        editar.setOnClickListener(v -> editarNombre());
        cabecera.addView(editar);
        c.addView(cabecera);

        // Editor del nombre (reemplaza al título mientras se muestra)
        editorNombre = Ui.columna(this);
        campoNombre = new EditText(this);
        campoNombre.setId(1002); // con id fijo, Android conserva lo escrito al girar el teléfono
        campoNombre.setSingleLine(true);
        campoNombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        campoNombre.setImeOptions(EditorInfo.IME_ACTION_DONE);
        campoNombre.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});
        campoNombre.setHint("Nombre de la cuenta");
        campoNombre.setTextColor(p.texto);
        campoNombre.setHintTextColor(p.gris);
        campoNombre.setBackground(Ui.forma(this, p.celda, p.linea, 1, 10));
        campoNombre.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        campoNombre.setOnEditorActionListener((v, accion, evento) -> {
            if (accion != EditorInfo.IME_ACTION_DONE) return false;
            guardarNombre();
            return true;
        });
        editorNombre.addView(campoNombre, Ui.lp(Ui.MATCH, Ui.WRAP));
        errorNombre = Ui.texto(this, "Escribí un nombre para la cuenta.", 13, p.texto, false);
        errorNombre.setVisibility(View.GONE);
        editorNombre.addView(errorNombre, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 6, 0, 0));
        LinearLayout cancelarGuardar = Ui.fila(this);
        Button cancelar = Ui.boton(this, p, "Cancelar", false);
        cancelar.setOnClickListener(v -> cerrarEditorNombre());
        cancelarGuardar.addView(cancelar, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        Button guardar = Ui.boton(this, p, "Guardar", true);
        guardar.setOnClickListener(v -> guardarNombre());
        cancelarGuardar.addView(guardar, Ui.peso(1));
        editorNombre.addView(cancelarGuardar, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 10, 0, 0));
        editorNombre.setVisibility(View.GONE);
        c.addView(editorNombre, Ui.lp(Ui.MATCH, Ui.WRAP));
        desde = Ui.texto(this, "", 14, p.gris, false);
        c.addView(desde, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 4, 0, 16));

        // Desglose compacto: una sola fila de 6
        LinearLayout grilla = Ui.fila(this);
        for (int k = 0; k < 6; k++) {
            LinearLayout celda = Ui.columna(this);
            celda.setGravity(Gravity.CENTER_HORIZONTAL);
            celda.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            celda.setBackground(Ui.forma(this, p.celda, 0, 0, 8));
            celdas[k] = Ui.mono(this, "0", 16, p.texto, true);
            celdas[k].setGravity(Gravity.CENTER_HORIZONTAL);
            celda.addView(celdas[k]);
            TextView etiqueta = Ui.texto(this, ETIQUETAS[k], 9, p.gris, false);
            etiqueta.setLetterSpacing(.05f);
            etiqueta.setGravity(Gravity.CENTER_HORIZONTAL);
            celda.addView(etiqueta);
            grilla.addView(celda, Ui.conMargenes(Ui.peso(1), this, k == 0 ? 0 : 2, 0, k == 5 ? 0 : 2, 0));
        }
        c.addView(grilla, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 8));

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

        // Detener-Retomar / Reiniciar a cero / Reiniciar desde fecha / Borrar
        acciones = Ui.columna(this);
        LinearLayout acciones1 = Ui.fila(this);
        botonDetener = Ui.boton(this, p, "", false);
        botonDetener.setOnClickListener(v -> detenerORetomar());
        acciones1.addView(botonDetener, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        Button reiniciar = Ui.boton(this, p, "Reiniciar a cero", false);
        reiniciar.setOnClickListener(v -> preguntar("reiniciar"));
        acciones1.addView(reiniciar, Ui.peso(1));
        acciones.addView(acciones1, Ui.lp(Ui.MATCH, Ui.WRAP));
        LinearLayout acciones2 = Ui.fila(this);
        Button reiniciarDesde = Ui.boton(this, p, "Reiniciar desde fecha", false);
        reiniciarDesde.setOnClickListener(v -> preguntar("desde"));
        acciones2.addView(reiniciarDesde, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        Button borrar = Ui.boton(this, p, "Borrar", false);
        borrar.setBackground(Ui.conToque(Ui.forma(this, 0x00000000, p.texto, 1, 999)));
        borrar.setOnClickListener(v -> preguntar("borrar"));
        acciones2.addView(borrar, Ui.peso(1));
        acciones.addView(acciones2, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 10, 0, 0));
        c.addView(acciones, Ui.lp(Ui.MATCH, Ui.WRAP));

        // Confirmación (reemplaza a los botones mientras se muestra)
        confirmar = Ui.columna(this);
        confirmar.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        confirmar.setBackground(Ui.forma(this, 0x00000000, p.texto, 1, 12));
        textoConfirmar = Ui.texto(this, "", 15, p.texto, false);
        confirmar.addView(textoConfirmar, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 12));
        // Fecha y hora, solo para "Reiniciar desde fecha"
        fechaHoraDesde = Ui.fila(this);
        botonFechaDesde = Ui.boton(this, p, "", false);
        botonFechaDesde.setOnClickListener(v -> elegirFechaDesde());
        fechaHoraDesde.addView(botonFechaDesde, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        botonHoraDesde = Ui.boton(this, p, "", false);
        botonHoraDesde.setOnClickListener(v -> elegirHoraDesde());
        fechaHoraDesde.addView(botonHoraDesde, Ui.lp(Ui.WRAP, Ui.WRAP));
        confirmar.addView(fechaHoraDesde, Ui.lp(Ui.MATCH, Ui.WRAP));
        errorDesde = Ui.texto(this, "La fecha no puede estar en el futuro.", 13, p.texto, true);
        errorDesde.setVisibility(View.GONE);
        confirmar.addView(errorDesde, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 8, 0, 0));
        LinearLayout siNo = Ui.fila(this);
        Button no = Ui.boton(this, p, "No", false);
        no.setOnClickListener(v -> cancelarPregunta());
        siNo.addView(no, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        botonSi = Ui.boton(this, p, "", true);
        botonSi.setOnClickListener(v -> confirmarAccion());
        siNo.addView(botonSi, Ui.peso(1));
        confirmar.addView(siNo, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 12, 0, 0));
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
        if (savedInstanceState != null && savedInstanceState.getBoolean(ESTADO_EDITANDO)) {
            // Lo escrito lo restaura Android solo; acá se vuelve a abrir el editor
            cabecera.setVisibility(View.GONE);
            editorNombre.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editorNombre != null) outState.putBoolean(ESTADO_EDITANDO, editorNombre.getVisibility() == View.VISIBLE);
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
        String textoDesde = "Desde el " + Tiempo.fechaYHora(cuenta.inicio);
        if (cuenta.detenida()) textoDesde += "\nDetenida el " + Tiempo.fechaYHora(cuenta.detenidaEn);
        desde.setText(textoDesde);
        botonDetener.setText(cuenta.detenida() ? "Retomar" : "Detener");

        // Detenida: el reloj queda quieto en lo que llevaba
        long inicio = cuenta.desde();
        long fin = cuenta.fin(ahora);
        long[] d = Tiempo.desglose(inicio, fin);
        for (int i = 0; i < 6; i++) {
            celdas[i].setText(String.valueOf(d[i]));
            celdas[i].setAlpha(cuenta.detenida() ? .45f : 1f);
        }

        numReinicios.setText(String.valueOf(cuenta.reinicios));
        numReinicios.setTextColor(color);
        palabraReinicios.setText(Tiempo.plural(cuenta.reinicios, "reinicio", "reinicios"));
        notaReinicios.setText(cuenta.reinicios > 0
                ? "Creada el " + Tiempo.fecha(cuenta.creada)
                : "Nunca la reiniciaste");

        long sec = Tiempo.segundos(inicio, fin);
        totalDias.setText("En total: " + Tiempo.numero(sec / 86_400) + " días");
        totalHorasMin.setText(Tiempo.numero(sec / 3_600) + " horas · " + Tiempo.numero(sec / 60) + " minutos");
        totalSegundos.setText(Tiempo.numero(sec) + " segundos");
    }

    private void editarNombre() {
        campoNombre.setText(cuenta.nombre);
        campoNombre.setSelection(campoNombre.length());
        errorNombre.setVisibility(View.GONE);
        cabecera.setVisibility(View.GONE);
        editorNombre.setVisibility(View.VISIBLE);
        campoNombre.requestFocus();
        teclado().showSoftInput(campoNombre, InputMethodManager.SHOW_IMPLICIT);
    }

    private void guardarNombre() {
        String n = campoNombre.getText().toString().trim();
        if (n.isEmpty()) {
            errorNombre.setVisibility(View.VISIBLE);
            return;
        }
        store.renombrar(cuenta.id, n);
        cerrarEditorNombre();
        mostrar(System.currentTimeMillis());
    }

    private void cerrarEditorNombre() {
        teclado().hideSoftInputFromWindow(campoNombre.getWindowToken(), 0);
        campoNombre.clearFocus();
        editorNombre.setVisibility(View.GONE);
        cabecera.setVisibility(View.VISIBLE);
    }

    private InputMethodManager teclado() {
        return (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private void detenerORetomar() {
        if (cuenta.detenida()) store.retomar(cuenta.id);
        else store.detener(cuenta.id);
        mostrar(System.currentTimeMillis());
    }

    private void preguntar(String accion) {
        pendiente = accion;
        fechaHoraDesde.setVisibility(View.GONE);
        errorDesde.setVisibility(View.GONE);
        if (accion.equals("reiniciar")) {
            textoConfirmar.setText("La cuenta vuelve a cero y empieza a correr desde ahora. Va a ser el reinicio n.º "
                    + (cuenta.reinicios + 1) + ". ¿Seguro?");
            botonSi.setText("Sí, reiniciar");
        } else if (accion.equals("desde")) {
            textoConfirmar.setText("La cuenta vuelve a contar desde la fecha que elijas (la hora es opcional). Va a ser el reinicio n.º "
                    + (cuenta.reinicios + 1) + ".");
            botonSi.setText("Sí, reiniciar");
            // Por defecto, hoy a las 00:00
            reinicioDesde = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            mostrarFechaHoraDesde();
            fechaHoraDesde.setVisibility(View.VISIBLE);
        } else {
            textoConfirmar.setText("Se borra esta cuenta para siempre. ¿Seguro?");
            botonSi.setText("Sí, borrar");
        }
        acciones.setVisibility(View.GONE);
        confirmar.setVisibility(View.VISIBLE);
    }

    private void mostrarFechaHoraDesde() {
        botonFechaDesde.setText(Tiempo.fecha(reinicioDesde));
        botonHoraDesde.setText(Tiempo.hora(reinicioDesde));
    }

    private LocalDateTime desdeActual() {
        return Instant.ofEpochMilli(reinicioDesde).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private void fijarDesde(LocalDateTime t) {
        reinicioDesde = t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        errorDesde.setVisibility(View.GONE);
        mostrarFechaHoraDesde();
    }

    private void elegirFechaDesde() {
        LocalDateTime a = desdeActual();
        DatePickerDialog d = new DatePickerDialog(this,
                (vista, anio, mes, dia) -> fijarDesde(LocalDateTime.of(LocalDate.of(anio, mes + 1, dia), a.toLocalTime())),
                a.getYear(), a.getMonthValue() - 1, a.getDayOfMonth());
        d.getDatePicker().setMaxDate(System.currentTimeMillis());
        d.show();
    }

    private void elegirHoraDesde() {
        LocalDateTime a = desdeActual();
        new TimePickerDialog(this,
                (vista, hora, minuto) -> fijarDesde(LocalDateTime.of(a.toLocalDate(), LocalTime.of(hora, minuto))),
                a.getHour(), a.getMinute(), true).show();
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
        if ("desde".equals(pendiente)) {
            if (reinicioDesde > System.currentTimeMillis()) {
                errorDesde.setVisibility(View.VISIBLE);
                return;
            }
            store.reiniciarDesde(cuenta.id, reinicioDesde);
            mostrar(System.currentTimeMillis());
        }
        cancelarPregunta();
    }
}
