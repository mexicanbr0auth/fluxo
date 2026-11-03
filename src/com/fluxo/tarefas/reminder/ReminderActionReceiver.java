package com.fluxo.tarefas.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fluxo.tarefas.data.Store;
import com.fluxo.tarefas.model.Task;
import com.fluxo.tarefas.ui.Dates;

/** Executa as ações "Concluir" e "Adiar" direto da notificação. */
public class ReminderActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        long id = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, 0L);
        Task t = Store.find(c, id);
        if (t == null) return;

        if (ReminderNotifier.ACTION_DONE.equals(intent.getAction())) {
            complete(c, t);
        } else if (ReminderNotifier.ACTION_SNOOZE.equals(intent.getAction())) {
            ReminderNotifier.dismiss(c, id);
            ReminderScheduler.snooze(c, id, 10);
        }
    }

    private void complete(Context c, Task t) {
        if (!t.done) {
            long now = System.currentTimeMillis();
            t.done = true;
            t.completedAt = now;

            if (t.repeat > 0) {
                Task existing = t.nextId > 0 ? Store.find(c, t.nextId) : null;
                if (existing == null) {
                    Task next = t.copy();
                    next.dueAt = Dates.nextOccurrence(t.dueAt, t.repeat, t.weekdays, now);
                    next.nextId = 0L;
                    t.nextId = next.id;
                    Store.upsert(c, t);
                    Store.upsert(c, next);
                } else {
                    Store.upsert(c, t);
                }
            } else {
                Store.upsert(c, t);
            }
        }
        ReminderScheduler.cancel(c, t.id);
        ReminderNotifier.dismiss(c, t.id);
        ReminderScheduler.syncAll(c);
    }
}
