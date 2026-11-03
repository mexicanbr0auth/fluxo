package com.fluxo.tarefas.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.fluxo.tarefas.EditorActivity;
import com.fluxo.tarefas.R;
import com.fluxo.tarefas.model.Task;
import com.fluxo.tarefas.ui.Dates;
import com.fluxo.tarefas.ui.Theme;

/**
 * Notificação de lembrete com som de alarme, vibração e ações rápidas.
 */
public final class ReminderNotifier {

    public static final String CHANNEL_ID = "fluxo_reminders";
    public static final String ACTION_DONE = "com.fluxo.tarefas.action.DONE";
    public static final String ACTION_SNOOZE = "com.fluxo.tarefas.action.SNOOZE";

    private ReminderNotifier() {
    }

    public static void ensureChannel(Context c) {
        NotificationManager nm =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (sound == null) sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Lembretes de tarefas", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Alarmes nos horários das suas tarefas");
        ch.setSound(sound, attrs);
        ch.enableVibration(true);
        ch.setVibrationPattern(new long[]{0, 400, 220, 400});
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }

    public static void show(Context c, Task t) {
        ensureChannel(c);

        String text = t.category == null || t.category.isEmpty()
                ? "Horário: " + Dates.time(t.dueAt)
                : t.category + " • " + Dates.time(t.dueAt);

        Intent open = t.id > 0
                ? new Intent(c, EditorActivity.class)
                        .putExtra("task_id", t.id)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                : new Intent(c, com.fluxo.tarefas.MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent content = PendingIntent.getActivity(
                c, ReminderScheduler.requestCode(t.id), open, flags);

        Intent done = new Intent(c, ReminderActionReceiver.class)
                .setAction(ACTION_DONE)
                .putExtra(ReminderScheduler.EXTRA_TASK_ID, t.id);
        PendingIntent donePi = PendingIntent.getBroadcast(
                c, ReminderScheduler.requestCode(t.id), done, flags);

        Intent snooze = new Intent(c, ReminderActionReceiver.class)
                .setAction(ACTION_SNOOZE)
                .putExtra(ReminderScheduler.EXTRA_TASK_ID, t.id);
        PendingIntent snoozePi = PendingIntent.getBroadcast(
                c, ReminderScheduler.requestCode(t.id), snooze, flags);

        Notification.Builder b = new Notification.Builder(c, CHANNEL_ID)
                .setSmallIcon(ReminderScheduler.smallIcon())
                .setContentTitle(t.title == null || t.title.isEmpty() ? "Lembrete" : t.title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(
                        t.notes == null || t.notes.isEmpty() ? text : t.notes + "\n" + text))
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(Notification.PRIORITY_HIGH)
                .setColor(Theme.palette(c).primary)
                .setWhen(t.dueAt)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(content);

        if (t.id > 0) {
            b.addAction(new Notification.Action.Builder(
                            Icon.createWithResource(c, R.drawable.ic_check),
                            "Concluir", donePi).build())
                    .addAction(new Notification.Action.Builder(
                            Icon.createWithResource(c, R.drawable.ic_time),
                            "Adiar 10 min", snoozePi).build());
        }

        NotificationManager nm =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(notificationId(t.id), b.build());
    }

    /** Mostra um lembrete de exemplo na hora, para testar som e vibração. */
    public static void showTest(Context c) {
        Task t = new Task();
        t.id = -424242L;
        t.title = "Lembrete de teste";
        t.notes = "Se você ouviu o som e sentiu a vibração, os lembretes estão funcionando.";
        t.category = "Fluxo";
        t.dueAt = System.currentTimeMillis();
        t.hasTime = true;
        show(c, t);
    }

    public static void dismiss(Context c, long taskId) {
        NotificationManager nm =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(notificationId(taskId));
    }

    private static int notificationId(long taskId) {
        return ReminderScheduler.requestCode(taskId);
    }
}
