package com.cuantollevo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guarda las cuentas en el teléfono (SharedPreferences, como JSON). Una sola instancia para toda la app.
 * Las cuentas se guardan en el orden en que se muestran.
 */
public final class CuentasStore {

    private static final String PREFS = "cuanto_llevo";
    private static final String KEY = "cuentas";
    /** Antes el orden era siempre "la más nueva primero"; esta marca indica que ya se guardó en el JSON. */
    private static final String KEY_ORDEN = "orden_guardado";

    private static CuentasStore instancia;

    public static synchronized CuentasStore de(Context context) {
        if (instancia == null) instancia = new CuentasStore(context.getApplicationContext());
        return instancia;
    }

    private final SharedPreferences prefs;
    private final List<Cuenta> cuentas = new ArrayList<>();

    private CuentasStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        cargar();
    }

    /** Las cuentas, en el orden elegido. */
    public List<Cuenta> todas() {
        return new ArrayList<>(cuentas);
    }

    public Cuenta buscar(String id) {
        for (Cuenta c : cuentas) if (c.id.equals(id)) return c;
        return null;
    }

    public List<String> coloresUsados() {
        List<String> usados = new ArrayList<>();
        for (Cuenta c : cuentas) usados.add(c.color);
        return usados;
    }

    public int cantidad() {
        return cuentas.size();
    }

    /** Las nuevas van arriba de todo. */
    public void agregar(Cuenta c) {
        cuentas.add(0, c);
        guardar();
    }

    /** Sube (-1) o baja (+1) una cuenta un lugar en la lista. */
    public void mover(String id, int direccion) {
        Cuenta c = buscar(id);
        if (c == null) return;
        int i = cuentas.indexOf(c);
        int j = i + direccion;
        if (j < 0 || j >= cuentas.size()) return;
        Collections.swap(cuentas, i, j);
        guardar();
    }

    public void renombrar(String id, String nombre) {
        Cuenta c = buscar(id);
        if (c == null) return;
        c.nombre = nombre;
        guardar();
    }

    public void cambiarColor(String id, String color) {
        Cuenta c = buscar(id);
        if (c == null) return;
        c.color = color;
        guardar();
    }

    public void detener(String id) {
        Cuenta c = buscar(id);
        if (c == null || c.detenida()) return;
        c.detenidaEn = System.currentTimeMillis();
        guardar();
    }

    public void retomar(String id) {
        Cuenta c = buscar(id);
        if (c == null || !c.detenida()) return;
        c.pausado += Math.max(0L, System.currentTimeMillis() - c.detenidaEn);
        c.detenidaEn = 0;
        guardar();
    }

    public void reiniciar(String id) {
        reiniciarDesde(id, System.currentTimeMillis());
    }

    /** Vuelve a contar desde {@code desde} (corriendo, sin pausas) y suma un reinicio. */
    public void reiniciarDesde(String id, long desde) {
        Cuenta c = buscar(id);
        if (c == null) return;
        c.inicio = desde;
        c.pausado = 0;
        c.detenidaEn = 0;
        c.reinicios++;
        guardar();
    }

    public void borrar(String id) {
        Cuenta c = buscar(id);
        if (c == null) return;
        cuentas.remove(c);
        guardar();
    }

    private void cargar() {
        String raw = prefs.getString(KEY, null);
        if (raw == null) return;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                cuentas.add(new Cuenta(
                        o.getString("id"),
                        o.getString("nombre"),
                        o.getLong("inicio"),
                        o.getLong("creada"),
                        o.optInt("reinicios", 0),
                        o.optString("color", ""),
                        o.optLong("pausado", 0),
                        o.optLong("detenidaEn", 0)));
            }
        } catch (JSONException e) {
            cuentas.clear();
            return;
        }
        if (!prefs.getBoolean(KEY_ORDEN, false)) {
            Collections.sort(cuentas, (a, b) -> Long.compare(b.creada, a.creada));
            guardar();
        }
    }

    private void guardar() {
        JSONArray arr = new JSONArray();
        try {
            for (Cuenta c : cuentas) {
                arr.put(new JSONObject()
                        .put("id", c.id)
                        .put("nombre", c.nombre)
                        .put("inicio", c.inicio)
                        .put("creada", c.creada)
                        .put("reinicios", c.reinicios)
                        .put("color", c.color)
                        .put("pausado", c.pausado)
                        .put("detenidaEn", c.detenidaEn));
            }
        } catch (JSONException e) {
            return;
        }
        prefs.edit().putString(KEY, arr.toString()).putBoolean(KEY_ORDEN, true).apply();
    }
}
