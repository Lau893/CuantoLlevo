package com.cuantollevo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

/** Pantalla para crear una cuenta: nombre, color y, si hace falta, cuándo empezó. */
public class NuevaCuentaActivity extends Activity {

    private static final String ESTADO_INICIO = "inicio";
    private static final String ESTADO_COLOR = "color";

    private Paleta p;
    private CuentasStore store;
    private EditText nombre;
    private SelectorColor selector;
    private CheckBox empezoAntes;
    private LinearLayout fechaHora;
    private Button botonFecha, botonHora;
    private TextView error;
    private View puntoTitulo;
    private long inicio = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Paleta.aplicarTema(this);
        super.onCreate(savedInstanceState);
        p = Paleta.de(this);
        store = CuentasStore.de(this);

        String colorInicial = ColorCuenta.sugerido(store.coloresUsados(), store.cantidad()).clave;
        if (savedInstanceState != null) {
            inicio = savedInstanceState.getLong(ESTADO_INICIO, inicio);
            colorInicial = savedInstanceState.getString(ESTADO_COLOR, colorInicial);
        }

        ScrollView scroll = new ScrollView(this);
        Ui.prepararVentana(this, p, scroll);
        LinearLayout c = Ui.columna(this);
        int g = Ui.dp(this, 16);
        c.setPadding(g, Ui.dp(this, 24), g, Ui.dp(this, 24));

        // Título con el punto del color elegido
        LinearLayout cabecera = Ui.fila(this);
        puntoTitulo = Ui.punto(this, p.de(colorInicial), 16);
        cabecera.addView(puntoTitulo);
        cabecera.addView(Ui.texto(this, "Nueva cuenta", 26, p.texto, true),
                Ui.conMargenes(Ui.lp(Ui.WRAP, Ui.WRAP), this, 10, 0, 0, 0));
        c.addView(cabecera, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 20));

        // Nombre
        c.addView(Ui.texto(this, "¿Qué querés contar?", 14, p.texto, true),
                Ui.conMargenes(Ui.lp(Ui.WRAP, Ui.WRAP), this, 0, 0, 0, 6));
        nombre = new EditText(this);
        nombre.setId(1001); // con id fijo, Android conserva lo escrito al girar el teléfono
        nombre.setHint("Ej: Sin fumar, Juntos, En el trabajo nuevo");
        nombre.setSingleLine(true);
        nombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nombre.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});
        nombre.setTextColor(p.texto);
        nombre.setHintTextColor(p.gris);
        nombre.setBackground(Ui.forma(this, p.celda, p.linea, 1, 10));
        nombre.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        c.addView(nombre, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 20));

        // Color
        selector = new SelectorColor(this, p, "Color", colorInicial,
                clave -> Ui.pintarPunto(puntoTitulo, p.de(clave)));
        c.addView(selector, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 0, 0, 10));

        // Empezó antes
        empezoAntes = new CheckBox(this);
        empezoAntes.setId(1002);
        empezoAntes.setText("Empezó antes (elegir fecha y hora)");
        empezoAntes.setTextColor(p.gris);
        c.addView(empezoAntes);

        fechaHora = Ui.fila(this);
        botonFecha = Ui.boton(this, p, "", false);
        botonFecha.setOnClickListener(v -> elegirFecha());
        fechaHora.addView(botonFecha, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        botonHora = Ui.boton(this, p, "", false);
        botonHora.setOnClickListener(v -> elegirHora());
        fechaHora.addView(botonHora, Ui.lp(Ui.WRAP, Ui.WRAP));
        fechaHora.setVisibility(View.GONE);
        c.addView(fechaHora, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 8, 0, 0));

        empezoAntes.setOnCheckedChangeListener((b, marcado) -> {
            fechaHora.setVisibility(marcado ? View.VISIBLE : View.GONE);
            error.setVisibility(View.GONE);
            mostrarFechaHora();
        });

        // Error
        error = Ui.texto(this, "", 14, p.texto, true);
        error.setVisibility(View.GONE);
        c.addView(error, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 16, 0, 0));

        // Botones
        LinearLayout acciones = Ui.fila(this);
        Button cancelar = Ui.boton(this, p, "Cancelar", false);
        cancelar.setOnClickListener(v -> finish());
        acciones.addView(cancelar, Ui.conMargenes(Ui.peso(1), this, 0, 0, 10, 0));
        Button crear = Ui.boton(this, p, "Empezar a contar", true);
        crear.setOnClickListener(v -> crear());
        acciones.addView(crear, Ui.peso(1));
        c.addView(acciones, Ui.conMargenes(Ui.lp(Ui.MATCH, Ui.WRAP), this, 0, 24, 0, 0));

        scroll.addView(c);
        setContentView(scroll);
        mostrarFechaHora();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ESTADO_INICIO, inicio);
        outState.putString(ESTADO_COLOR, selector.getSeleccionado());
    }

    private void mostrarFechaHora() {
        botonFecha.setText(Tiempo.fecha(inicio));
        botonHora.setText(Tiempo.hora(inicio));
    }

    private LocalDateTime actual() {
        return Instant.ofEpochMilli(inicio).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private void fijar(LocalDateTime t) {
        inicio = t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        error.setVisibility(View.GONE);
        mostrarFechaHora();
    }

    private void elegirFecha() {
        LocalDateTime a = actual();
        DatePickerDialog d = new DatePickerDialog(this,
                (vista, anio, mes, dia) -> fijar(LocalDateTime.of(LocalDate.of(anio, mes + 1, dia), a.toLocalTime())),
                a.getYear(), a.getMonthValue() - 1, a.getDayOfMonth());
        d.getDatePicker().setMaxDate(System.currentTimeMillis());
        d.show();
    }

    private void elegirHora() {
        LocalDateTime a = actual();
        new TimePickerDialog(this,
                (vista, hora, minuto) -> fijar(LocalDateTime.of(a.toLocalDate(), LocalTime.of(hora, minuto))),
                a.getHour(), a.getMinute(), true).show();
    }

    private void crear() {
        String n = nombre.getText().toString().trim();
        long ahora = System.currentTimeMillis();
        if (n.isEmpty()) {
            mostrarError("Escribí un nombre para la cuenta.");
            return;
        }
        if (empezoAntes.isChecked() && inicio > ahora) {
            mostrarError("La fecha de inicio no puede estar en el futuro.");
            return;
        }
        store.agregar(new Cuenta(
                UUID.randomUUID().toString(),
                n,
                empezoAntes.isChecked() ? inicio : ahora,
                ahora,
                0,
                selector.getSeleccionado()));
        finish();
    }

    private void mostrarError(String msg) {
        error.setText(msg);
        error.setVisibility(View.VISIBLE);
    }
}
