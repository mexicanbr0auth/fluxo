package com.fluxo.tarefas.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.fluxo.tarefas.model.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class Store {

    private static final String FILE = "fluxo_data";
    private static final String KEY = "tasks";

    private Store() {
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static List<Task> load(Context c) {
        ArrayList<Task> out = new ArrayList<>();
        String raw = sp(c).getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) out.add(Task.fromJson(o));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static void save(Context c, List<Task> tasks) {
        JSONArray arr = new JSONArray();
        for (Task t : tasks) arr.put(t.toJson());
        sp(c).edit().putString(KEY, arr.toString()).apply();
    }

    public static void upsert(Context c, Task task) {
        List<Task> all = load(c);
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id == task.id) {
                all.set(i, task);
                found = true;
                break;
            }
        }
        if (!found) all.add(task);
        save(c, all);
    }

    public static void delete(Context c, long id) {
        List<Task> all = load(c);
        for (int i = all.size() - 1; i >= 0; i--) {
            if (all.get(i).id == id) all.remove(i);
        }
        save(c, all);
    }

    public static Task find(Context c, long id) {
        for (Task t : load(c)) {
            if (t.id == id) return t;
        }
        return null;
    }

    public static int clearCompleted(Context c) {
        List<Task> all = load(c);
        int removed = 0;
        for (int i = all.size() - 1; i >= 0; i--) {
            if (all.get(i).done) {
                all.remove(i);
                removed++;
            }
        }
        save(c, all);
        return removed;
    }

    public static List<String> categories(Context c) {
        List<String> out = new ArrayList<>();
        for (Task t : load(c)) {
            if (!t.category.isEmpty() && !out.contains(t.category)) out.add(t.category);
        }
        return out;
    }
}
