package com.fluxo.tarefas.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.fluxo.tarefas.R;
import com.fluxo.tarefas.data.Prefs;
import com.fluxo.tarefas.data.Store;
import com.fluxo.tarefas.model.Task;

import java.util.List;

/**
 * Agenda alarmes exatos para tarefas com data e horário definidos.
 * O alarme dispara {@link ReminderReceiver}, que mostra a notificação.
 */
public final class ReminderScheduler {

    public static final String ACTION_REMIND = "com.fluxo.tarefas.action.REMIND";
    public static final String EXTRA_TASK_ID = "task_id";

    private ReminderScheduler() {
    }

    /** Reagenda tudo: cancela alarmes inválidos e agenda os próximos. */
    public static void syncAll(Context c) {
        List<Task> all = Store.load(c);
        for (Task t : all) schedule(c, t);
    }

    /** Agenda (ou cancela) o alarme de uma tarefa. */
    public static void schedule(Context c, Task t) {
        if (t == null) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = alarmIntent(c, t.id);
        if (!Prefs.remindersEnabled(c) || t.done || !t.hasTime
                || t.dueAt <= System.currentTimeMillis()) {
            am.cancel(pi);
            return;
        }

        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            // Sem a permissão de alarmes exatos, usa um alarme de relógio:
            // continua preciso e é sempre permitido pelo sistema.
            try {
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(t.dueAt, showIntent(c)), pi);
            } catch (SecurityException e) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.dueAt, pi);
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.dueAt, pi);
        }
    }

    public static void cancel(Context c, long taskId) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(alarmIntent(c, taskId));
    }

    /** Cancela todos os alarmes conhecidos (usado ao desativar lembretes). */
    public static void cancelAll(Context c) {
        for (Task t : Store.load(c)) cancel(c, t.id);
    }

    /** Agenda um alarme avulso para daqui a alguns minutos (adiar). */
    public static void snooze(Context c, long taskId, long minutes) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        long when = System.currentTimeMillis() + minutes * 60_000L;
        PendingIntent pi = alarmIntent(c, taskId);
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            try {
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(when, showIntent(c)), pi);
            } catch (SecurityException e) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        }
    }

    private static PendingIntent alarmIntent(Context c, long taskId) {
        Intent i = new Intent(c, ReminderReceiver.class)
                .setAction(ACTION_REMIND)
                .putExtra(EXTRA_TASK_ID, taskId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(c, requestCode(taskId), i, flags);
    }

    /** Intent exibido ao tocar no ícone de alarme do sistema. */
    private static PendingIntent showIntent(Context c) {
        Intent i = new Intent(c, com.fluxo.tarefas.MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(c, 900001, i, flags);
    }

    static int requestCode(long taskId) {
        return (int) (taskId ^ (taskId >>> 32));
    }

    /** Ícone pequeno usado nas notificações (silhueta branca). */
    static int smallIcon() {
        return R.drawable.ic_tasklist;
    }
}
