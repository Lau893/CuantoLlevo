package com.cuantollevo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Guarda las cuentas en el teléfono (SharedPreferences, como JSON). Una sola instancia para toda la app. */
public final class CuentasStore {

    private static final String PREFS = "cuanto_llevo";
    private static final String KEY = "cuentas";

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

    /** Las cuentas, la más nueva primero. */
    public List<Cuenta> todas() {
        List<Cuenta> copia = new ArrayList<>(cuentas);
        Collections.sort(copia, (a, b) -> Long.compare(b.creada, a.creada));
        return copia;
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

    public void agregar(Cuenta c) {
        cuentas.add(c);
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

    public void reiniciar(String id) {
        Cuenta c = buscar(id);
        if (c == null) return;
        c.inicio = System.currentTimeMillis();
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
                        o.optString("color", "")));
            }
        } catch (JSONException e) {
            cuentas.clear();
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
                        .put("color", c.color));
            }
        } catch (JSONException e) {
            return;
        }
        prefs.edit().putString(KEY, arr.toString()).apply();
    }
}
