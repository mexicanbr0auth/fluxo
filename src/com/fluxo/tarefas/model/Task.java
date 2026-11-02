package com.fluxo.tarefas.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Task {

    public long id;
    public String title = "";
    public String notes = "";
    public String category = "";
    public int priority = 0; // 0 = nenhuma, 1 = baixa, 2 = média, 3 = alta
    public boolean done = false;
    public boolean hasTime = false;
    public long dueAt = 0L;
    public long createdAt = 0L;
    public long completedAt = 0L;
    /** 0 = não repete, 1 = diária, 2 = dias úteis, 3 = semanal, 4 = mensal */
    public int repeat = 0;
    /** id da próxima ocorrência já criada (evita duplicar ao concluir) */
    public long nextId = 0L;

    public static Task create(String title) {
        Task t = new Task();
        t.id = System.currentTimeMillis() * 1000L + (long) (Math.random() * 1000);
        t.title = title;
        t.createdAt = System.currentTimeMillis();
        return t;
    }

    public Task copy() {
        Task t = new Task();
        t.id = System.currentTimeMillis() * 1000L + (long) (Math.random() * 1000);
        t.title = title;
        t.notes = notes;
        t.category = category;
        t.priority = priority;
        t.done = false;
        t.hasTime = hasTime;
        t.dueAt = dueAt;
        t.repeat = repeat;
        t.createdAt = System.currentTimeMillis();
        return t;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("title", title);
            o.put("notes", notes);
            o.put("category", category);
            o.put("priority", priority);
            o.put("done", done);
            o.put("hasTime", hasTime);
            o.put("dueAt", dueAt);
            o.put("createdAt", createdAt);
            o.put("completedAt", completedAt);
            o.put("repeat", repeat);
            o.put("nextId", nextId);
        } catch (JSONException ignored) {
        }
        return o;
    }

    public static Task fromJson(JSONObject o) {
        Task t = new Task();
        t.id = o.optLong("id");
        t.title = o.optString("title", "");
        t.notes = o.optString("notes", "");
        t.category = o.optString("category", "");
        t.priority = o.optInt("priority", 0);
        t.done = o.optBoolean("done", false);
        t.hasTime = o.optBoolean("hasTime", false);
        t.dueAt = o.optLong("dueAt", 0L);
        t.createdAt = o.optLong("createdAt", 0L);
        t.completedAt = o.optLong("completedAt", 0L);
        t.repeat = o.optInt("repeat", 0);
        t.nextId = o.optLong("nextId", 0L);
        return t;
    }
}
