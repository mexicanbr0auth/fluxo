package com.fluxo.tarefas.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fluxo.tarefas.data.Store;
import com.fluxo.tarefas.model.Task;

/** Recebe o alarme agendado e publica a notificação de lembrete. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        long id = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, 0L);
        Task t = Store.find(c, id);
        if (t == null || t.done) return;
        ReminderNotifier.show(c, t);
    }
}
