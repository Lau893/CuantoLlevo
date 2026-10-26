package com.cuantollevo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Título + los 8 colores en dos filas de 4. El elegido lleva un anillo alrededor. */
public class SelectorColor extends LinearLayout {

    public interface AlElegir {
        void elegir(String clave);
    }

    private final Paleta p;
    private final AlElegir alElegir;
    private final LinearLayout circulos;
    private String seleccionado;

    public SelectorColor(Context c, Paleta p, String titulo, String seleccionado, AlElegir alElegir) {
        super(c);
        this.p = p;
        this.alElegir = alElegir;
        this.seleccionado = seleccionado;
        setOrientation(VERTICAL);

        TextView t = Ui.texto(c, titulo, 14, p.texto, true);
        addView(t, Ui.conMargenes(Ui.lp(Ui.WRAP, Ui.WRAP), c, 0, 0, 0, 8));

        circulos = Ui.columna(c);
        addView(circulos);
        dibujar();
    }

    public String getSeleccionado() {
        return seleccionado;
    }

    private void dibujar() {
        Context c = getContext();
        circulos.removeAllViews();
        ColorCuenta[] todos = ColorCuenta.values();
        for (int f = 0; f < 2; f++) {
            LinearLayout fila = Ui.fila(c);
            for (int i = 0; i < 4; i++) {
                ColorCuenta cc = todos[f * 4 + i];
                View v = new View(c);
                v.setBackground(circulo(c, cc, cc.clave.equals(seleccionado)));
                v.setContentDescription(cc.nombre);
                v.setClickable(true);
                v.setFocusable(true);
                v.setOnClickListener(x -> {
                    seleccionado = cc.clave;
                    dibujar();
                    alElegir.elegir(cc.clave);
                });
                fila.addView(v, Ui.conMargenes(Ui.lp(Ui.dp(c, 44), Ui.dp(c, 44)), c, 0, 0, 12, 0));
            }
            circulos.addView(fila, Ui.conMargenes(Ui.lp(Ui.WRAP, Ui.WRAP), c, 0, 0, 0, 10));
        }
    }

    private Drawable circulo(Context c, ColorCuenta cc, boolean activo) {
        GradientDrawable anillo = new GradientDrawable();
        anillo.setShape(GradientDrawable.OVAL);
        anillo.setColor(0x00000000);
        anillo.setStroke(Ui.dp(c, activo ? 2.5f : 1f), activo ? p.texto : p.linea);

        GradientDrawable relleno = new GradientDrawable();
        relleno.setShape(GradientDrawable.OVAL);
        relleno.setColor(cc.valor(p.oscuro));

        LayerDrawable capas = new LayerDrawable(new Drawable[] {anillo, relleno});
        int hueco = Ui.dp(c, 5);
        capas.setLayerInset(1, hueco, hueco, hueco, hueco);
        return capas;
    }
}
