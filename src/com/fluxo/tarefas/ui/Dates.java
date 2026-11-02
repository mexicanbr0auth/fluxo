package com.fluxo.tarefas.ui;

import com.fluxo.tarefas.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Dates {

    public static final Locale PT = Locale.forLanguageTag("pt-BR");

    private Dates() {
    }

    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static int dayDiff(long a, long b) {
        return (int) Math.round((startOfDay(a) - startOfDay(b)) / 86400000.0);
    }

    public static String fullDate(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("EEEE, d 'de' MMMM", PT);
        return capitalize(f.format(new Date(millis)));
    }

    public static String shortDate(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("d 'de' MMM", PT);
        return f.format(new Date(millis));
    }

    public static String shortDateYear(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("d 'de' MMM 'de' yyyy", PT);
        return f.format(new Date(millis));
    }

    public static String time(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", PT);
        return f.format(new Date(millis));
    }

    public static String relative(long millis, long now) {
        int d = dayDiff(millis, now);
        if (d == 0) return "Hoje";
        if (d == 1) return "Amanhã";
        if (d == -1) return "Ontem";
        Calendar a = Calendar.getInstance();
        a.setTimeInMillis(millis);
        Calendar b = Calendar.getInstance();
        b.setTimeInMillis(now);
        if (a.get(Calendar.YEAR) == b.get(Calendar.YEAR)) return shortDate(millis);
        return shortDateYear(millis);
    }

    public static String dueLabel(Task t) {
        if (t.dueAt <= 0) return "Sem prazo";
        String base = relative(t.dueAt, System.currentTimeMillis());
        if (t.hasTime) base += ", " + time(t.dueAt);
        return base;
    }

    public static String dueSummary(Task t) {
        return dueSummary(t.dueAt, t.hasTime);
    }

    public static String dueSummary(long dueAt, boolean hasTime) {
        if (dueAt <= 0) return "Sem prazo definido";
        String base = fullDate(dueAt);
        if (hasTime) base += " às " + time(dueAt);
        return base;
    }

    public static boolean isOverdue(Task t) {
        return !t.done && t.dueAt > 0 && dayDiff(t.dueAt, System.currentTimeMillis()) < 0;
    }

    /** Rótulo curto usado nos cartões e chips. */
    public static String repeatLabel(int repeat) {
        switch (repeat) {
            case 1:
                return "Diária";
            case 2:
                return "Dias úteis";
            case 3:
                return "Semanal";
            case 4:
                return "Mensal";
            default:
                return "";
        }
    }

    /** Frase completa usada no editor. */
    public static String repeatSummary(int repeat) {
        switch (repeat) {
            case 1:
                return "Repete todos os dias";
            case 2:
                return "Repete de segunda a sexta";
            case 3:
                return "Repete toda semana";
            case 4:
                return "Repete todo mês";
            default:
                return "";
        }
    }

    /**
     * Calcula a próxima ocorrência de uma tarefa recorrente.
     * Mantém o horário do prazo e sempre retorna uma data futura em relação a agora.
     */
    public static long nextOccurrence(long dueAt, int repeat, long now) {
        if (repeat <= 0) return 0L;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dueAt > 0 ? dueAt : startOfDay(now));
        int guard = 0;
        do {
            advance(c, repeat);
            guard++;
        } while (c.getTimeInMillis() <= now && guard < 800);
        return c.getTimeInMillis();
    }

    private static void advance(Calendar c, int repeat) {
        switch (repeat) {
            case 1: // diária
                c.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case 2: // dias úteis
                do {
                    c.add(Calendar.DAY_OF_YEAR, 1);
                } while (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                        || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
                break;
            case 4: // mensal
                c.add(Calendar.MONTH, 1);
                break;
            case 3: // semanal
            default:
                c.add(Calendar.DAY_OF_YEAR, 7);
                break;
        }
    }

    public static String chartDay(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("EEE", PT);
        return f.format(new Date(millis)).replace(".", "").toLowerCase(PT);
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
