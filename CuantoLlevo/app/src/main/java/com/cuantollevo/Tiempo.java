package com.cuantollevo;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Cálculos de tiempo transcurrido y textos para mostrarlo. */
public final class Tiempo {

    private Tiempo() {}

    public static final Locale AR = Locale.forLanguageTag("es-AR");

    private static final NumberFormat ENTEROS = NumberFormat.getIntegerInstance(AR);
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", AR);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", AR);

    /**
     * {años, meses, días, horas, minutos, segundos}. Años y meses son de calendario
     * (del 31/1 al 28/2 es "28 días", no "1 mes").
     */
    public static long[] desglose(long inicio, long ahora) {
        if (ahora <= inicio) return new long[] {0, 0, 0, 0, 0, 0};
        ZoneId zona = ZoneId.systemDefault();
        LocalDateTime a = Instant.ofEpochMilli(inicio).atZone(zona).toLocalDateTime();
        LocalDateTime b = Instant.ofEpochMilli(ahora).atZone(zona).toLocalDateTime();
        long anios = ChronoUnit.YEARS.between(a, b);
        LocalDateTime t = a.plusYears(anios);
        long meses = ChronoUnit.MONTHS.between(t, b);
        t = t.plusMonths(meses);
        long resto = Duration.between(t, b).getSeconds();
        long dias = resto / 86_400;
        resto -= dias * 86_400;
        long horas = resto / 3_600;
        resto -= horas * 3_600;
        long minutos = resto / 60;
        return new long[] {anios, meses, dias, horas, minutos, resto - minutos * 60};
    }

    public static long segundos(long inicio, long ahora) {
        return Math.max(0L, (ahora - inicio) / 1000);
    }

    static String plural(long n, String uno, String muchos) {
        return n == 1 ? uno : muchos;
    }

    public static String numero(long n) {
        return ENTEROS.format(n);
    }

    /** "12 días · 04:37:09", o solo el reloj si todavía no pasó un día. */
    public static String titular(long inicio, long ahora) {
        long sec = segundos(inicio, ahora);
        long dias = sec / 86_400;
        long r = sec % 86_400;
        String reloj = String.format(Locale.ROOT, "%02d:%02d:%02d", r / 3600, r % 3600 / 60, r % 60);
        return dias > 0 ? numero(dias) + " " + plural(dias, "día", "días") + " · " + reloj : reloj;
    }

    /** "1 año · 2 meses · 3 días · 4 h 5 min" */
    public static String subtitulo(long inicio, long ahora) {
        long[] d = desglose(inicio, ahora);
        List<String> partes = new ArrayList<>();
        if (d[0] > 0) partes.add(d[0] + " " + plural(d[0], "año", "años"));
        if (d[1] > 0) partes.add(d[1] + " " + plural(d[1], "mes", "meses"));
        if (d[2] > 0 || !partes.isEmpty()) partes.add(d[2] + " " + plural(d[2], "día", "días"));
        partes.add(d[3] + " h " + d[4] + " min");
        return String.join(" · ", partes);
    }

    public static String reinicios(int n) {
        return n == 0 ? "Sin reinicios" : n + " " + plural(n, "reinicio", "reinicios");
    }

    public static String fecha(long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(FECHA);
    }

    public static String hora(long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(HORA);
    }

    public static String fechaYHora(long ms) {
        return fecha(ms) + ", " + hora(ms);
    }
}
